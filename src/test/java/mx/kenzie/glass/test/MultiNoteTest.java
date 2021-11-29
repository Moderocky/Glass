package mx.kenzie.glass.test;

import mx.kenzie.glass.annotation.MultiTarget;
import mx.kenzie.glass.annotation.Target;
import org.junit.Test;

import java.lang.annotation.Annotation;

public class MultiNoteTest {
    
    @Test
    public void extractMulti() {
        final Annotation[] annotations = Blob.class.getAnnotations();
        assert annotations.length == 1;
        assert annotations[0] instanceof MultiTarget;
        final MultiTarget multi = (MultiTarget) annotations[0];
        assert multi.value().length == 2;
        final Target first = multi.value()[0];
        final Target second = multi.value()[1];
        assert first.handler().equals("1_16");
        assert second.handler().equals("1_17");
    }
    
    @Target(handler = "1_16")
    @Target(handler = "1_17")
    interface Blob {
    
    }
    
}
