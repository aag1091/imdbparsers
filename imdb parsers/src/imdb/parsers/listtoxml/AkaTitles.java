package imdb.parsers.listtoxml;

import java.util.HashMap;
import java.util.Map;


public class AkaTitles extends Parser{

    public enum COLUMNS {akaTitle};
    
    private String currentMovieName;
    private boolean skipGroup = false;
    
    public AkaTitles(String filePath){super(filePath);}
    public AkaTitles(){super();}
    
    protected String getListFilenameWithoutExtension() {return "aka-titles";}
    protected int getNumberOfLinesToSkipBeforeStart() {return 2;}
    
    protected boolean isStart(String line) {
	return line.contains("AKA TITLES LIST");
    }
    
    protected boolean isEndOfData(String line) {
	return line.contains("------------");
    }
    
    protected Map<String, String> parseLine(String line) {
	// aka-titles format:
	// (\n*)
	// name
	//   (space*) (aka (alt-name)) (\t) (language or country where the name is used)
	//   ^^^ this line one or more times
	// (\n*)
	
	if(skipGroup && !line.isEmpty()) return null;
	if(skipGroup && line.isEmpty()) skipGroup = false;
	// pre-row group
	if(currentMovieName == null && line.isEmpty()) return null;
	// first row: movie name, store and wait for next line
	if(currentMovieName == null && !line.isEmpty()){
	    if (!Movies.isUseful(line)){
		skipGroup = true;
		return null; 
	    }
	    currentMovieName = line;
	    return null;
	}
	// here currentMovieName must be non-null
	assert(currentMovieName != null);
	// after first row: aka title
	if(line.isEmpty()){
	    // end of row group, discard currentMovieName, no useful aka-title was found
	    currentMovieName = null;
	    return null;
	}
	// aka-title line
	String[] lineParts = line.split("\t");
	String akaTitleWrappedFull = lineParts[0];
	String akaWhereUsed = lineParts[lineParts.length - 1];
	if(usefulAkaTitle(akaWhereUsed)){
	    Map<String, String> r = new HashMap<String, String>();
	    r.putAll(Movies.getMovieKey(currentMovieName));
	    String akaTitleFull = stripAkaTitleWrapper(akaTitleWrappedFull);
	    String akaTitle = Movies.getMovieNameOnly(akaTitleFull);
	    r.put(COLUMNS.akaTitle.name(), akaTitle);
	    ListToXML.LOG.fine("record: "+currentMovieName);
	    return r;
	}
	return null;
    }
       
    private static boolean usefulAkaTitle(String akaWhereUsed){
	return (akaWhereUsed.contains("International") || akaWhereUsed.contains("English") || akaWhereUsed.contains("USA") || akaWhereUsed.contains("UK"));
    }
    
    private static String stripAkaTitleWrapper(String akaTitleWrapped){
	String r = akaTitleWrapped.trim().substring(5);
	r = r.substring(0, r.length()-1);
	return r;
    }

}
