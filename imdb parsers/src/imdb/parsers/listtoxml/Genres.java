package imdb.parsers.listtoxml;

import java.util.HashMap;
import java.util.Map;


public class Genres extends OneLinePerRecordParser {
    
    public enum COLUMNS {name, genre};
    
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
	// genres list format:
	// (name) (\t*) (genre)
	Map<String, String> r = new HashMap<String, String>();
	String[] lineParts = line.split("\t");
	r.put(COLUMNS.name.toString(), lineParts[0]);
	r.put(COLUMNS.genre.toString(), lineParts[lineParts.length - 1]);
	return r;
    }

}
