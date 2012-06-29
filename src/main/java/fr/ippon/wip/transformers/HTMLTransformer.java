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
import fr.ippon.wip.util.WIPUtil;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
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

/**
 * A transformer to process the rewriting of HTML content. This transformer uses
 * a XSLT stylesheet to perform its modifications.
 *
 * @author François Prot
 */
public class HTMLTransformer extends AbstractTransformer {

    /**
     * The parser used to parse content from html to xhtml
     */
    private static final String parserClassName = "org.cyberneko.html.parsers.SAXParser";

    /**
     * The instance of the WIPConfiguration class
     */
    private final WIPConfiguration wipConfig;

    /**
     * A portletRequest object
     */
    private final PortletRequest request;

    /**
     * A portletResponse object sent to the rewriters  to create PortletURLs when needed
     */
    private final PortletResponse response;

    /**
     * A constructor who will create a HTMLTransformer using the given fields
     *
     * @param request  The request object
     * @param response The response object used to build PortletURL when needed
     * @throws MalformedURLException
     */
    public HTMLTransformer(PortletRequest request, PortletResponse response) throws MalformedURLException {
        super(request);
        this.wipConfig = WIPUtil.getConfiguration(request);
        this.request = request;
        this.response = response;
    }

    @Override
    public String transform(String input) throws SAXException, IOException, TransformerException {
    	super.transform(input);
    	
        //TODO: manage a pool of XMLReader objects
        // Create HTML-capable CyberNeko SAX parser
        XMLReader parser = XMLReaderFactory.createXMLReader(parserClassName);
        parser.setFeature("http://cyberneko.org/html/features/override-namespaces", true);
        parser.setFeature("http://cyberneko.org/html/features/insert-namespaces", true);
        parser.setProperty("http://cyberneko.org/html/properties/default-encoding", "UTF-8");
        parser.setProperty("http://cyberneko.org/html/properties/doctype/pubid", "-//W3C//DTD XHTML 1.0 Transitional//EN");
        parser.setProperty("http://cyberneko.org/html/properties/doctype/sysid", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
        parser.setFeature("http://cyberneko.org/html/features/insert-doctype", true);
        parser.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
        parser.setFeature("http://cyberneko.org/html/features/scanner/notify-builtin-refs", true);

        // Create XSL TransformerFactory
        SAXTransformerFactory transformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance();

        // Set URIResolver
        transformerFactory.setURIResolver(new ClippingURIResolver(wipConfig));

        //TODO: manage a map of javax.xml.transform.Templates (one per config) and create a Transformer instances from it
        String xsltTransform = wipConfig.getXsltTransform();
        StreamSource rewriteXslt = new StreamSource(new ByteArrayInputStream(xsltTransform.getBytes()));
        TransformerHandler transformerHandler = transformerFactory.newTransformerHandler(rewriteXslt);

        // Set parameters
        transformerHandler.getTransformer().setParameter("type", wipConfig.getClippingType());
        transformerHandler.getTransformer().setParameter("request", request);
        transformerHandler.getTransformer().setParameter("response", response);
        transformerHandler.getTransformer().setParameter("wip_divClassName", wipConfig.getPortletDivId());
        transformerHandler.getTransformer().setParameter("retrieveCss", wipConfig.isEnableCssRetrieving());
        transformerHandler.getTransformer().setParameter("rewriteUrl", wipConfig.isEnableUrlRewriting());

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
        parser.setContentHandler(transformerHandler);
        parser.setProperty("http://xml.org/sax/properties/lexical-handler", transformerHandler);
        parser.parse(inputSource);

        return resultWriter.toString();
    }
}
