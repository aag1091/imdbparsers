package imdb.parsers.xmltosql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class Movies extends Parser {
    
    private static final String TABLE_NAME = "movies";
    
    enum COLUMNS_FROM_XML {name, year, inyear, istv};
    
    public static final String KEY_COLUMNS = "name CHAR(255) NOT NULL, year INT NOT NULL, inyear CHAR(10)";
    public static final String PRIMARY_KEY_STRING = "PRIMARY KEY (name, year, inyear)";
    public static final String FOREIGN_KEY_STRING = "FOREIGN KEY (name, year, inyear) REFERENCES "+TABLE_NAME+"(name, year, inyear)";
    
    public Movies(Connection conn) {
	super(conn);
    }
    
    public Movies(String filePath, Connection conn) {
	super(filePath, conn);
    }
    
    protected String getTableName() {return TABLE_NAME;}
    protected String getXMLFilenameWithoutExtension() {return "movies";}
    
    protected String getCreateTableStatement() {
	return "CREATE TABLE "+TABLE_NAME+" ("+KEY_COLUMNS+", istv CHAR(1), "+PRIMARY_KEY_STRING+");";
    }
    
    protected void setValue(PreparedStatement stmt, int index, String columnKey, String valueString) throws SQLException {
	if (columnKey.equalsIgnoreCase(COLUMNS_FROM_XML.year.name())) {
	    try {
		stmt.setInt(index, Integer.parseInt(valueString));
	    } catch (NumberFormatException e) {
		throw new RuntimeException("text in year node in movies xml is not a number: " + valueString);
	    }
	} else {
	    stmt.setString(index, valueString);
	}
    }

    
}
