package imdb.parsers.listtoxml;

import java.util.HashMap;
import java.util.Map;


public class Movies extends OneLinePerRecordParser {
    
    public enum COLUMNS {name, year};
    
    public Movies(String filePath){super(filePath);}
    public Movies(){super();}
    
    protected String getListFilenameWithoutExtension() {return "movies";}
    protected int getNumberOfLinesToSkipBeforeStart() {return 2;}
    
    protected boolean isStart(String line) {
	return line.contains("MOVIES LIST");
    }
    
    protected boolean isEndOfData(String line) {
	return line.contains("-------");
    }

    protected Map<String, String> parseLine(String line) {
	// movies list format:
	// (name [(TV)] [(V)] [(etc.)] (year)) (\t*) (year or "????")
	Map<String, String> r = new HashMap<String, String>();
	String[] lineParts = line.split("\t");
	r.put(COLUMNS.name.toString(), lineParts[0]);
	r.put(COLUMNS.year.toString(), lineParts[lineParts.length - 1]);
	return r;
    }

}
