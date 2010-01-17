package test;

public class Wtf {
    
    public static void main(String[] args) {
	System.out.println(new Wtf().run());
    }
    
    @SuppressWarnings("finally")
    public boolean run() {
	try {
	    return true;
	} finally {
	    return false;
	}
    }
}
