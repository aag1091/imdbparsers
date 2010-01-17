package imdb.parsers.xmltosql;

import imdb.parsers.Utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
	// drop table
	String dropQuery = "DROP TABLE IF EXISTS " + getTableName();
	try {
	    Statement stmt = conn.createStatement();
	    stmt.executeUpdate(dropQuery);
	    stmt.close();
	} catch (SQLException e) {
	    XMLToSQL.LOG.log(Level.SEVERE, "SQL error, query: " + dropQuery, e);
	}
	// create table
	try {
	    Statement stmt = conn.createStatement();
	    stmt.executeUpdate(getCreateTableStatement());
	    stmt.close();
	} catch (SQLException e) {
	    XMLToSQL.LOG.log(Level.SEVERE, "SQL error, query: " + getCreateTableStatement(), e);
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
		} else if (event == XMLStreamConstants.START_ELEMENT) {
		    // not <records> or <record>, must be an element inside <record>
		    columnNameValueMap.put(in.getLocalName(), in.getElementText());
		} else if (event == XMLStreamConstants.END_ELEMENT && in.getLocalName().equals("record")) {
		    try{
		    insertValues(conn, columnNameValueMap);
		    }catch(SQLException e){
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
    
    
    protected void insertValues(Connection conn, Map<String, String> columnNameValueMap) throws SQLException {
	XMLToSQL.LOG.finer("insertValues called");
	PreparedStatement stmt = null;
	// build a query that uses "?" as placeholders for values
	// this allows us to sanitize input, especially needed in this situation where movies can have single and double
	// quotes in their names
	List<String> columnNamesOrdered = new ArrayList<String>(columnNameValueMap.keySet());
	String query = getInsertStatement(columnNamesOrdered);
	XMLToSQL.LOG.finer(query);
	try {
	    stmt = conn.prepareStatement(query);
	    int index = 1; // prepared statement index starts at 1
	    for (String columnName : columnNamesOrdered) {
		setValue(stmt, index, columnName, columnNameValueMap.get(columnName));
		index++;
	    }
	    XMLToSQL.LOG.fine("Prepared statement: " + stmt.toString());
	    stmt.executeUpdate();
	} catch (SQLException e) {
	    if (e.getMessage().startsWith("Duplicate entry")) {
		XMLToSQL.LOG.finer("Attmpted to insert a Duplicate entry, ignoring");
	    } else {
		XMLToSQL.LOG.log(Level.SEVERE, "SQL exception, query: " + query + ", prepared statement: " + stmt.toString(), e);
		throw new SQLException(e);
	    }
	} finally {
	    if (stmt != null) stmt.close();
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
    
    private String getInsertStatement(List<String> columnNamesOrdered) {
	String columnNamesString = Utils.joinStringList(columnNamesOrdered, ",");
	List<String> placeholders = new ArrayList<String>();
	for (int i = 0; i < columnNamesOrdered.size(); i++) {
	    placeholders.add("?");
	}
	String placeholdersString = Utils.joinStringList(placeholders, ",");
	return "INSERT INTO " + getTableName() + " (" + columnNamesString + ") VALUES (" + placeholdersString + ");";
    }
    
    /*
     * Abstract methods
     */
    protected abstract String getXMLFilenameWithoutExtension();
    
    protected abstract String getTableName();
    
    protected abstract String getCreateTableStatement();
    
    protected abstract void setValue(PreparedStatement stmt, int index, String columnKey, String valueString) throws SQLException;
    
}
