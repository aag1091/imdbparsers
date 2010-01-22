package imdb.parsers.listtoxml;

import imdb.parsers.Utils;
import java.util.HashMap;
import java.util.Map;

/**
 * Note: only parses ratings for movies and for-video-movies
 */
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
	//
	String ratingStr = lineParts[3];
	String votesStr = lineParts[2];
	r.putAll(Movies.getMovieKey(fullName));
	r.put(COLUMNS.rating.name(), String.valueOf(ratingStr));
	r.put(COLUMNS.votes.name(), String.valueOf(votesStr));
	return r;
    }
}