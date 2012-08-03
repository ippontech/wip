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
import org.xml.sax.SAXException;

import fr.ippon.wip.http.UrlFactory;

/**
 * A SAX handler decorator providing a callback on the BASE htlm node, so we can
 * update the URL used to transform relative URLs into absolute ones.
 * 
 * @author Yohan Legat
 * 
 */
public class BaseHandlerDecorator extends HandlerDecorator {

	private UrlFactory urlFactory;

	public BaseHandlerDecorator(ContentHandler decorated, UrlFactory urlFactory) {
		super(decorated);
		this.urlFactory = urlFactory;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		super.startElement(uri, localName, qName, atts);

		if (!qName.equalsIgnoreCase("BASE"))
			return;

		String url = atts.getValue("href");
		urlFactory.setActualUrl(url);
	}
}
