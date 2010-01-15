package imdb.parsers.xmltosql;

import java.sql.Connection;


public class Movies extends Parser{

    enum COLUMN_NAMES {name, year};
    
    public Movies(Connection conn) {
	super(conn);
    }

    public Movies(String filePath, Connection conn) {
	super(filePath, conn);
    }

    protected String getTableName() {return "movies";}
    protected String getXMLFilenameWithoutExtension() {return "movies";}
    protected String getCreateTableStatement(){
	return "CREATE TABLE IF NOT EXISTS movies (name char(255) NOT NULL UNIQUE, year int);";
    }

    //protected String getWrappedValue(String columnName) {
	//if(columnName.equals(COLUMN_NAMES.year.name())){
	    
	//}
   // }
    
}
