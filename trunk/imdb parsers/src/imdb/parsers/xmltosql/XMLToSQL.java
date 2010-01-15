package imdb.parsers.xmltosql;

import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * XML files to be parsed must in this format:
 * <records>
 * 	<record>
 * 		<name>value</name>
 * 		<name>value</name>
 * 		<name>value</name>
 * 		<name>value</name>
 * 	<record>
  * 	<record>
 * 		<name>value</name>
 * 		<name>value</name>
 * 	<record>
 * </records>
 * where name-value elements are key-value mappings, and must have children elements.
 */
public class XMLToSQL {
    
    //The parsers to run. Must be of type Parser.
    @SuppressWarnings("unchecked")
    Class[] PARSERS = {Movies.class, Genres.class, Ratings.class/*, AkaTitles.class*/};
    
    private static final Logger LOG = Logger.getLogger(XMLToSQL.class.getName());
    private static final String USAGE = "Usage: java imdb.parsers.xmltosql.XMLToSQL jdbc-string [driver:jdbc-driver] [debug] [directory xml files]\n" +
    		"Where: jdbc-string is in the format: \"jdbc:somejdbcvendor:other data needed by some jdbc vendor\", and driver is the java path to the Driver class for a jdbc driver, for example: " +
    		"\"jdbc:mysql://localhost/table?user=cat&pass=meow\" \"driver:com.mysql.jdbc.Driver\".\n" +
    		"Note: if a driver is specified, it must be specified in the classpath when running, e.g. java -cp /path/to/here/* \"jdbc:abc\" \"driver:xyz\" \"xmlfiles\".\n" +
    		"Note: XMLToSQL will only create a table if it does not exist (using \"CREATE TABLE IF NOT EXISTS table_name\").";
    
    public static void main(String[] args) {
	List<String> argsList = new ArrayList<String>();
	for (int i = 0; i < args.length; i++) {
	    argsList.add(args[i]);
	}
	//
	LOG.setUseParentHandlers(false);
	Handler handler = new ConsoleHandler();
	handler.setLevel(Level.ALL);
	LOG.addHandler(handler);
	//
	XMLToSQL instance = new XMLToSQL();
	//
	if(!argsList.isEmpty() && ("help".equals(argsList.get(0)) || "usage".equals(argsList.get(0)))){
	    System.out.println(USAGE);
	    return;
	}
	if (!argsList.isEmpty()) { // jdbc string
	    instance.jdbcString = argsList.get(0);
	    argsList.remove(0);
	}else{
	    System.out.println(USAGE);
	    return;
	}
	if (!argsList.isEmpty() && argsList.get(0).startsWith("driver:")) { // jdbc driver
	    instance.jdbcDriver = argsList.get(0).substring(7);
	    argsList.remove(0);
	}
	if (!argsList.isEmpty() && "debug".equals(argsList.get(0))) {
	    System.out.println("dev mode, logger set to show all messages");
	    LOG.setLevel(Level.ALL);
	    argsList.remove(0);
	}
	if (!argsList.isEmpty()) {
	    // try provided
	    File inputFilesPath = new File(argsList.get(0));
	    if (inputFilesPath.canRead()) {
		instance.filePath = argsList.get(0);
	    } else {
		System.out.println("Cannot read from path provided as input");
		System.out.println(USAGE);
	    }
	}
	instance.run();
    }
    
    private String jdbcString;
    private String jdbcDriver;
    private String filePath;
      
    public void run(){
	LOG.info("Parsing from XML to SQL");
	if(jdbcString == null) throw new IllegalStateException();
	Connection conn;
	try {
	    if(jdbcDriver != null) {
		Class.forName(jdbcDriver);
	    }
	    conn = DriverManager.getConnection(jdbcString);
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException(e);
	}
	
	//
	for (Class<Parser> parserClass : PARSERS) {
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
	LOG.info("Parsing from XML to SQL - finished");
    }
    
}
