package imdb.parsers.xmltosql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


public abstract class Parser {
    
    private static final Logger    LOG = Logger.getLogger(Parser.class.getSimpleName());
    
    private static XMLInputFactory xif = XMLInputFactory.newInstance();
    
    private File		   inFile;
    private XMLStreamReader	in;
    private Connection	     conn;
    
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
	LOG.fine("Parsing: " + getXMLFilenameWithoutExtension() + getXMLExtension());
	try {
	    // attempt to create table if it does not exist
	    Statement stmt = conn.createStatement();
	    stmt.executeUpdate(getCreateTableStatement());
	    //
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
		    insertValues(conn, columnNameValueMap);
		    columnNameValueMap = null;
		}
	    }
	    LOG.fine("Finished Parsing: " + getXMLFilenameWithoutExtension() + getXMLExtension());
	} catch (XMLStreamException e) {
	    throw new RuntimeException(e);
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	} finally {
	    try {
		in.close();
		conn.close();
	    } catch (Exception e) {}
	}
	
    }
    
    
    protected void insertValues(Connection conn, Map<String, String> columnNameValueMap) throws SQLException {
	LOG.fine("insertValues called");
	Statement stmt = conn.createStatement();
	String columnNames = "";
	String values = "";
	boolean first = true;
	for (String key : columnNameValueMap.keySet()) {
	    if (!first) {
		// this acts like a join so there is no comma after the last item
		columnNames += ",";
		values += ",";
	    }
	    columnNames += key;
	    values += "\"" + columnNameValueMap.get(key) + "\"";
	    first = false;
	}
	String query = "INSERT INTO " + getTableName() + " (" + columnNames + ") VALUES (" + values + ");";
	LOG.fine(query);
	try {
	    stmt.executeUpdate(query);
	} catch (SQLException e) {
	    if (e.getMessage().startsWith("Duplicate entry")) {
		LOG.fine("Attmpted to insert a Duplicate entry, ignoring");
	    } else {
		throw new SQLException(e);
	    }
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
    
    /*
     * Abstract methods
     */
    protected abstract String getXMLFilenameWithoutExtension();
    
    protected abstract String getTableName();
    
    protected abstract String getCreateTableStatement();
    
    /**
     * a value may need to be wrapped to convert from data type String as stored in xml to a different data type. For
     * example, a "votes" column value might be wrapped in "number(" and ")" if the data type definition of the table
     * says it requires a number.
     */
    // protected abstract String getWrappedValue(String columnName);
    
}
