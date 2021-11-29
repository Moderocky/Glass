package mx.kenzie.glass.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.TYPE, ElementType.METHOD})
@Repeatable(MultiTarget.class)
public @interface Target {
    
    String handler() default "";
    
    String name() default "";
    
    Class<?> returnType() default void.class;
    
    Class<?>[] parameterTypes() default {};
    
    String owner() default "";
    
}
