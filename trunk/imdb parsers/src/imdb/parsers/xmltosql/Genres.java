package imdb.parsers.xmltosql;

import java.sql.Connection;


public class Genres extends Parser{

    public Genres(Connection conn) {
	super(conn);
    }

    public Genres(String filePath, Connection conn) {
	super(filePath, conn);
    }

    protected String getTableName() {return "genres";}
    protected String getXMLFilenameWithoutExtension() {return "genres";}
    protected String getCreateTableStatement(){
	return "CREATE TABLE IF NOT EXISTS genres (name char(255) NOT NULL, genre char(100) NOT NULL);";
    }

}
