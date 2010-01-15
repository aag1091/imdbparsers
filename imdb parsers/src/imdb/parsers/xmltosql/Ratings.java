package imdb.parsers.xmltosql;

import java.sql.Connection;


public class Ratings  extends Parser{

    public Ratings(Connection conn) {
	super(conn);
    }

    public Ratings(String filePath, Connection conn) {
	super(filePath, conn);
    }

    protected String getTableName() {return "ratings";}
    protected String getXMLFilenameWithoutExtension() {return "ratings";}
    protected String getCreateTableStatement(){
	return "CREATE TABLE IF NOT EXISTS ratings (name char(255) NOT NULL UNIQUE, votes int, rating float);";
    }

}
