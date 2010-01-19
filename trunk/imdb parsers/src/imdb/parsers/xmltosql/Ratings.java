package imdb.parsers.xmltosql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;


public class Ratings extends Parser {
    
    enum COLUMNS {rating, votes};
    
    public static final String CREATE_RATINGS_TABLE = "CREATE TABLE ratings (movies_id INT, votes INT, rating FLOAT NOT NULL, FOREIGN KEY (movies_id) REFERENCES movies(id));";
    
    public Ratings(Connection conn) {super(conn);}
    public Ratings(String filePath, Connection conn) {super(filePath, conn);}
    
    @Override
    protected String getTableName() {return "ratings";}
    @Override
    protected String getXMLFilenameWithoutExtension() {return "ratings";}
    
    @Override
    protected void beforeInserts(Connection conn) throws SQLException {
	Statement stmt = null;
	try{
	    stmt = conn.createStatement();
	    stmt.executeUpdate(CREATE_RATINGS_TABLE);
	}finally{
	    stmt.close();
	}
    }
    
    @Override
    protected void beforeInsert(Connection conn, Map<String, String> columnNameValueMap) throws SQLException {}
    
    @Override
    protected void setValue(NamedParameterStatement namedStmt, String columnKey, String valueString) throws SQLException {
	if (columnKey.equals(COLUMNS.rating.name())) {
	    namedStmt.setFloat(columnKey, Float.valueOf(valueString));
	} else if (columnKey.equals(COLUMNS.votes.name())) {
	    namedStmt.setInt(columnKey, Integer.valueOf(valueString));
	} else {
	    namedStmt.setString(columnKey, valueString);
	}
    }
    
    @Override
    protected String getInsertStatement(Map<String, String> columnNameValueMap) {
	boolean inyearIsNull = !columnNameValueMap.containsKey(Movies.COLUMNS_FROM_XML.inyear.name());
	return "INSERT INTO ratings (movies_id, votes, rating) SELECT movies.id, :votes, :rating FROM movies WHERE (name = :name AND year = :year AND inyear " + (inyearIsNull?"IS NULL":"= :inyear") + ");";
    }
    
}
