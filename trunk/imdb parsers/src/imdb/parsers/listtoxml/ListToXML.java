package imdb.parsers.listtoxml;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses each .list and outputs to a .xml
 * Explicitly uses UTF-8 encoding.
 */
public class ListToXML {
    

    //The parsers to run. Must be of type Parser.
    @SuppressWarnings("unchecked")
    Class[] PARSERS = {Movies.class, Genres.class, Ratings.class, AkaTitles.class};
    
    private static final Logger LOG = Logger.getLogger(ListToXML.class.getName());
    public static final ConsoleHandler LOG_HANDLER = new ConsoleHandler();
    public static boolean DEV = false;
    private static final String USAGE = "Usage: java imdb.parsers.listtoxml.ListToXML [debug] [path to directory holding lists]";
    
    public static void main(String[] args) {
	List<String> argsList = new ArrayList<String>();
	for (int i = 0; i < args.length; i++) {
	    argsList.add(args[i]);
	}
	LOG.setUseParentHandlers(false);
	LOG_HANDLER.setLevel(Level.ALL);
	LOG.addHandler(LOG_HANDLER);
	if(!argsList.isEmpty() && ("help".equals(argsList.get(0)) || "usage".equals(argsList.get(0)))){
	    System.out.println(USAGE);
	    return;
	}
	if (!argsList.isEmpty() && "debug".equals(argsList.get(0))) {
	    System.out.println("dev mode, logger set to show all messages");
	    LOG.setLevel(Level.ALL);
	    DEV = true;
	    argsList.remove(0);
	}
	if (!argsList.isEmpty()) {
	    // try provided
	    File inputFilesPath = new File(argsList.get(0));
	    if (inputFilesPath.canRead()) {
		new ListToXML(argsList.get(0));
	    } else {
		System.out.println("Cannot read from path provided as input");
		System.out.println(USAGE);
	    }
	} else {
	    new ListToXML();
	}
    }
    
    
    public ListToXML(String filePath) {
	LOG.info("Parsing from List to XML");
	for (Class<Parser> parserClass : PARSERS) {
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
    }
    
    public ListToXML() {
	this(null);
    }
    

}
