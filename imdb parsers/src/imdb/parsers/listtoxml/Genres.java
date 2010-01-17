package imdb.parsers.listtoxml;

import java.util.HashMap;
import java.util.Map;


public class Genres extends Parser {
    
    public enum COLUMNS {genre};
    
    public Genres(String filePath){super(filePath);}
    public Genres(){super();}
    
    protected String getListFilenameWithoutExtension() {return "genres";}
    protected int getNumberOfLinesToSkipBeforeStart() {return 2;}
    
    protected boolean isStart(String line) {
	return line.contains("8: THE GENRES LIST");
    }
    
    protected boolean isEndOfData(String line) {
	return false; // use end of file
    }

    protected Map<String, String> parseLine(String line) {
	ListToXML.LOG.finest("genres parsing line: "+line);
	// genres list format:
	// (name) (\t*) (genre)
	Map<String, String> r = new HashMap<String, String>();
	String[] lineParts = line.split("\t");
	String fullName = lineParts[0];
	if (!Movies.isUseful(fullName)) return null; 
	String genre = lineParts[lineParts.length - 1];
	r.putAll(Movies.getMovieKey(fullName));
	r.put(COLUMNS.genre.name(), genre);
	return r;
    }

}
