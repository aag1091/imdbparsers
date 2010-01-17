package imdb.parsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Utils {

    private static final ConsoleHandler LOG_HANDLER = new ConsoleHandler();
    
    static{
	LOG_HANDLER.setLevel(Level.ALL);
    }
    
    @SuppressWarnings("unchecked")
    public static Logger getConsoleLogger(Class me){
	Logger r = Logger.getLogger(me.getName());
	r.setUseParentHandlers(false);
	r.addHandler(LOG_HANDLER);
	return r;
    }

    public static List<String> stringArrayToList(String[] arr){
	List<String> r = new ArrayList<String>();
	for (int i = 0; i < arr.length; i++) {
	    r.add(arr[i]);
	}
	return r;
    }
    
    public static boolean containsStartsWith(Collection<String> collection, String prefix){
        return Utils.getStartsWith(collection, prefix) != null;
    }

    public static String getStartsWith(Collection<String> collection, String prefix){
        for(String str : collection){
            if(str.startsWith(prefix)){
        	return str;
            }
        }
        return null;
    }
   

    /*
     * Collections
     */
    public static String join(String[] strings, String separator) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            if (i != 0)
        	sb.append(separator);
            sb.append(strings[i]);
        }
        return sb.toString();
    }

    /**
     * Splits a string at the points where there are two or more spaces
     * "a b  c      d" gives {"a b", "c", "d"} "  x" gives {"", "x"}
     * 
     * @throws Exception
     */
    public static String[] splitByMultipleSpaces(String in, int expectedNumberOfItems) throws IllegalArgumentException {
        String truncated = in;// .replaceAll("\\p{Cntrl}", ""); // remove
        // non-printable characters
        String[] inSplit = truncated.split("  ");
        List<String> rList = new ArrayList<String>();
        int added = 0;
        if (truncated.startsWith("  ")) {
            rList.add("");
            added = 1;
        }
        for (int i = 0; i < inSplit.length; i++) {
            if (!inSplit[i].isEmpty()) {
        	rList.add(inSplit[i].trim());
        	added++;
            }
        }
        String[] r = rList.toArray(new String[rList.size()]);
        if (rList.size() != expectedNumberOfItems) {
            throw new IllegalArgumentException("expectedNumberOfItems: " + expectedNumberOfItems + ", differs from actual size:"
        	    + rList.size() + ", in array:" + join(r, ","));
        }
        return r;
    }
    
    public static String joinStringList(List<String> list, String separator){
	String[] r = list.toArray(new String[list.size()]);
	return join(r, separator);
    }
}
