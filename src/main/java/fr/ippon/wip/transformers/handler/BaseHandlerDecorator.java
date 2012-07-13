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
