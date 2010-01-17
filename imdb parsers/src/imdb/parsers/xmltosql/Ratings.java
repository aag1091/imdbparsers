package imdb.parsers.xmltosql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class Ratings extends Parser {
    
    public static final String TABLE_NAME = "ratings";
    
    enum COLUMNS {
	rating, votes
    };
    
    public Ratings(Connection conn) {
	super(conn);
    }
    
    public Ratings(String filePath, Connection conn) {
	super(filePath, conn);
    }
    
    protected String getTableName() {
	return TABLE_NAME;
    }
    
    protected String getXMLFilenameWithoutExtension() {
	return "ratings";
    }
    
    protected String getCreateTableStatement() {
	return "CREATE TABLE " + TABLE_NAME + " (" + Movies.KEY_COLUMNS + ", votes int, rating float NOT NULL, " + Movies.FOREIGN_KEY_STRING + ");";
    }
    
    protected void setValue(PreparedStatement stmt, int index, String columnKey, String valueString) throws SQLException {
	if (columnKey.equals(COLUMNS.rating.name())) {
	    stmt.setFloat(index, Float.valueOf(valueString));
	} else if (columnKey.equals(COLUMNS.votes.name())) {
	    stmt.setInt(index, Integer.valueOf(valueString));
	} else {
	    stmt.setString(index, valueString);
	}
    }
    
}
