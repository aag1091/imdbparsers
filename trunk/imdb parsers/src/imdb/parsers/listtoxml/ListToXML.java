package imdb.parsers.listtoxml;

import imdb.parsers.Utils;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses each .list and outputs to a .xml
 * Explicitly uses UTF-8 encoding.
 */
public class ListToXML{
    

    //The parsers to run. Must be of type Parser.
    @SuppressWarnings("unchecked")
    Class[] PARSERS = {Movies.class, Genres.class, Ratings.class, AkaTitles.class};
    
    public static Logger LOG = Utils.getConsoleLogger(ListToXML.class);
    private static final String USAGE = "Usage: java imdb.parsers.listtoxml.ListToXML parse:classname-or-all [filepath:path/to/directory/holding/lists] [debug]\n" +
    		"Example: java -cp /path/to/here/* imdb.parsers.listtoxml.ListToXML parse:all filepath:files/lists/ debug\n" +
    		"Note: only parses movies and tv shows, and only those with a year specified\n" +
    		"Warning: overwrites existing xml files without asking, which is why the 'parse:' argument is required.";
    
    public static int maxNameLength = 0; //FIXME
    
    public static void main(String[] args) {
	ListToXML instance = new ListToXML();
	List<String> argsList = Utils.stringArrayToList(args);	
	if(argsList.contains("help") || argsList.contains("usage") || !Utils.containsStartsWith(argsList, "parse:")){
	    System.out.println(USAGE);
	    return;
	}
	if (argsList.contains("debug")) {
	    System.out.println("dev mode, logger set to show all messages");
	    LOG.setLevel(Level.ALL);
	}
	String parse = Utils.getStartsWith(argsList, "parse:").substring(6);
	if(!parse.equalsIgnoreCase("all")){
	    instance.onlyParse = parse;
	}
	if (Utils.containsStartsWith(argsList, "filepath:")) {
	    // try provided
	    String filePath = Utils.getStartsWith(argsList,"filepath:").substring(9);
	    File inputFilesPath = new File(filePath);
	    if (inputFilesPath.canRead()) {
		instance.filePath = filePath;
	    } else {
		System.out.println("Cannot read from filepath provided, resolves to: "+inputFilesPath.getAbsolutePath());
		System.out.println(USAGE);
		return;
	    }
	}
	instance.run();
    }
    
    private String filePath;
    private String onlyParse; // which class name to parse, if null will parse all in PARSERS[]
    
    public void run() {
	LOG.info("Parsing from List to XML");
	for (Class<Parser> parserClass : PARSERS) {
	    if(onlyParse != null && !parserClass.getSimpleName().equalsIgnoreCase(onlyParse)) continue;
	    LOG.finest("Instantiating parser: " + parserClass.getSimpleName());
	    Parser parser;
	    try {
		if (filePath == null) {
		    parser = parserClass.newInstance();
		} else {
		    @SuppressWarnings("unchecked")
		    Class[] constructorParameterSignature = {String.class};
		    Constructor<Parser> constructor = parserClass.getConstructor(constructorParameterSignature);
		    parser = constructor.newInstance(filePath);
		}
	    } catch (Exception e) {
		LOG.log(Level.SEVERE, "Could not instantiate a parser", e);
		continue;
	    }
	    LOG.fine("Parsing: " + parserClass.getSimpleName());
	    parser.parse();
	    LOG.fine("Parsing: " + parserClass.getSimpleName() + " - finished");
	}
	LOG.info("Parsing from List to XML - finished");
	System.out.println("maxNameLength:"+maxNameLength); // FIXME
    }
}
