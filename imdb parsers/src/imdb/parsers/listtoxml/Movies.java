package imdb.parsers.listtoxml;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Note: applies to both movies and TV, but ignores video games
 */
public class Movies extends Parser {
    
    // see test.Movies for documentation
    public static final String REGEX_YEAR = "\\(" + "([0-9]{4})" + "(/(I*))?" + "\\)";
    public static final String REGEX_YEAR_AND_AFTER = REGEX_YEAR + "(.*)";
    public static final String REGEX_OUTSIDE_DOUBLE_QUOTES = "[(^\")(\"$)]";
    public static final String REGEX_YEAR_MULTIPLE_NAMES_IN_YEAR = "(.*)" + "\\(" + "([0-9]{4})" + "(/(I*))" + "\\)" + "(.*)";
    public static final String REGEX_NAME_IN_YEAR = "(?<=\\(([0-9]{4})/)" + "(I*)" + "(?=\\))";
    public static final Pattern PATTERN_NAME_IN_YEAR = Pattern.compile(REGEX_NAME_IN_YEAR);
    public static final String REGEX_YEAR_FROM_NAME = "(?<=\\()" + "([0-9]{4})" + "(?=(/(I*))?\\))";
    public static final Pattern PATTERN_YEAR_FROM_NAME = Pattern.compile(REGEX_YEAR_FROM_NAME);
    
    /**
     * name (NOT NULL), year (NOT NULL), and inyear combined are unique
     */
    public enum KEY_COLUMNS {
	name, year, inyear
    }
    
    public enum COLUMNS {
	istv
    };
    
    public Movies(String filePath) {
	super(filePath);
    }
    
    public Movies() {
	super();
    }
    
    protected String getListFilenameWithoutExtension() {
	return "movies";
    }
    
    protected int getNumberOfLinesToSkipBeforeStart() {
	return 2;
    }
    
    protected boolean isStart(String line) {
	return line.contains("MOVIES LIST");
    }
    
    protected boolean isEndOfData(String line) {
	return line.contains("-------");
    }
    
    protected Map<String, String> parseLine(String line) {
	// movies list format:
	// (name) (year) [(TV)] [(V)] [(etc)] {(episode name)} (\t*) (year or "????")
	// note: episode name is surrounded by curly brackets
	// note: tv shows have name surrounded by quotes
	Map<String, String> r = new HashMap<String, String>();
	
	String[] lineParts = line.split("\t");
	String fullName = lineParts[0];
	// ignore yearColumn because other lists don't have it. Movies without a year in yearcolumn are rare,
	// (FIXME:assuming) most likely much less popular
	// String yearColumn = lineParts[lineParts.length - 1];
	if (!isUseful(fullName)) return null; 
	r.putAll(getMovieKey(fullName));
	if (isTVShow(fullName)) r.put(COLUMNS.istv.name(), "Y");
	return r;
    }
    
    /**
     * Strip everything but name.
     * 
     * @see http://www.imdb.com/help/show_leaf?titleformat
     * @see Movies in test package
     */
    public static String getMovieNameOnly(String fullName) {
	// FIXME
	String r = removeOutsideQuotes(stripYearAndAllAfter(fullName));
	if(r.length() > ListToXML.maxNameLength){
	    ListToXML.maxNameLength = r.length();
	}
	return r;
    }
    
    public static String removeOutsideQuotes(String fullName) {
	if (fullName.startsWith("\"") && fullName.endsWith("\"")) {
	    return fullName.substring(1, fullName.length() - 1);
	} else {
	    return fullName;
	}
    }
    
    public static String stripYearAndAllAfter(String fullName) {
	return fullName.replaceFirst(REGEX_YEAR_AND_AFTER, "").trim();
    }
    
    /*
     * year methods
     */
    // public static boolean hasYearInYearColumn(String yearColumn) {
    // try {
    // Integer.parseInt(yearColumn);
    // if this doesn't throw an exception parsing to number, then year is provided rather than ????
    // return true;
    // } catch (NumberFormatException e) {}
    // return false;
    // }
    
    // public static String getYearFromYearColumn(String yearColumn) {
    // return yearColumn.trim();
    // }
    
    public static boolean hasYearInName(String fullName) {
	try {
	    getYearFromName(fullName);
	    return true;
	} catch (IllegalArgumentException e) {
	    return false;
	}
    }
    
    public static String getYearFromName(String fullName) {
	Matcher m = PATTERN_YEAR_FROM_NAME.matcher(fullName);
	if (m.find()) {
	    return m.group();
	} else {
	    throw new IllegalArgumentException("no year found in: " + fullName + ", use hasYearInName() first");
	}
    }
    
    /*
     * get identifier when there are multiple movies in a certain year
     * e.g. (2008/I) returns I, (2008/II) returns II
     */
    public static boolean isUniqueNameForYear(String fullName) {
	return !fullName.matches(REGEX_YEAR_MULTIPLE_NAMES_IN_YEAR);
    }
    
    public static String getInYearIdentifier(String fullName) {
	Matcher m = PATTERN_NAME_IN_YEAR.matcher(fullName);
	if (m.find()) {
	    return m.group();
	} else {
	    throw new IllegalArgumentException("no In Year Identifier found, use isUniqueNameForYear() first");
	}
    }
    
    /*
     * @see http://www.imdb.com/help/search?domain=helpdesk_faq&index=2&file=title_formats
     */
    public static boolean isUseful(String fullName) {
	return (isMovie(fullName) || isTVShow(fullName)) && hasYearInName(fullName);
    }
    
    public static boolean isMovie(String fullName) {
	return (!isVideoGame(fullName) && !isTVShow(fullName));
    }
    
    public static boolean isVideoGame(String fullName) {
	return fullName.contains("(VG)");
    }
    
    public static boolean isTVShow(String fullName) {
	return fullName.contains("(TV)");
    }
    
    public static boolean isMadeForVideoMovie(String fullName) {
	return fullName.contains("(V)");
    }
    
    
    /**
     * Common: key
     */
    public static Map<String, String> getMovieKey(String fullName) {
	Map<String, String> r = new HashMap<String, String>();
	r.put(KEY_COLUMNS.name.name(), getMovieNameOnly(fullName));
	r.put(KEY_COLUMNS.year.name(), getYearFromName(fullName));
	if (!isUniqueNameForYear(fullName)) r.put(KEY_COLUMNS.inyear.name(), getInYearIdentifier(fullName));
	return r;
    }
}
