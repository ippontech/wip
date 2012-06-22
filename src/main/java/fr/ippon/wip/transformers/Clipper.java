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

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.util.CachedDTD;
import fr.ippon.wip.util.WIPUtil;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.portlet.PortletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A class used to process a clipping thanks to a XSLT transformation
 * This class implements the WIPTransformer interface
 *
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class Clipper implements WIPTransformer {

    private static final Logger LOG = Logger.getLogger(Clipper.class.getName());

    /**
     * The XSLT stylesheet used to process the clipping
     */
    private final String xsltClipping;

    /**
     * The instance of the WIPConfiguration
     */
    private final WIPConfiguration wipConfig;

    /**
     * A basic constructor, getting the WIPConfiguration instance and the stylesheet
     */
    public Clipper(PortletRequest request) {
    	wipConfig = WIPUtil.extractConfiguration(request);
        xsltClipping = wipConfig.getXsltClipping();
    }

    /**
     * This method will be called before the HTMLTransformer's one when a clipping is set,
     * processing the transformation of the given input keeping only the wanted parts of the content
     *
     * @param input The input String corresponding to the whole content of the distant page
     * @return Only the content specified in the configuration
     * @throws TransformerException
     */
    public String transform(String input) throws SAXException, IOException, TransformerException {
    	LOG.log(Level.INFO, "Processing clipping.");
        // Parsing the content into XHTML
        input = HTMLTransformer.htmlToXhtml(input);

        // Performing the XSLT transformation
        InputSource xhtml = new InputSource(new ByteArrayInputStream(input.getBytes()));

        // Initilazing the transformer
        TransformerFactory tFactory = TransformerFactory.newInstance();
        StringReader sr = new StringReader(xsltClipping);
        Transformer transformer = tFactory.newTransformer(new StreamSource(sr));

        DocumentBuilder db = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not create XML document builder", e);
            return input;
        }
        db.setEntityResolver(new CachedDTD());
        Document doc = db.parse(xhtml);
        transformer.setErrorListener(new ParserErrorListener());

        // Setting parameters used in the stylesheet
        if (wipConfig.getClippingType().equals("xpath")) {
            transformer.setParameter("xpath", wipConfig.getXPath());
        }
        transformer.setParameter("type", wipConfig.getClippingType());

        // Processing the transformation
        ByteArrayOutputStream resultOutputStream = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(resultOutputStream));

        return resultOutputStream.toString();
    }

}
