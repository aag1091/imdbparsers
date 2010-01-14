package imdb.parsers.listtoxml;

import java.io.BufferedReader;
import java.util.Map;



public abstract class OneLinePerRecordParser extends Parser {
    
    public OneLinePerRecordParser(String filePath) {
	super(filePath);
    }

    public OneLinePerRecordParser() {
	super();
    }

    protected Map<String, String> parseNextRecord(BufferedReader in) {
	try {
	    String line = in.readLine();
	    if (line == null || isEndOfData(line)) return null; // end of file or end of data
	    return parseLine(line);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }
    
    protected abstract Map<String, String> parseLine(String line);
}
