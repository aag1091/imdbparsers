package imdb.parsers.xmltosql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;


public class Movies extends Parser {
    
    enum COLUMNS_FROM_XML {name, year, inyear, istv};

    public static final String QUERY_EXISTS = "SELECT * FROM movies WHERE (name = ? AND year = ? AND inyear = ?);";
    public static final String CREATE_MOVIES_TABLE = "CREATE TABLE movies (id INT AUTO_INCREMENT PRIMARY KEY, name CHAR(255) NOT NULL, year INT NOT NULL, inyear CHAR(10), istv CHAR(1), CONSTRAINT UNIQUE CLUSTERED (name, year, inyear));";
    
    public Movies(Connection conn) {super(conn);}
    public Movies(String filePath, Connection conn) {super(filePath, conn);}
    
    @Override
    protected String getTableName() {return "movies";}
    @Override
    protected String getXMLFilenameWithoutExtension() {return "movies";}
    
    @Override
    protected void beforeInserts(Connection conn) throws SQLException{
	Statement stmt = null;
	try{
	    stmt = conn.createStatement();
	    stmt.executeUpdate(CREATE_MOVIES_TABLE);
	}finally{
	    stmt.close();
	}
    }
        
    @Override
    protected void beforeInsert(Connection conn, Map<String, String> columnNameValueMap) throws SQLException {}
        
    @Override
    protected void setValue(NamedParameterStatement namedStmt, String columnKey, String valueString) throws SQLException {
	if (columnKey.equalsIgnoreCase(COLUMNS_FROM_XML.year.name())) {
	    namedStmt.setInt(columnKey, Integer.parseInt(valueString));
	} else {
	    namedStmt.setString(columnKey, valueString);
	}
    }
}