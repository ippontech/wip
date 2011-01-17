/*
 *	Copyright 2010,2011 Ippon Technologies 
 *  
 *	This file is part of Wip Portlet.
 *	Wip Portlet is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Wip Portlet is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Wip Portlet.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ippon.wip.transformers;

import java.io.IOException;

import javax.portlet.PortletResponse;

import org.xml.sax.SAXException;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;
import fr.ippon.wip.rewriters.CSSRewriter;

/**
 * CSSTransformer implements the WIPTransformer interface that defines the
 * transform method used to rewrite the CSS code of the distant application.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class CSSTransformer implements WIPTransformer {

	/**
	 * The WIPConfiguration instance
	 */
	private WIPConfiguration wipConfig;
	
	/**
	 * A CSSRewriter used to rewrite CSS-specific parts of code
	 */
	private CSSRewriter rewriter;
	
	/**
	 * An empty PortletResponse used to create ResourceURLs
	 */
	private PortletResponse response;
	
	/**
	 * Create a new CSSTransformer by getting the portlet configuration, 
	 * initializing the CSS rewriter and setting the PortletResponse.
	 * @param portletResponse The empty PortletResponse used to build ResourceURLs
	 * @param currentUrl The Url of the page of the distant application currently displayed in the portlet
	 * @param authenticated A boolean to tell the rewriter wether the user is authenticated or not
	 */
	public CSSTransformer(PortletResponse portletResponse, String currentUrl, boolean authenticated) {
		WIPConfigurationManager w = WIPConfigurationManager.getInstance();
		rewriter = new CSSRewriter(currentUrl, authenticated);
		response = portletResponse;
		wipConfig = w.getConfiguration(response.getNamespace());
	}
	
	/**
	 * Transform the given CSS code: add the prefix defined in portlet config
	 * before each selectors, rewrite URL thanks to the CSS regex defined in 
	 * config, remove absolute positionning and add custom CSS.
	 * @param input the string corresponding to the CSS code
	 * @return a string corresponding to the transformed CSS code 
	 */
	public String transform(String input) throws SAXException, IOException {
		if (wipConfig.getEnableCssRewriting()) {
			// Getting prefix
			String wip = "\n." + wipConfig.getPortletDivId() + " ";
		
			// Removing all \n
			input = input.replaceAll("\\r|\\n", "");
			input = input.replaceAll(System.getProperty("line.separator"), "");
			input = " "+input;
			
			 // Removing comments
			int idx1 = input.indexOf("/*", 0);
			int idx2 = input.indexOf("*/", 0);
			while (idx1 > -1) {
				input = input.substring(0, idx1) + input.substring(idx2+2);
				idx1 = input.indexOf("/*", 0);
				idx2 = input.indexOf("*/", 0);
			}
			
			// Parsing the file to add the wip prefix before selectors
			if (wipConfig.getAddPrefix()) {
				String aux = "", imported = "";
				String selector = null;

				int index = 0;
				int blocStart = input.indexOf("{", 0);
				int blocEnd = input.indexOf("}", 0);
				
				if (blocStart > -1) {
					do {
						selector = input.substring(index+1, blocStart);
						if (selector.startsWith("@import") || selector.startsWith("@CHARSET")) {
							// Get import and save it
							int i = input.indexOf(";", index);
							imported += input.substring(index+1, i+1);
							index = i;
						} else {
							if (selector.startsWith("@media")) {
								// Copy the entire bloc without modification
								blocEnd = input.indexOf("}}", index)+1;
								aux += "\n" + selector; 
								aux += input.substring(blocStart, blocEnd+1);
							} else {
								// Add prefix
								if (selector.indexOf(",") > -1) 
									selector = selector.replaceAll(",", "," + wip);
								aux += "\n" + wip + selector;
								aux += input.substring(blocStart, blocEnd+1);
							}
							// Next step
							index = blocEnd;
							blocStart = input.indexOf("{", index);
							blocEnd = input.indexOf("}", blocStart);
						}
					} while (blocStart > -1);
					
					input = imported + aux;
				}
			}
			
			// Rewriting URLs
			String regex = wipConfig.getCssRegex();
			input = rewriter.rewrite(regex, input, response);
	
			// Removing position: absolute;
			if (!wipConfig.getAbsolutePositioning())
				input = input.replaceAll("absolute", "relative");
		}
		return input;
	}
	
	/**
	 * Get custom CSS code in the portlet configuration
	 * @return the custom CSS code as a string
	 */
	public String getCustomCss() {
		return wipConfig.getCustomCss();
	}

}
