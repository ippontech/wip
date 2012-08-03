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

package fr.ippon.wip.transformers.handler;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * An abstract decorator for decorating SAX handlers
 * 
 * @author Yohan Legat
 *
 */
public abstract class HandlerDecorator implements ContentHandler {

	private ContentHandler decorated;
	
	public HandlerDecorator(ContentHandler decorated) {
		this.decorated = decorated;
	}
	
	public void setDocumentLocator(Locator locator) {
		decorated.setDocumentLocator(locator);
	}

	public void startDocument() throws SAXException {
		decorated.startDocument();
	}

	public void endDocument() throws SAXException {
		decorated.endDocument();
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		decorated.startPrefixMapping(prefix, uri);
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		decorated.endPrefixMapping(prefix);
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		decorated.startElement(uri, localName, qName, atts);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		decorated.endElement(uri, localName, qName);
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		decorated.characters(ch, start, length);
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		decorated.ignorableWhitespace(ch, start, length);
	}

	public void processingInstruction(String target, String data) throws SAXException {
		decorated.processingInstruction(target, data);
	}

	public void skippedEntity(String name) throws SAXException {
		decorated.skippedEntity(name);
	}
}
