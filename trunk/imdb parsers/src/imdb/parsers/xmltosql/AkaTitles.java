package imdb.parsers.xmltosql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;


public class AkaTitles extends Parser {
    
    enum XML_COLUMNS {akaTitle};
    
    public static final String CREATE_AKA_TITLE_TABLE = "CREATE TABLE akatitles (movies_id INT, aka char(255) NOT NULL, FOREIGN KEY (movies_id) REFERENCES movies(id));";
    
    
    public AkaTitles(Connection conn) {super(conn);}
    public AkaTitles(String filePath, Connection conn) {super(filePath, conn);}
    
    @Override
    protected String getTableName() {return "akatitles";}
    @Override
    protected String getXMLFilenameWithoutExtension() {return "aka-titles";}
        
    @Override
    protected void beforeInserts(Connection conn) throws SQLException {
	Statement stmt = null;
	try{
	    stmt = conn.createStatement();
	    stmt.executeUpdate(CREATE_AKA_TITLE_TABLE);
	}finally{
	    stmt.close();
	}
    }
    
    @Override
    protected void beforeInsert(Connection conn, Map<String, String> columnNameValueMap) throws SQLException {}
    
    @Override
    protected void setValue(NamedParameterStatement namedStmt, String columnKey, String valueString) throws SQLException {
	if (columnKey.equals(XML_COLUMNS.akaTitle.name())||
		columnKey.equals(Movies.COLUMNS_FROM_XML.name.name())||
		columnKey.equals(Movies.COLUMNS_FROM_XML.inyear.name())){
	    namedStmt.setString(columnKey, valueString);
	} else if (columnKey.equals(Movies.COLUMNS_FROM_XML.year.name())) {
	    namedStmt.setInt(columnKey, Integer.valueOf(valueString));
	}
    }
    
    @Override
    protected String getInsertStatement(Map<String, String> columnNameValueMap) {
	boolean inyearIsNull = !columnNameValueMap.containsKey(Movies.COLUMNS_FROM_XML.inyear.name());
	// FIXME: this convention should be code reused (NULL is stored as empty string so that equality comparisons can be made between movies without inyears)
	return "INSERT INTO akatitles (movies_id, aka) SELECT movies.id, :akaTitle FROM movies WHERE (name = :name AND year = :year AND inyear = " + ( inyearIsNull ? "''" : ":inyear" ) + ");";
    }
}
