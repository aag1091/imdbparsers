package imdb.parsers.listtoxml;

import java.util.HashMap;
import java.util.Map;


public class Ratings extends OneLinePerRecordParser {
    
    public enum COLUMNS {name, rating, votes};
    
    public Ratings(String filePath){super(filePath);}
    public Ratings(){super();}
    
    protected String getListFilenameWithoutExtension() {return "ratings";}
    protected int getNumberOfLinesToSkipBeforeStart() {return 2;}
    
    protected boolean isStart(String line) {
	return line.contains("MOVIE RATINGS REPORT");
    }
    
    protected boolean isEndOfData(String line) {
	return line.contains("--------------") || line.isEmpty();
    }

    protected Map<String, String> parseLine(String line) {
	// ratings list format:
	// [*(new)] (distribution) (votes) (rating) (name)
	Map<String, String> r = new HashMap<String, String>();
	String[] lineParts = Utils.splitByMultipleSpaces(line, 5);
	r.put(COLUMNS.name.toString(), lineParts[4]);
	r.put(COLUMNS.rating.toString(), lineParts[3]);
	r.put(COLUMNS.votes.toString(), lineParts[2]);
	return r;
    }

}