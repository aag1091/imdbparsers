package imdb.parsers.xmltosql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;


public class Genres extends Parser {
    
    enum COLUMNS {genre};
    
    
    public static final String DROP_GENRE_NAMES_TABLE = "DROP TABLE genre_names;";
    public static final String DROP_GENRES_TABLE = "DROP TABLE genres;";
    public static final String CREATE_GENRE_NAMES_TABLE = "CREATE TABLE genre_names (id INT AUTO_INCREMENT PRIMARY KEY, name char(100) NOT NULL UNIQUE);";
    public static final String CREATE_GENRES_TABLE = "CREATE TABLE genres (movies_id INT, genre_names_id INT, FOREIGN KEY (movies_id) REFERENCES movies(id), FOREIGN KEY (genre_names_id) REFERENCES genre_names(id));";
    public static final String INSERT_GENRE_NAME = "INSERT INTO genre_names (name) VALUES (?);";
    
    public Genres(Connection conn) {super(conn);}
    public Genres(String filePath, Connection conn) {super(filePath, conn);}
    
    @Override
    protected String getTableName() {return "genres";}
    @Override
    protected String getXMLFilenameWithoutExtension() {return "genres";}
    
    @Override
    protected void beforeInserts(Connection conn) throws SQLException {
	// drop genre names table
	Statement stmt = null;
	try{
	    stmt = conn.createStatement();
	    stmt.executeUpdate(DROP_GENRE_NAMES_TABLE);
	}catch(SQLException e){ // don't care
	}finally{
	    stmt.close();
	}
	// drop genres table
	stmt = null;
	try{
	    stmt = conn.createStatement();
	    stmt.executeUpdate(DROP_GENRES_TABLE);
	}catch(SQLException e){ // don't care
	}finally{
	    stmt.close();
	}
	// create genre names table
	stmt = null;
	try{
	    stmt = conn.createStatement();
	    stmt.executeUpdate(CREATE_GENRE_NAMES_TABLE);
	}finally{
	    stmt.close();
	}
	// create genres table
	try{
	    stmt = conn.createStatement();
	    stmt.executeUpdate(CREATE_GENRES_TABLE);
	}finally{
	    stmt.close();
	}
    }
    
    @Override
    protected void beforeInsert(Connection conn, Map<String, String> columnNameValueMap) throws SQLException {
	// add genre name to genre_names if it's new
	PreparedStatement stmt = null;
	try {
	    stmt = conn.prepareStatement(INSERT_GENRE_NAME);
	    stmt.setString(1, columnNameValueMap.get(COLUMNS.genre.name()));
	    stmt.executeUpdate();
	} catch (SQLException e) {
	    if (!e.getMessage().startsWith("Duplicate")) throw e;
	}finally{
	    stmt.close();
	}
    }
    
    @Override
    protected void setValue(NamedParameterStatement namedStmt, String columnKey, String valueString) throws SQLException {
	if (columnKey.equals(Movies.COLUMNS_FROM_XML.name.name()) ||
	    columnKey.equals(Movies.COLUMNS_FROM_XML.inyear.name()) ||
	    columnKey.equals(COLUMNS.genre.name())) {
	    namedStmt.setString(columnKey, valueString);
	} else if (columnKey.equals(Movies.COLUMNS_FROM_XML.year.name())) {
	    namedStmt.setInt(columnKey, Integer.valueOf(valueString));
	}
    }
    
    @Override
    protected String getInsertStatement(Map<String, String> columnNameValueMap) {
	boolean inyearIsNull = !columnNameValueMap.containsKey(Movies.COLUMNS_FROM_XML.inyear.name());
	return "INSERT INTO genres (movies_id, genre_names_id) SELECT movies.id, genre_names.id FROM movies, genre_names WHERE (movies.name = :name AND movies.year = :year AND movies.inyear " + (inyearIsNull?"IS NULL":"= :inyear") + " AND genre_names.name = :genre);";
    }
}
