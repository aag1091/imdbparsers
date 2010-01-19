package imdb.parsers.listtoxml;

import imdb.parsers.Utils;
import java.util.HashMap;
import java.util.Map;

/**
 * Note: for TV Shows, the list contains a rating for each episode. This parser averages the ratings and sums the votes, returning 1 record per TV Show.
 */
public class Ratings extends Parser {
    
    public enum COLUMNS {rating, votes};
    
    private String currentTitle;
    private String currentTitleFullName;
    private float ratingSum;
    private int numberOfEntries;
    private int votesSum;
    private boolean flushedLast = false;
    
    public Ratings(String filePath){super(filePath);}
    public Ratings(){super();}
    
    protected String getListFilenameWithoutExtension() {return "ratings";}
    protected int getNumberOfLinesToSkipBeforeStart() {return 2;}
    
    protected boolean isStart(String line) {
	return line.contains("MOVIE RATINGS REPORT");
    }
    
    /*
     * flushed last used so we can return the last record of the list, because we don't use lookahead
     */
    protected boolean isEndOfData(String line) {
	return flushedLast && isEndOfDataInternal(line);
    }
    
    private boolean isEndOfDataInternal(String line){
	return line.contains("--------------") || line.isEmpty();
    }

    protected Map<String, String> parseLine(String line) {
	// ratings list format:
	// [*(new)] (distribution) (votes) (rating) (name)
	Map<String, String> r = null;
	if(isEndOfDataInternal(line)){
	    flushedLast = true;
	    return getCurrentRecordSummed();
	}
	String[] lineParts = Utils.splitByMultipleSpaces(line, 5);
	String fullName = lineParts[4];
	if(!Movies.isUseful(fullName)) return null;
	//
	String name = Movies.getMovieNameOnly(fullName);
	String ratingStr = lineParts[3];
	String votesStr = lineParts[2];
	if(name.equals(currentTitle)){
	    // same title
	    ratingSum += Float.valueOf(ratingStr);
	    numberOfEntries++;
	    votesSum += Integer.valueOf(votesStr);
	}else{
	    if(currentTitle != null){
		// different title, sum, reset and return
		r = getCurrentRecordSummed();
	    }
	    // first or reset
	    currentTitle = name;
	    currentTitleFullName = fullName;
	    ratingSum = Float.valueOf(ratingStr);
	    votesSum = Integer.valueOf(votesStr);
	    numberOfEntries = 1;
	}
	return r;
    }
    
    private Map<String, String> getCurrentRecordSummed(){
	Map<String, String> r = new HashMap<String, String>();
	float avgRating = ratingSum / numberOfEntries;
	r.putAll(Movies.getMovieKey(currentTitleFullName));
	r.put(COLUMNS.rating.name(), String.valueOf(avgRating));
	r.put(COLUMNS.votes.name(), String.valueOf(votesSum));
	return r;
    }
}