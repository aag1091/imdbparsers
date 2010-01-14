package imdb.parsers.listtoxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


public abstract class Parser {
    
    private static final Logger     LOG = Logger.getLogger(Parser.class.getSimpleName());
    
    private static XMLOutputFactory xof = XMLOutputFactory.newInstance();
    
    private File		    inFile;
    private BufferedReader	    in;
    private File		    outFile;
    private XMLStreamWriter	    out;
    
    public Parser() {
	inFile = new File(getListFilenameWithoutExtension() + getListExtension());
	outFile = new File(getListFilenameWithoutExtension() + getXMLExtension());
	getReader();
	getWriter();
    }
    
    
    public Parser(String filePath) {
	inFile = new File(filePath, getListFilenameWithoutExtension() + getListExtension());
	outFile = new File(filePath, getListFilenameWithoutExtension() + getXMLExtension());
	getReader();
	getWriter();
    }
    
    public void parse() {
	LOG.fine("Parsing: " + getListFilenameWithoutExtension());
	try {
	    // explicitly state UTF-8 encoding
	    out.writeStartDocument("UTF-8", "1.0");
	    out.writeStartElement("records");
	    Map<String, String> record;
	    while ((record = getRecord()) != null) {
		out.writeStartElement("record");
		for (String key : record.keySet()) {
		    writeElement(out, key, record.get(key));
		}
		out.writeEndElement(); // </record>
	    }
	    out.writeEndElement(); // </records>
	    out.writeEndDocument();
	    LOG.fine("Finished Parsing: " + getListFilenameWithoutExtension());
	} catch (XMLStreamException e) {
	    throw new RuntimeException(e);
	} finally{
	    try{
		in.close();
		out.close();
	    }catch(Exception e){}
	}
    }
    

    
    /**
     * @return null if end of data or file
     */
    private Map<String, String> getRecord() {
	   if (in == null) throw new IllegalStateException("BufferedReader not set yet.");
	   return parseNextRecord(in);
    }
    
    


    /*
     * Reader & Writer
     */
    private void getReader() {
	try {
	    if (in != null) in.close();
	    in = new BufferedReader(new FileReader(inFile));
	    String line;
	    while ((line = in.readLine()) != null) {
		if (isStart(line)) {
		    for (int i = 0; i < getNumberOfLinesToSkipBeforeStart(); i++) {
			in.readLine();
		    }
		    break;
		}
	    }
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }
    
    private void getWriter() {
	try {
	    if (out != null) out.close();
	    out = xof.createXMLStreamWriter(new FileOutputStream(outFile), "UTF-8"); // explicit encoding
	} catch (IOException e) {
	    throw new RuntimeException(e);
	} catch (XMLStreamException e) {
	    throw new RuntimeException(e);
	}
    }
    
    
    /*
     * Methods expected to be overloaded sometimes
     */
    protected String getListExtension() {return ".list";}
    protected String getXMLExtension() {return ".xml";}
    protected int numberOfLinesPerParseGroup(){return 1;}
    

    /*
     * Abstract methods 
     */
    protected abstract String getListFilenameWithoutExtension();
    
    protected abstract int getNumberOfLinesToSkipBeforeStart();
    
    protected abstract boolean isEndOfData(String line);
    
    protected abstract boolean isStart(String line);
    
    protected abstract Map<String, String> parseNextRecord(BufferedReader in);
    
    
    /*
     * Helper methods
     */
    static void writeElement(XMLStreamWriter out, String localName, String value) throws XMLStreamException {
	out.writeStartElement(localName);
	out.writeCharacters(value);
	out.writeEndElement();
    }
}
