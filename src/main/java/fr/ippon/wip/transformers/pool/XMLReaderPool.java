/*
 *	Copyright 2010,2011 Ippon Technologies 
 *  
 *	This file is part of Web Integration Portlet (WIP).
 *	Web Integration Portlet (WIP) is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Web Integration Portlet (WIP) is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Web Integration Portlet (WIP).  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ippon.wip.transformers.pool;

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
		super(poolSize);
	}

	@Override
	protected CloseableXmlReader buildResource() {
		try {
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
			
			return new CloseableXmlReader(this, parser);
			
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		}
	}
}
