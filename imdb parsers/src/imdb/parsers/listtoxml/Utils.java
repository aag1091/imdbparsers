package imdb.parsers.listtoxml;

import java.util.ArrayList;
import java.util.List;


public class Utils {

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
    
}
