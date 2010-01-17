package imdb.parsers.xmltosql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class Genres extends Parser {
    
    enum COLUMNS {
	genre
    };
    
    public Genres(Connection conn) {
	super(conn);
    }
    
    public Genres(String filePath, Connection conn) {
	super(filePath, conn);
    }
    
    protected String getTableName() {
	return "genres";
    }
    
    protected String getXMLFilenameWithoutExtension() {
	return "genres";
    }
    
    protected String getCreateTableStatement() {
	return "CREATE TABLE genres (" + Movies.KEY_COLUMNS + ", genre char(100) NOT NULL, " + Movies.FOREIGN_KEY_STRING + ");";
    }
    
    protected void setValue(PreparedStatement stmt, int index, String columnKey, String valueString) throws SQLException {
	stmt.setString(index, valueString);
    }
}
