package imdb.parsers.listtoxml;

import imdb.parsers.Utils;
import java.util.HashMap;
import java.util.Map;


public class Ratings extends Parser {
    
    public enum COLUMNS {rating, votes};
    
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
	String fullName = lineParts[4];
	if(!Movies.isUseful(fullName)) return null;
	r.putAll(Movies.getMovieKey(fullName));
	r.put(COLUMNS.rating.name(), lineParts[3]);
	r.put(COLUMNS.votes.name(), lineParts[2]);
	return r;
    }
}