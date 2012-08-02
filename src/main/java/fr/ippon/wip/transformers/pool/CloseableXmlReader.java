package fr.ippon.wip.transformers.pool;

import java.io.Closeable;
import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * An XMLReader proxy linked to a pool and providing a close method.
 * This resource returns into this pool when the close method is called.
 *  
 * @author Yohan Legat
 *
 */
public class CloseableXmlReader implements XMLReader, Closeable {

	// the linked pool
	private Pool<CloseableXmlReader> pool;
	
	// the XMLReader proxied
	private XMLReader xmlReader;
	
	public CloseableXmlReader(Pool<CloseableXmlReader> pool, XMLReader xmlReader) {
		this.pool = pool;
		this.xmlReader = xmlReader;
	}
	
	public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		return xmlReader.getFeature(name);
	}

	public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
		xmlReader.setFeature(name, value);
	}

	public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
		return xmlReader.getProperty(name);
	}

	public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
		xmlReader.setProperty(name, value);
	}

	public void setEntityResolver(EntityResolver resolver) {
		xmlReader.setEntityResolver(resolver);
	}

	public EntityResolver getEntityResolver() {
		return xmlReader.getEntityResolver();
	}

	public void setDTDHandler(DTDHandler handler) {
		xmlReader.setDTDHandler(handler);
	}

	public DTDHandler getDTDHandler() {
		return xmlReader.getDTDHandler();
	}

	public void setContentHandler(ContentHandler handler) {
		xmlReader.setContentHandler(handler);
	}

	public ContentHandler getContentHandler() {
		return xmlReader.getContentHandler();
	}

	public void setErrorHandler(ErrorHandler handler) {
		xmlReader.setErrorHandler(handler);
	}

	public ErrorHandler getErrorHandler() {
		return xmlReader.getErrorHandler();
	}

	public void parse(InputSource input) throws IOException, SAXException {
		xmlReader.parse(input);
	}

	public void parse(String systemId) throws IOException, SAXException {
		xmlReader.parse(systemId);
	}

	public void close() throws IOException {
		pool.leave(this);
	}

}
