package mx.kenzie.glass.test;

import mx.kenzie.glass.Glass;
import mx.kenzie.glass.Window;
import mx.kenzie.glass.annotation.Target;
import org.junit.BeforeClass;
import org.junit.Test;

public class WindowTest {
    
    static final Glass GLASS = new Glass();
    
    @BeforeClass
    public static void load() {
        GLASS.createWindow(Simple.class, new Hidden1(), "1_16");
        GLASS.createWindow(Simple.class, new Hidden2(), "1_17");
        GLASS.createWindow(Complex.class, new Hidden1(), "1_16");
        GLASS.createWindow(Complex.class, new Hidden2(), "1_17");
        GLASS.createWindow(Mixed.class, new RealMixed());
    }
    
    @Test
    public void simple() {
        final Simple first = GLASS.createWindow(Simple.class, new Hidden1(), "1_16");
        assert first.getVersion() == 16;
        final Simple second = GLASS.createWindow(Simple.class, new Hidden2(), "1_17");
        assert second.getVersion() == 17;
    }
    
    @Test
    public void complex() {
        final Complex first = GLASS.createWindow(Complex.class, new Hidden1(), "1_16");
        assert first.getVersion() == 16;
        assert first.getName().equals("Henry");
        final Complex second = GLASS.createWindow(Complex.class, new Hidden2(), "1_17");
        assert second.getVersion() == 17;
        assert second.getName().equals("James");
        assert second.getLength("blob") == 4;
        assert second.string(1, "blob").equals("1blob");
        assert second.string("a", 3).equals("a3");
        final Complex third = GLASS.createWindow(Complex.class, new Hidden2(), "1_17");
        assert second.getClass() == third.getClass();
    }
    
    @Test
    public void mixedTypes() {
        final Mixed window = GLASS.createWindow(Mixed.class, new RealMixed());
        assert window.getDouble() == 2L;
        assert window.getInt() == 3L;
        assert window.getSetInt(4.5F) == 5;
    }
    
    public interface Mixed
        extends Window {
        
        @Target(returnType = double.class)
        long getDouble();
        
        @Target(returnType = int.class)
        long getInt();
        
        @Target(returnType = int.class, parameterTypes = int.class)
        long getSetInt(float f);
        
    }
    
    public static class RealMixed {
        
        public double getDouble() {
            return 2.5;
        }
        
        public int getInt() {
            return 3;
        }
        
        public int getSetInt(int i) {
            return i + 1;
        }
        
    }
    
    public interface Simple
        extends Window {
        
        @Target(handler = "1_16", name = "getVersion")
        @Target(handler = "1_17", name = "v")
        int getVersion();
        
    }
    
    public interface Complex
        extends Window {
        
        @Target(handler = "1_16", name = "getVersion")
        @Target(handler = "1_17", name = "v")
        int getVersion();
        
        @Target(handler = "1_16", name = "getName")
        @Target(handler = "1_17", name = "name")
        String getName();
        
        @Target(handler = "1_16", name = "lengthOf")
        @Target(handler = "1_17", name = "length")
        int getLength(String word);
        
        @Target
        String string(long number, String word);
        
        @Target(parameterTypes = {String.class, Number.class})
        String string(Object a, Object b);
        
    }
    
    public static class Hidden1 {
        
        public int getVersion() {
            return 16;
        }
        
        public String getName() {
            return "Henry";
        }
        
        public int lengthOf(String word) {
            return word.length();
        }
        
        public String string(long number, String word) {
            return number + word;
        }
        
        public String string(String a, Number b) {
            return a + b;
        }
        
    }
    
    public static class Hidden2 {
        
        public int v() {
            return 17;
        }
        
        public String name() {
            return "James";
        }
        
        public int length(String word) {
            return word.length();
        }
        
        public String string(long number, String word) {
            return number + word;
        }
        
        public String string(String a, Number b) {
            return a + b;
        }
        
    }
    
}
