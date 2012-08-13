/*
 *	Copyright 2012 Ippon Technologies
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
package fr.ippon.wip.transformers;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.http.UrlFactory;
import fr.ippon.wip.transformers.handler.BaseHandlerDecorator;
import fr.ippon.wip.transformers.pool.CloseableXmlReader;
import fr.ippon.wip.util.WIPUtil;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Stopwatch;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A transformer to process the rewriting of HTML content. This transformer uses
 * a XSLT stylesheet to perform its modifications.
 * 
 * @author François Prot
 * @author Yohan Legat
 */
public class HTMLTransformer extends AbstractTransformer {

	private class TimestampedTemplates implements Templates {

		private final Templates templates;

		private final long timestamp;

		public TimestampedTemplates(Templates templates, long timestamp) {
			this.timestamp = timestamp;
			this.templates = templates;
		}

		public Properties getOutputProperties() {
			return templates.getOutputProperties();
		}

		public long getTimestamp() {
			return timestamp;
		}

		public Transformer newTransformer() throws TransformerConfigurationException {
			return templates.newTransformer();
		}
	}

	/**
	 * The instance of the WIPConfiguration class
	 */
	private final WIPConfiguration wipConfig;

	/**
	 * A portletRequest object
	 */
	private final PortletRequest request;

	/**
	 * A portletResponse object sent to the rewriters to create PortletURLs when
	 * needed
	 */
	private final PortletResponse response;

	private CloseableXmlReader parser;

	private static Map<String, TimestampedTemplates> templatesMap = new HashMap<String, TimestampedTemplates>();

	private static final ReentrantLock lock = new ReentrantLock();

	public static ThreadLocal<Long> timeProcess = new ThreadLocal<Long>();

	/**
	 * A constructor who will create a HTMLTransformer using the given fields
	 * 
	 * @param request
	 *            The request object
	 * @param response
	 *            The response object used to build PortletURL when needed
	 * @throws MalformedURLException
	 */
	public HTMLTransformer(PortletRequest request, PortletResponse response, String actualURL, CloseableXmlReader parser) {
		super(request, actualURL);
		this.wipConfig = WIPUtil.getConfiguration(request);
		this.request = request;
		this.response = response;
		this.parser = parser;
	}

	@Override
	public String transform(String input) throws SAXException, IOException, TransformerException {
		super.transform(input);

		Stopwatch stopwatch = new Stopwatch().start();
		try {
			// Create XSL TransformerFactory
			SAXTransformerFactory transformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance();

			// Set URIResolver
			transformerFactory.setURIResolver(new ClippingURIResolver(wipConfig));

			TimestampedTemplates template;
			TransformerHandler transformerHandler;
			try {
				lock.lock();
				template = templatesMap.get(wipConfig.getName());

				if (template == null || template.getTimestamp() < wipConfig.getTimestamp()) {
					String xsltTransform = wipConfig.getXsltTransform();
					StreamSource rewriteXslt = new StreamSource(new ByteArrayInputStream(xsltTransform.getBytes()));
					template = new TimestampedTemplates(transformerFactory.newTemplates(rewriteXslt), wipConfig.getTimestamp());
					templatesMap.put(wipConfig.getName(), template);
				}
			} finally {
				lock.unlock();
			}
			
			transformerHandler = transformerFactory.newTransformerHandler(template);

			// Set parameters
			transformerHandler.getTransformer().setParameter("type", wipConfig.getClippingType());
			transformerHandler.getTransformer().setParameter("request", request);
			transformerHandler.getTransformer().setParameter("response", response);
			transformerHandler.getTransformer().setParameter("actualUrl", actualUrl);
			transformerHandler.getTransformer().setParameter("wip_divClassName", wipConfig.getPortletDivId());
			transformerHandler.getTransformer().setParameter("retrieveCss", wipConfig.isEnableCssRetrieving());
			transformerHandler.getTransformer().setParameter("rewriteUrl", wipConfig.isEnableUrlRewriting());
			transformerHandler.getTransformer().setParameter("urlfact", new UrlFactory(request, actualUrl));

			// Set XPath expression for clipping
			if (wipConfig.getClippingType().equals("xpath")) {
				transformerHandler.getTransformer().setParameter("xpath", wipConfig.getXPath());
			}

			// Set ErrorListener
			transformerHandler.getTransformer().setErrorListener(new ParserErrorListener());

			// Create input source
			InputSource inputSource = new InputSource(new ByteArrayInputStream(input.getBytes()));

			// Execute transformation
			StringWriter resultWriter = new StringWriter();
			StreamResult streamResult = new StreamResult(resultWriter);
			transformerHandler.setResult(streamResult);

			ContentHandler decoratedHandler = new BaseHandlerDecorator(transformerHandler, urlFactory);
			parser.setContentHandler(decoratedHandler);
			parser.setProperty("http://xml.org/sax/properties/lexical-handler", transformerHandler);
			parser.parse(inputSource);

			timeProcess.set(stopwatch.elapsedMillis());

			return resultWriter.toString();

		} finally {
			parser.close();
		}
	}
}
