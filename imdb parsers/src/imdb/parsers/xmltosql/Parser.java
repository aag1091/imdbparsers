package imdb.parsers.xmltosql;

import imdb.parsers.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


public abstract class Parser {
    
    private static XMLInputFactory xif = XMLInputFactory.newInstance();
    
    private File inFile;
    private XMLStreamReader in;
    protected long inRecordNumber;
    private Connection conn;
    
    public Parser(Connection conn) {
	inFile = new File(getXMLFilenameWithoutExtension() + getXMLExtension());
	this.conn = conn;
	getReader();
    }
    
    public Parser(String filePath, Connection conn) {
	inFile = new File(filePath, getXMLFilenameWithoutExtension() + getXMLExtension());
	this.conn = conn;
	getReader();
    }
    
    public void parse() {
	XMLToSQL.LOG.fine("Parsing: " + getXMLFilenameWithoutExtension() + getXMLExtension());
	// prepare progress reporting
	prepareProgressReporting();
	// prepare (create tables)
	try {
	    beforeInserts(conn);
	} catch (SQLException e) {
	    XMLToSQL.LOG.log(Level.SEVERE, "SQL error", e);
	    throw new RuntimeException(e);
	}
	// populate table
	try {
	    Map<String, String> columnNameValueMap = null;
	    while (in.hasNext()) {
		int event = in.next();
		if (event == XMLStreamConstants.START_ELEMENT && in.getLocalName().equals("records")) {
		    // do nothing
		} else if (event == XMLStreamConstants.START_ELEMENT && in.getLocalName().equals("record")) {
		    // open record element
		    columnNameValueMap = new HashMap<String, String>();
		    inRecordNumber++;
		    printProgressReport();
		} else if (event == XMLStreamConstants.START_ELEMENT) {
		    // not <records> or <record>, must be an element inside <record>
		    columnNameValueMap.put(in.getLocalName(), in.getElementText());
		} else if (event == XMLStreamConstants.END_ELEMENT && in.getLocalName().equals("record")) {
		    try {
			insertValues(conn, columnNameValueMap);
		    } catch (SQLException e) {
			XMLToSQL.LOG.log(Level.WARNING, "SQL Error", e);
		    }
		    columnNameValueMap = null;
		}
	    }
	    XMLToSQL.LOG.fine("Finished Parsing: " + getXMLFilenameWithoutExtension() + getXMLExtension());
	} catch (XMLStreamException e) {
	    throw new RuntimeException(e);
	} finally {
	    try {
		in.close();
	    } catch (Exception e) {}
	}
	
    }
    
    /**
     * Called on Parser before parse() is called.
     * Here we just drop tables.
     */
    public void beforeParse(){
	beforeCreateTables(conn);
    }
       
    protected void insertValues(Connection conn, Map<String, String> columnNameValueMap) throws SQLException {
	// before insert
	beforeInsert(conn, columnNameValueMap);
	// insert
	NamedParameterStatement namedStmt = null;
	// build a query that uses placeholders for values
	// this allows us to sanitize input, especially needed in this situation where movies can have single and double
	// quotes in their names
	String query = getInsertStatement(columnNameValueMap);
	XMLToSQL.LOG.finer(query);
	try {
	    namedStmt = new NamedParameterStatement(conn, query);
	    for (String columnName : columnNameValueMap.keySet()) {
		setValue(namedStmt, columnName, columnNameValueMap.get(columnName));
	    }
	    XMLToSQL.LOG.fine("Prepared statement: " + namedStmt.toString());
	    namedStmt.executeUpdate();
	} catch (SQLException e) {
	    if (e.getMessage().startsWith("Duplicate")) {
		XMLToSQL.LOG.finer("Attmpted to insert a Duplicate entry, ignoring");
	    } else {
		XMLToSQL.LOG.log(Level.SEVERE, "SQL exception, query: " + query + ", prepared statement: " + namedStmt.toString(), e);
		throw new SQLException(e);
	    }
	} finally {
	    if (namedStmt != null) namedStmt.close();
	}
    }
    
    /*
     * Reader
     */
    private void getReader() {
	try {
	    if (in != null) in.close();
	    in = xif.createXMLStreamReader(new FileInputStream(inFile));
	} catch (IOException e) {
	    throw new RuntimeException(e);
	} catch (XMLStreamException e) {
	    throw new RuntimeException(e);
	}
    }
    
    /*
     * Methods expected to be overloaded sometimes
     */
    protected String getXMLExtension() {
	return ".xml";
    }
    
    protected void beforeCreateTables(Connection conn) {
	String dropQuery = "DROP TABLE IF EXISTS " + getTableName();
	Statement stmt = null;
	try {
	    stmt = conn.createStatement();
	    stmt.executeUpdate(dropQuery);
	} catch (SQLException e) {
	    XMLToSQL.LOG.log(Level.SEVERE, "SQL error, query: " + dropQuery, e);
	    // most likely a table already dropped/never created
	} finally {
	    try {
		stmt.close();
	    } catch (SQLException e) {}
	}
    }
    
    /**
     * given column names, e.g. {B, A, C}
     * returns INSERT INTO table (B,A,C) VALUES (:B,:A,:C);
     * where :X is a placeholder, which can be set using stmt.setString("X", columnNameValueMap.get(X))
     */
    protected String getInsertStatement(Map<String, String> columnNameValueMap) {
	List<String> columnNamesOrdered = new ArrayList<String>(columnNameValueMap.keySet());
	String columnNamesString = Utils.joinStringList(columnNamesOrdered, ",");
	List<String> placeholders = new ArrayList<String>();
	for (String columnName : columnNamesOrdered) {
	    placeholders.add(":" + columnName);
	}
	String placeholdersString = Utils.joinStringList(placeholders, ",");
	return "INSERT INTO " + getTableName() + " (" + columnNamesString + ") VALUES (" + placeholdersString + ");";
    }
    
    /*
     * Abstract methods
     */
    protected abstract String getXMLFilenameWithoutExtension();
    
    protected abstract String getTableName();
    
    protected abstract void beforeInserts(Connection conn) throws SQLException;
    
    protected abstract void beforeInsert(Connection conn, Map<String, String> columnNameValueMap) throws SQLException;
    
    protected abstract void setValue(NamedParameterStatement namedStmt, String columnKey, String valueString) throws SQLException;
    
    /*
     * Progress bar
     */
    private long numberOfRecordsInXML;
    private long timeAtStart;
    private long timeAtLastReport;
    
    private void prepareProgressReporting() {
	numberOfRecordsInXML = 0;
	timeAtStart = System.currentTimeMillis();
	timeAtLastReport = timeAtStart;
	try {
	    while (in.hasNext()) {
		int event = in.next();
		if (event == XMLStreamConstants.START_ELEMENT && in.getLocalName().equals("record")) {
		    numberOfRecordsInXML++;
		}
	    }
	} catch (XMLStreamException e) {
	    XMLToSQL.LOG.log(Level.WARNING, "Could not count number of lines in file in preperation for progress reporting", e);
	}
	getReader();
    }
    
    private void printProgressReport() {
	if (inRecordNumber > numberOfRecordsInXML) return;
	//
	long timeSinceLastReport = System.currentTimeMillis() - timeAtLastReport;
	if (timeSinceLastReport < 1000) return;
	timeAtLastReport = System.currentTimeMillis();
	//
	String str = getXMLFilenameWithoutExtension()+getXMLExtension() + " [";
	int cols = 20;
	float percent = (float) inRecordNumber / (float) numberOfRecordsInXML;
	int colsFull = (int) (percent * cols);
	for (int i = 0; i < cols; i++) {
	    str += i <= colsFull ? "=" : " ";
	}
	str += "] \t" + (((float) ((int) (percent * 10000))) / 100) + "% \t" + inRecordNumber + " / " + numberOfRecordsInXML;
	System.out.println(str);
    }
}
