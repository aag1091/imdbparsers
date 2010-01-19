package imdb.parsers.xmltosql;

import imdb.parsers.Utils;
import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * XML files to be parsed must in this format:
 * <records>
 * <record>
 * <name>value</name>
 * <name>value</name>
 * <name>value</name>
 * <name>value</name>
 * <record>
 * <record>
 * <name>value</name>
 * <name>value</name>
 * <record>
 * </records>
 * where name-value elements are key-value mappings, and must have children elements.
 * Explicitly uses UTF-8.
 */
public class XMLToSQL {
    
    // The parsers to run. Must be of type Parser.
    @SuppressWarnings("unchecked")
    Class[] PARSERS = {Movies.class, Genres.class, Ratings.class, AkaTitles.class};
    
    public static final Logger LOG = Utils.getConsoleLogger(XMLToSQL.class);
    private static final String USAGE = "Usage: java imdb.parsers.xmltosql.XMLToSQL connection:jdbc-url parse:classname-or-all [driver:jdbc-driver] [filepath:path/to/directory/holding/lists] [debug]\n"
					+ "Where: jdbc-url is in the format: \"jdbc:somejdbcvendor:otherdata\", and driver is the java path to the Driver class for a jdbc driver.\n"
					+ "Example: java -cp /path/to/here/* connection:jdbc:mysql://localhost/table?user=cat&pass=meow parse:all driver:com.mysql.jdbc.Driver filepath:files/xml/ debug \n"
					+ "Warning: XMLToSQL will drop tables if they exist, which is why the 'parse:' argument is required";
    
    public static void main(String[] args) {
	XMLToSQL instance = new XMLToSQL();
	List<String> argsList = Utils.stringArrayToList(args);
	if (argsList.contains("help") ||
	    argsList.contains("usage") ||
	    !Utils.containsStartsWith(argsList, "parse:") ||
	    !Utils.containsStartsWith(argsList, "parse:")) {
	    System.out.println(USAGE);
	    return;
	}
	if (argsList.contains("debug")) {
	    System.out.println("dev mode, logger set to show all messages");
	    LOG.setLevel(Level.ALL);
	}
	String parse = Utils.getStartsWith(argsList, "parse:").substring(6);
	if (!parse.equalsIgnoreCase("all")) {
	    instance.onlyParse = parse;
	}
	if (Utils.containsStartsWith(argsList, "filepath:")) {
	    // try provided
	    String filePath = Utils.getStartsWith(argsList, "filepath:").substring(9);
	    File inputFilesPath = new File(filePath);
	    if (inputFilesPath.canRead()) {
		instance.filePath = filePath;
	    } else {
		System.out.println("Cannot read from filepath provided, resolves to: " + inputFilesPath.getAbsolutePath());
		System.out.println(USAGE);
		return;
	    }
	}
	instance.jdbcConnection = Utils.getStartsWith(argsList, "connection:").substring(11);
	if (Utils.containsStartsWith(argsList, "driver:")) {
	    instance.jdbcDriver = Utils.getStartsWith(argsList, "driver:").substring(7);
	}
	instance.run();
    }
    
    
    private String jdbcConnection;
    private String jdbcDriver;
    private String filePath;
    private String onlyParse; // which class name to parse, if null will parse all in PARSERS[]
    
    public void run() {
	LOG.info("Parsing from XML to SQL");
	LOG.info("connection:"+jdbcConnection);
	LOG.info("driver:"+jdbcDriver);
	LOG.info("filePath:"+filePath);
	LOG.info("parse:"+onlyParse);
	if (jdbcConnection == null) throw new IllegalStateException();
	Connection conn;
	try {
	    if (jdbcDriver != null) {
		Class.forName(jdbcDriver);
	    }
	    conn = DriverManager.getConnection(jdbcConnection);
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException(e);
	}
	if (onlyParse != null) validateOnlyParse();
	for (Class<Parser> parserClass : PARSERS) {
	    if (onlyParse != null && !parserClass.getSimpleName().equalsIgnoreCase(onlyParse)) continue;
	    LOG.finest("Instantiating parser: " + parserClass.getSimpleName());
	    Parser parser;
	    try {
		if (filePath == null) {
		    @SuppressWarnings("unchecked")
		    Class[] constructorParameterSignature = {Connection.class};
		    Constructor<Parser> constructor = parserClass.getConstructor(constructorParameterSignature);
		    parser = constructor.newInstance(conn);
		} else {
		    @SuppressWarnings("unchecked")
		    Class[] constructorParameterSignature = {String.class, Connection.class};
		    Constructor<Parser> constructor = parserClass.getConstructor(constructorParameterSignature);
		    parser = constructor.newInstance(filePath, conn);
		}
	    } catch (Exception e) {
		LOG.log(Level.SEVERE, "Could not instantiate a parser", e);
		continue;
	    }
	    LOG.fine("Parsing: " + parserClass.getSimpleName());
	    parser.parse();
	    LOG.fine("Parsing: " + parserClass.getSimpleName() + " - finished");
	}
	try {
	    conn.close();
	} catch (SQLException e) {}
	LOG.info("Parsing from XML to SQL - finished");
    }
    
    
    private void validateOnlyParse() {
	boolean found = false;
	for (Class<Parser> parserClass : PARSERS) {
	    if (parserClass.getSimpleName().equalsIgnoreCase(onlyParse)) {
		found = true;
		break;
	    }
	}
	if (!found) throw new IllegalStateException("Specified parser not found: " + onlyParse);
    }
}
