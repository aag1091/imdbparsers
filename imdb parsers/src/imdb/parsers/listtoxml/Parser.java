package imdb.parsers.listtoxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


public abstract class Parser {
    
    private static XMLOutputFactory xof = XMLOutputFactory.newInstance();
    
    private File inFile;
    private BufferedReader in;
    protected long inLineNumber;
    private File outFile;
    private XMLStreamWriter out;
    private long recordsAdded;
    
    private Map<String, String> nextRecord;
    
    public Parser() {
	inFile = new File(getListFilenameWithoutExtension() + getListExtension());
	outFile = new File(getListFilenameWithoutExtension() + getXMLExtension());
    }
    
    
    public Parser(String filePath) {
	inFile = new File(filePath, getListFilenameWithoutExtension() + getListExtension());
	outFile = new File(filePath, getListFilenameWithoutExtension() + getXMLExtension());
    }
    
    public void parse() {
	ListToXML.LOG.fine("Parsing: " + getListFilenameWithoutExtension());
	getNewReader();
	getNewWriter();
	// prepare progress reporting
	prepareProgressReporting();
	// get first record
	nextRecord = subclassGetNextRecord();
	//
	try {
	    // explicitly state UTF-8 encoding
	    out.writeStartDocument("UTF-8", "1.0");
	    out.writeStartElement("records");
	    while (hasMoreRecords()) {
		Map<String, String> record = getNextRecord();
		out.writeStartElement("record");
		ListToXML.LOG.log(Level.FINEST, "record");
		for (String key : record.keySet()) {
		    writeElement(out, key, record.get(key));
		    ListToXML.LOG.log(Level.FINEST, key+"="+record.get(key));
		}
		out.writeEndElement(); // </record>
		recordsAdded++;
		printProgressReport();
	    }
	    out.writeEndElement(); // </records>
	    out.writeEndDocument();
	    ListToXML.LOG.fine("Finished Parsing: " + getListFilenameWithoutExtension());
	} catch (XMLStreamException e) {
	    throw new RuntimeException(e);
	} finally {
	    try {
		in.close();
		out.close();
	    } catch (Exception e) {}
	}
    }
    
    /*
     * Record retrieval
     */
    protected boolean hasMoreRecords() {
	return nextRecord != null;
    }
    
    private Map<String, String> getNextRecord() {
	if (!hasMoreRecords()) throw new IllegalStateException("hasMoreRecords() is false");
	Map<String, String> toReturn = nextRecord;
	nextRecord = subclassGetNextRecord();
	return toReturn;
    }

    private Map<String, String> subclassGetNextRecord() {
	try {
	    Map<String, String> parsed = null;
	    // get record, skipping any lines as per subclass
	    while (parsed == null) {
		String line = in.readLine();
		if (line == null || isEndOfData(line)) return null; // no more
		parsed = parseLine(line);
		inLineNumber++;
	    }
	    if (parsed == null) return null; // no more
	    return parsed;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }
    
    /*
     * Reader & Writer
     */
    private void getNewReader() {
	inLineNumber = 0;
	try {
	    if (in != null) in.close();
	    in = new BufferedReader(new FileReader(inFile));
	    String line;
	    while ((line = in.readLine()) != null) {
		if (isStart(line)) {
		    for (int i = 0; i < getNumberOfLinesToSkipBeforeStart(); i++) {
			in.readLine();
			inLineNumber++;
		    }
		    break;
		}
	    }
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }
    
    private void getNewWriter() {
	try {
	    if (out != null) out.close();
	    out = xof.createXMLStreamWriter(new FileOutputStream(outFile), "UTF-8"); // explicit encoding
	} catch (IOException e) {
	    throw new RuntimeException(e);
	} catch (XMLStreamException e) {
	    throw new RuntimeException(e);
	}
    }
    
    private long numberOfLinesInFile;
    private long timeAtStart;
    private long timeAtLastReport;
    
    private void prepareProgressReporting() {
	if(nextRecord != null) throw new IllegalStateException("prepareProgressReporting() must be called before starting parsing of actual data, because it uses the same reader");
	recordsAdded = 0;
	numberOfLinesInFile = 0;
	timeAtStart = System.currentTimeMillis();
	timeAtLastReport = timeAtStart;
	try {
	    String line;
	    while ((line = in.readLine()) != null){
		if(isEndOfData(line)) break;
		numberOfLinesInFile++;
	    }
		
		
	} catch (IOException e) {
	    ListToXML.LOG.log(Level.WARNING, "Could not count number of lines in file in preperation for progress reporting", e);
	}
	getNewReader();
    }
    
    
    private void printProgressReport() {
	if (inLineNumber > numberOfLinesInFile) return;
	//
	long timeSinceLastReport = System.currentTimeMillis() - timeAtLastReport;
	if (timeSinceLastReport < 10000) return;
	timeAtLastReport = System.currentTimeMillis();
	//
	String str = getListFilenameWithoutExtension()+getListExtension() + " [";
	int cols = 20;
	float percent = (float) inLineNumber / (float) numberOfLinesInFile;
	int colsFull = (int) (percent * cols);
	for (int i = 0; i < cols; i++) {
	    str += i <= colsFull ? "=" : " ";
	}
	float percentRounded = (((float) ((int) (percent * 10000))) / 100);
	str += "] \t" + percentRounded + "% \t" + inLineNumber + " / " + numberOfLinesInFile +" \t records added: "+recordsAdded + "\t est. total: " + recordsAdded * (100 / percentRounded);
	System.out.println(str);
    }
    
    /*
     * Methods expected to be overloaded sometimes
     */
    protected String getListExtension() {
	return ".list";
    }
    
    protected String getXMLExtension() {
	return ".xml";
    }
    
    protected int numberOfLinesPerParseGroup() {
	return 1;
    }
    
    
    /*
     * Abstract methods
     */
    protected abstract String getListFilenameWithoutExtension();
    
    protected abstract int getNumberOfLinesToSkipBeforeStart();
    
    protected abstract boolean isEndOfData(String line);
    
    protected abstract boolean isStart(String line);
    
    /**
     * return null if you need more lines to create a record
     * @param index 
     */
    protected abstract Map<String, String> parseLine(String line);
    
    /*
     * Helper methods
     */
    public static void writeElement(XMLStreamWriter out, String localName, String value) throws XMLStreamException {
	out.writeStartElement(localName);
	out.writeCharacters(value);
	out.writeEndElement();
    }
}
