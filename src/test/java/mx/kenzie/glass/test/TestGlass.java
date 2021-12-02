package mx.kenzie.glass.test;

import mx.kenzie.glass.Glass;
import mx.kenzie.glass.Window;

public class TestGlass extends Glass {
    
    public <Template extends Window> byte[] getWindow(Class<Template> template, Object target) {
        final Class<?> targetType = target.getClass();
        final String hash = template.hashCode() + "" + targetType.hashCode();
        return this.writeConnectingCode(template, targetType, "mx/kenzie/glass/generated/Frame_" + hash, "");
    }
    
    public <Template extends Window> byte[] getWindow(Class<Template> template, Object target, String handler) {
        final Class<?> targetType = target.getClass();
        final String hash = template.hashCode() + "" + targetType.hashCode();
        return this.writeConnectingCode(template, targetType, "mx/kenzie/glass/generated/Frame_" + hash, handler);
    }
    
}

