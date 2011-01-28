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

package fr.ippon.wip.transformers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;

import javax.portlet.PortletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xml.serializer.ToHTMLStream;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;
import fr.ippon.wip.util.CachedDTD;

/**
 * A transformer to process the rewriting of HTML content. This transformer uses
 * a XSLT stylesheet to perfom its modifications.
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class HTMLTransformer implements WIPTransformer {
	
	/**
	 * The parser used to parse content from html to xhtml
	 */
	private static final String parserClassName = "org.cyberneko.html.parsers.SAXParser";

	/**
	 * A boolean to check wether the user is authenticated or not
	 */
	private boolean authenticated;

	/**
	 * The XSLT stylesheet that will be used by the transformer.
	 */
	private String xsltTransform;
	
	/**
	 * The current URL, used to resolve relative URLs
	 */
	private String currentUrl;
	
	/**
	 * The instance of the WIPConfiguration class
	 */
	private WIPConfiguration wipConfig;

	/**
	 * A portletResponse object sent to the rewriters  to create PortletURLs when needed
	 */
	private PortletResponse response;

	/**
	 * A constructor who will create a HTMLTransformer using the given fields
	 * @param response The response object used to build PortletURL when needed
	 * @param currentUrl The URL of the distant page currently displayed in the portlet
	 * @param authenticated A boolean to tell the stylesheet wheter the user is authenticated or not to the distant application
	 * @throws MalformedURLException
	 */
	public HTMLTransformer(PortletResponse response, String currentUrl, boolean authenticated) throws MalformedURLException {
		this.wipConfig = WIPConfigurationManager.getInstance().getConfiguration(response.getNamespace());
		this.currentUrl = currentUrl;
		this.xsltTransform = wipConfig.getXsltTransform();
		this.response = response;
		this.authenticated = authenticated;
	}

	/**
	 * This method will process the XSLT transformation of a given string
	 * containing HTML content.
	 * @param input The input string to rewrite
	 * @throws TransformerException 
	 */
	public String transform(String input) throws SAXException, IOException, TransformerException {
		// Processing clipping
		if (!wipConfig.getClippingType().equals("none")) {
			Clipper clipper = new Clipper(response);
			try {
				input = clipper.transform(input);
			} catch (SAXException e) {
				e.printStackTrace();
			}
		}
		
		// Parsing html content to xhtml
		input = htmlToXhtml(input);		 

		InputSource xhtml = new InputSource(new ByteArrayInputStream(input.getBytes()));
		ByteArrayOutputStream resultOutputStream = new ByteArrayOutputStream();
			
		// Parsing the xhtml
		DocumentBuilder db = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		}
		db.setEntityResolver(new CachedDTD());
		Document doc = db.parse(xhtml);
		
		// Initializing the transformer
		TransformerFactory tFactory = TransformerFactory.newInstance();
		StringReader sr = new StringReader(xsltTransform);
		Transformer transformer = tFactory.newTransformer(new StreamSource(sr));
		transformer.setErrorListener(new ParserErrorListener());
		
		// Sending parameters to the stylesheet
		transformer.setParameter("response", response);
		transformer.setParameter("wip_divClassName", wipConfig.getPortletDivId());
		transformer.setParameter("currentUrl", currentUrl);			
		transformer.setParameter("authenticated", authenticated);
		if (wipConfig.getEnableCssRetrieving())
			transformer.setParameter("retrieveCss", "true");
		else
			transformer.setParameter("retrieveCss", "false");
		if (wipConfig.getEnableUrlRewriting())
			transformer.setParameter("rewriteUrl", "true");
		else
			transformer.setParameter("rewriteUrl", "false");
		
		// Processing the transformation
		transformer.transform(new DOMSource(doc), new StreamResult(resultOutputStream));
		
		return resultOutputStream.toString();
	}

	
	/**
	 * A static method used by the HTMLTransformer and the Clipper to parse HTML into XHTML, to make the web content
	 * ready to be transformed by a XSLT stylesheet.
	 * @param input The html web content
	 * @return The web content normalized into xhtml
	 * @throws SAXException
	 * @throws IOException
	 */
	public static String htmlToXhtml(String input) throws SAXException, IOException {
		ByteArrayOutputStream xhtmlOutputStream = new ByteArrayOutputStream();
		XMLFilterImpl filter = new XMLFilterImpl();
		XMLReader parser = XMLReaderFactory.createXMLReader(parserClassName);
		configureParser(parser);
		filter.setParent(parser);
		ToHTMLStream writer = new ToHTMLStream();
		writer.setOutputStream(xhtmlOutputStream);
		filter.setContentHandler(writer.asContentHandler());
		InputSource inputSource = new InputSource(new ByteArrayInputStream(input.toString().getBytes()));
		filter.parse(inputSource);
		String xhtml = xhtmlOutputStream.toString();
		String doctype = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";
		xhtml = doctype + "\n" + xhtml;
		return xhtml;
	}

	/**
	 * This method will configure the cyberneko parser properly.
	 * @param parser The cyberneko parser to configure
	 */
	public static void configureParser(XMLReader parser) throws SAXNotRecognizedException, SAXNotSupportedException {
		parser.setFeature("http://cyberneko.org/html/features/balance-tags", true);
		parser.setFeature("http://cyberneko.org/html/features/override-namespaces", true);
		parser.setFeature("http://cyberneko.org/html/features/insert-namespaces", true);
		parser.setProperty("http://cyberneko.org/html/properties/default-encoding","UTF-8");
	}

}
