package imdb.parsers.xmltosql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class AkaTitles extends Parser {
    
    private static final String TABLE_NAME = "akatitles";
    
    public AkaTitles(Connection conn) {
	super(conn);
    }
    
    public AkaTitles(String filePath, Connection conn) {
	super(filePath, conn);
    }
    
    protected String getTableName() {
	return TABLE_NAME;
    }
    
    protected String getXMLFilenameWithoutExtension() {
	return "aka-titles";
    }
    
    protected String getCreateTableStatement() {
	return "CREATE TABLE "+TABLE_NAME+" ("+Movies.KEY_COLUMNS+", akatitle char(255) NOT NULL, "+Movies.FOREIGN_KEY_STRING+");";
    }
    
    protected void setValue(PreparedStatement stmt, int index, String columnKey, String valueString) throws SQLException {
	stmt.setString(index, valueString);
    }
}
