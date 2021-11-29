package mx.kenzie.glass;

public class WindowFrame implements Window {
    
    protected final Object target;
    protected final Class<?> targetType;
    
    public WindowFrame(Object target) {
        this.target = target;
        this.targetType = target.getClass();
    }
    
    @Override
    public Object getTarget() {
        return target;
    }
    
    @Override
    public Class<?> getTargetType() {
        return targetType;
    }
    
}
