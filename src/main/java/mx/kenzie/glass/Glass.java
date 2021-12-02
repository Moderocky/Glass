package mx.kenzie.glass;

import mx.kenzie.glass.annotation.MultiTarget;
import mx.kenzie.glass.annotation.Target;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class Glass {
    
    protected Map<String, Class<? extends Window.WindowFrame>> cache;
    protected RuntimeClassLoader loader = new RuntimeClassLoader();
    
    public Glass() {
        this.cache = new HashMap<>();
    }
    
    public <Template extends Window> Template createWindow(Class<Template> template, Object target) {
        return this.createWindow(template, target, "");
    }
    
    public <Template extends Window> Template createWindow(Class<Template> template, Object target, String handler) {
        if (!template.isInterface()) throw new IllegalArgumentException("Window requires an interface to implement.");
        final Class<?> type = target.getClass();
        final String hash = template.hashCode() + "" + type.hashCode();
        final Class<? extends Window.WindowFrame> frame;
        if (cache.containsKey(hash)) frame = cache.get(hash);
        else frame = this.createFrameClass(template, type, handler);
        return (Template) createFrame(frame, target);
    }
    
    protected Window.WindowFrame createFrame(Class<? extends Window.WindowFrame> type, Object target) {
        try {
            final Constructor<Window.WindowFrame> constructor = (Constructor<Window.WindowFrame>) type.getConstructor(Object.class);
            return constructor.newInstance(target);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("An impossible state has been met during frame creation.", e);
        }
    }
    
    protected Class<? extends Window.WindowFrame> createFrameClass(Class<?> template, Class<?> targetType, String handler) {
        final String hash = template.hashCode() + "" + targetType.hashCode();
        final byte[] bytecode = this.writeConnectingCode(template, targetType, "mx/kenzie/glass/generated/Frame_" + hash, handler);
        final Class<? extends Window.WindowFrame> loaded = (Class<? extends Window.WindowFrame>) this.loadClass("mx.kenzie.glass.generated.Frame_" + hash, bytecode);
        this.cache.put(hash, loaded);
        return loaded;
    }
    
    protected byte[] writeConnectingCode(Class<?> template, Class<?> targetType, String location, String handler) {
        final String temp = Type.getInternalName(template);
        final ClassWriter writer = new ClassWriter(0);
        writer.visit(V16, ACC_PUBLIC | ACC_SUPER, location, null, "mx/kenzie/glass/Window$WindowFrame", new String[]{temp, "mx/kenzie/glass/Window"});
        
        constructor:
        {
            final MethodVisitor visitor;
            visitor = writer.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Object;)V", null, null);
            visitor.visitCode();
            visitor.visitVarInsn(ALOAD, 0);
            visitor.visitVarInsn(ALOAD, 1);
            visitor.visitMethodInsn(INVOKESPECIAL, "mx/kenzie/glass/Window$WindowFrame", "<init>", "(Ljava/lang/Object;)V", false);
            visitor.visitInsn(RETURN);
            visitor.visitMaxs(2, 2);
            visitor.visitEnd();
        }
        
        for (Method method : template.getMethods()) {
            Target target = null;
            if (method.isAnnotationPresent(Target.class)) {
                final Target temporary = method.getAnnotation(Target.class);
                this.writeTarget(temporary, writer, location, method, targetType, temporary.handler());
            } else if (method.isAnnotationPresent(MultiTarget.class)) {
                final MultiTarget multi = method.getAnnotation(MultiTarget.class);
                for (final Target temporary : multi.value()) {
                    if (target == null && temporary.handler().equals("")) target = temporary;
                    else if (temporary.handler().equals(handler)) target = temporary;
                }
                if (target == null)
                    throw new IllegalArgumentException("No matching handler for method '" + method.getName() + "'");
                this.writeTarget(target, writer, location, method, targetType, target.handler());
            }
        }
        writer.visitEnd();
        
        return writer.toByteArray();
    }
    
    private void writeTarget(Target target, ClassWriter writer, String location, Method method, Class<?> targetType, String handler) {
        final String targetClass = Type.getInternalName(targetType);
        final MethodVisitor visitor;
        final Type methodType = Type.getType(method);
        visitor = writer.visitMethod(ACC_PUBLIC | ACC_BRIDGE, method.getName(), methodType.getDescriptor(), null, null);
        visitor.visitCode();
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitFieldInsn(GETFIELD, location, "target", "Ljava/lang/Object;");
        visitor.visitTypeInsn(CHECKCAST, targetClass); // CC is okay here, always right
        final StringBuilder builder = new StringBuilder().append("(");
        final int length = method.getParameterTypes().length;
        parameters:
        {
            final Class<?>[] targetParams = target.parameterTypes();
            if (targetParams.length == 0 && length == 0) break parameters;
            if (Arrays.equals(targetParams, method.getParameterTypes())) {
                int index = 0;
                for (Class<?> type : targetParams) {
                    builder.append(Type.getDescriptor(type));
                    visitor.visitVarInsn(20 + instructionOffset(type), ++index);
                    if (type == long.class || type == double.class) index++;
                }
            } else if (targetParams.length == 0) {
                int index = 0;
                for (Class<?> type : method.getParameterTypes()) {
                    builder.append(Type.getDescriptor(type));
                    visitor.visitVarInsn(20 + instructionOffset(type), ++index);
                    if (type == long.class || type == double.class) index++;
                }
            } else {
                if (targetParams.length != length)
                    throw new IllegalArgumentException("Target parameters do not conform to template parameters for '" + method.getName() + "'");
                int index = 0;
                for (int i = 0; i < targetParams.length; i++) {
                    final Class<?> a = method.getParameterTypes()[i];
                    final Class<?> b = targetParams[i];
                    if (a == b) {
                        builder.append(Type.getDescriptor(a));
                        visitor.visitVarInsn(20 + instructionOffset(a), ++index);
                    } else {
                        builder.append(Type.getDescriptor(b));
                        visitor.visitVarInsn(20 + instructionOffset(a), ++index);
                        this.doTypeConversion(visitor, a, b);
                    }
                    if (a == long.class || a == double.class) index++;
                }
            }
        }
        builder.append(")");
        if (method.getReturnType() == target.returnType()
            || target.returnType() == void.class) builder.append(Type.getDescriptor(method.getReturnType()));
        else builder.append(Type.getDescriptor(target.returnType()));
        final String name;
        if (target.name().equals("")) name = method.getName();
        else name = target.name();
        visitor.visitMethodInsn(INVOKEVIRTUAL, targetClass, name, builder.toString(), false);
        if (method.getReturnType() != target.returnType() && target.returnType() != void.class)
            this.doTypeConversion(visitor, target.returnType(), method.getReturnType());
        visitor.visitInsn(171 + instructionOffset(method.getReturnType()));
        final int offset = Math.max(wideIndexOffset(method.getParameterTypes(), method.getReturnType()), wideIndexOffset(target.parameterTypes(), target.returnType()));
        final int size = 1 + length + offset;
        visitor.visitMaxs(size, size);
        visitor.visitEnd();
    }
    
    private void doTypeConversion(MethodVisitor visitor, Class<?> from, Class<?> to) {
        if (from == to) return;
        if (from == void.class || to == void.class) return;
        if (from.isPrimitive() && to.isPrimitive()) {
            final int opcode;
            if (from == float.class) {
                if (to == double.class) opcode = F2D;
                else if (to == long.class) opcode = F2L;
                else opcode = F2I;
            } else if (from == double.class) {
                if (to == float.class) opcode = D2F;
                else if (to == long.class) opcode = D2L;
                else opcode = D2I;
            } else if (from == long.class) {
                if (to == float.class) opcode = L2F;
                else if (to == double.class) opcode = L2D;
                else opcode = L2I;
            } else {
                if (to == float.class) opcode = I2F;
                else if (to == double.class) opcode = I2D;
                else if (to == byte.class) opcode = I2B;
                else if (to == short.class) opcode = I2S;
                else if (to == char.class) opcode = I2C;
                else opcode = I2L;
            }
            visitor.visitInsn(opcode);
        } else if (from.isPrimitive() ^ to.isPrimitive()) {
            throw new IllegalArgumentException("Type wrapping is currently unsupported due to side-effects: '" + from.getSimpleName() + "' -> '" + to.getSimpleName() + "'");
        } else visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(to));
    }
    
    private int wideIndexOffset(Class<?>[] params, Class<?> ret) {
        int i = 0;
        for (Class<?> param : params) {
            i += wideIndexOffset(param);
        }
        return Math.max(i, wideIndexOffset(ret));
    }
    
    private int wideIndexOffset(Class<?> thing) {
        if (thing == long.class || thing == double.class) return 1;
        return 0;
    }
    
    private int instructionOffset(Class<?> type) {
        if (type == int.class) return 1;
        if (type == long.class) return 2;
        if (type == float.class) return 3;
        if (type == double.class) return 4;
        if (type == void.class) return 6;
        return 5;
    }
    
    private Class<?> loadClass(final String name, final byte[] bytes) {
        try {
            return Class.forName(name); // Can't duplicate-load
        } catch (Throwable ex) {
            return loader.loadClass(name, bytes);
        }
    }
    
    static class RuntimeClassLoader extends ClassLoader {
        
        public RuntimeClassLoader(String name, ClassLoader parent) {
            super(name, parent);
        }
        
        public RuntimeClassLoader(ClassLoader parent) {
            super(parent);
        }
        
        public RuntimeClassLoader() {
            super();
        }
        
        public Class<?> loadClass(String name, byte[] bytecode) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }
}
