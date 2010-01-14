package imdb.parsers.listtoxml;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class AkaTitles extends Parser{

    private static final Logger LOG = Logger.getLogger(AkaTitles.class.getSimpleName());
    
    public enum COLUMNS {name, akaTitle};
    
    public AkaTitles(String filePath){super(filePath);}
    public AkaTitles(){super();}
    
    protected String getListFilenameWithoutExtension() {return "aka-titles";}
    protected int getNumberOfLinesToSkipBeforeStart() {return 2;}
    
    protected boolean isStart(String line) {
	return line.contains("AKA TITLES LIST");
    }
    
    protected boolean isEndOfData(String line) {
	return line.contains("-------");
    }
       
    protected Map<String, String> parseNextRecord(BufferedReader in) {
	if(ListToXML.DEV) LOG.setLevel(Level.ALL);
	LOG.addHandler(ListToXML.LOG_HANDLER);
	//LOG.finest("parseNextRecord");
	return parseARecord(in);
    }
    
    /**
     * Recursive: if no usable/english aka-title is found for a movie it continues to the next movie
     */
    private Map<String, String> parseARecord(BufferedReader in) {
	// aka-titles format:
	// (\n*)
	// name
	//   (space*) (aka (alt-name)) (\t) (language or country where the name is used)
	//   ^^^ this line one or more times
	// (\n*)
	Map<String, String> r = new HashMap<String, String>();
	String line;
	try {
	    // first we skip any empty lines
	    while((line = in.readLine()) != null && line.isEmpty()){} // empty signifies before start of group
	    //LOG.finest("parseNextRecord, line: "+line);
	    // check for end of file or end of data
	    if(line == null || isEndOfData(line)) return null;
	    // store name
	    r.put(COLUMNS.name.toString(), line);
	    // next we go through the aka-titles and find the first english/international one
	    String akaTitle = null;
	    while((line = in.readLine()) != null && !line.isEmpty()){ // empty signifies end of group
		String[] lineParts = line.split("\t");
		String akaWhereUsed = lineParts[lineParts.length - 1];
		if(akaWhereUsed.contains("International") || akaWhereUsed.contains("English") || akaWhereUsed.contains("USA") || akaWhereUsed.contains("UK")){
		    akaTitle = lineParts[0].trim().substring(5).trim();
		    akaTitle = akaTitle.substring(0, akaTitle.length() - 1);
		    break;
		}
	    }
	    if(akaTitle == null){
		// no usable/english aka-title found, continues to next movie
		return parseARecord(in);
	    }
	    r.put(COLUMNS.akaTitle.toString(), akaTitle);
	    return r;
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }


    
}
