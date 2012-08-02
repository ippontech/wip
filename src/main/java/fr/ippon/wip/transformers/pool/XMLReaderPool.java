package fr.ippon.wip.transformers.pool;

import java.util.HashSet;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A CloseableXMLReader Pool. CloseableXMLReader is used because it provides an
 * easy way to make the resource return to the pool when calling its close
 * method.
 * 
 * @author Yohan Legat
 * 
 */
public class XMLReaderPool extends AbstractPool<CloseableXmlReader> {

	/**
	 * The parser used to parse content from html to xhtml
	 */
	private static final String parserClassName = "org.cyberneko.html.parsers.SAXParser";

	public XMLReaderPool(int poolSize) {
		super(new HashSet<CloseableXmlReader>());
		
		for (int i = 0; i < poolSize; i++) {
			try {
				pool.add(new CloseableXmlReader(this, buildXMLReader()));
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Instanciate and configure a XMLReader
	 * 
	 * @return an XMLReader
	 * @throws SAXException
	 */
	private XMLReader buildXMLReader() throws SAXException {
		XMLReader parser = XMLReaderFactory.createXMLReader(parserClassName);
		parser.setFeature("http://cyberneko.org/html/features/override-namespaces", true);
		parser.setFeature("http://cyberneko.org/html/features/insert-namespaces", true);
		parser.setFeature("http://cyberneko.org/html/features/scanner/ignore-specified-charset", true);
		parser.setProperty("http://cyberneko.org/html/properties/default-encoding", "UTF-8");
		parser.setProperty("http://cyberneko.org/html/properties/doctype/pubid", "-//W3C//DTD XHTML 1.0 Transitional//EN");
		parser.setProperty("http://cyberneko.org/html/properties/doctype/sysid", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
		parser.setFeature("http://cyberneko.org/html/features/insert-doctype", true);
		parser.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
		parser.setFeature("http://cyberneko.org/html/features/scanner/notify-builtin-refs", true);

		return parser;
	}
}
