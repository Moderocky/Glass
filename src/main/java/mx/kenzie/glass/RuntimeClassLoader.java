package mx.kenzie.glass;

class RuntimeClassLoader extends ClassLoader {
    
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
