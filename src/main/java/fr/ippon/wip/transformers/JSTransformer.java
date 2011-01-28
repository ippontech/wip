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

import java.io.IOException;
import java.util.Map;

import javax.portlet.PortletResponse;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;

import org.xml.sax.SAXException;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;
import fr.ippon.wip.rewriters.HTMLRewriter;
import fr.ippon.wip.rewriters.JSRewriter;

/**
 * JSTransformer implements the WIPTransformer interface that defines the
 * transform method used to rewrite the JavaScript code of the distant
 * application.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class JSTransformer implements WIPTransformer {

	/**
	 * The rewriter used to rewrite JS-specific parts of code
	 */
	private JSRewriter jsRewriter;
	
	/**
	 * The rewriter used to rewrite JS-specific parts of code
	 */
	private HTMLRewriter htmlRewriter;

	/**
	 * A PortletResponse used by the rewriter to create ResourceUrls
	 */
	private PortletResponse response;

	/**
	 * The instance of WIPConfiguration
	 */
	private WIPConfiguration wipConfig;

	/**
	 * A boolean to check wether the user is authenticated or not
	 */
	private boolean authenticated;

	/**
	 * Create a new JSTransformer by initializing the rewriter, getting the
	 * portlet configuration and initializing the other fields with given
	 * values.
	 * @param portletResponse the Portlet response used to create ResourceURLs
	 * @param currentUrl  The URL of the page of the distant application currently displayed, used to instanciate the JSRewriter
	 * @param authenticated  A boolean to check wether the user is authenticated or not
	 */
	public JSTransformer(PortletResponse portletResponse, String currentUrl, boolean authenticated) {
		super();
		this.jsRewriter = new JSRewriter(currentUrl, authenticated);
		this.htmlRewriter = new HTMLRewriter(currentUrl);
		this.response = portletResponse;
		this.wipConfig = WIPConfigurationManager.getInstance().getConfiguration(response.getNamespace());
		this.authenticated = authenticated;
	}

	/**
	 * Transform the given Javascript code: rewrite Ajax URLs and regular URLs
	 * defined in the portlet configuration.
	 * @param input the string corresponding to the original JavaScript code
	 * @return a string corresponding to the transformed JavaScript code
	 */
	public String transform(String input) throws SAXException, IOException {
		String url = null;
		
		// CUSTOM ------------------------------------------------------------------
		
		// Rewrite constants
		if (input.contains("window.location.protocol + \"//\" + window.location.host + ")) {
			int index = 0;
			while (index >= 0) {
				index = input.indexOf("window.location.protocol + \"//\" + window.location.host + ");
				if (index != -1) {
					input = input.substring(0, index) + input.substring(index + 57);
				}
			}
		}
		if (input.contains("Alfresco.constants.URL_CONTEXT = ")) {
			int index = input.indexOf("Alfresco.constants.URL_CONTEXT = ");
			if (index != -1) {
				input = input.substring(0, index) + "Alfresco.constants.URL_CONTEXT = \"" + jsRewriter.toAbsolute("/share/") + "\";" + input.substring(index + 43);
			}
		}

		// Rewrite control links
		if (input.contains("Alfresco.constants.PROXY_URI + recordData.contentUrl")) {
			int index = 0;
			while (index >= 0) {
				index = input.indexOf("Alfresco.constants.PROXY_URI + recordData.contentUrl");
				if (index != -1) {
					input = input.substring(0, index)
							+ "\"http://ged.ippon-technologies.net\" + "
							+ input.substring(index + 30);
				}
			}
		}
		
		int i = input.indexOf("document-details?");
		while (i > -1) {
			input = input.substring(0, i)+"/share/page/"+input.substring(i);
			i = input.indexOf("document-details?", i+30);
		}
		
		i = input.indexOf("edit-metadata?");
		if (i > -1)
			input = input.substring(0, i)+"/share/page/"+input.substring(i);
		
		i = input.indexOf("inline-edit?");
		if (i > -1)
			input = input.substring(0, i)+"/share/page/"+input.substring(i);
		
		i = input.indexOf("manage-permissions?");
		if (i > -1)
			input = input.substring(0, i)+"/share/page/"+input.substring(i);
		
		// TMP: modify authorizations
		if (input.contains("if (aTag.rel !== \"\")")) {
			int start = input.indexOf("if (aTag.rel !== \"\")");
			if (start > -1) {
				int end = input.indexOf("// Need the \"More >\" container?", start);
				if (end > -1)
					input = input.substring(0, start) + "}" + input.substring(end);
			}
		}

		if (input.contains("if (aTag.rel !== \"\")")) {
			int start = input.indexOf("if (aTag.rel !== \"\")");
			if (start > -1) {
				int end = input.indexOf("Dom.setStyle", start);
				if (end > -1)
					input = input.substring(0, start) + input.substring(end);
			}
		}
		//////
		
		//---------------------------------------------------------------------------

		ResourceURL rUrl = null;
		if (response instanceof RenderResponse)
			rUrl = ((RenderResponse)response).createResourceURL();
		else if (response instanceof ResourceResponse)
			rUrl = ((ResourceResponse)response).createResourceURL();
		
		Map<String, URLTypes> jsUrls = wipConfig.getJavascriptUrls();
		for(String jsUrl : jsUrls.keySet()) {
			url = jsUrl;
			switch(jsUrls.get(jsUrl)) {
				case AJAX : 
					input = input.replaceAll(url, jsRewriter.rewriteAjax(url, rUrl)); 
					break;
				case LINK : 
					input = input.replaceAll(url, htmlRewriter.rewriteLink(url, response));
					break;
				case FORM :
					input = input.replaceAll(url, htmlRewriter.rewriteForm(url, response, "POST")); 
					break;
				case REGULAR :  
					if (authenticated) 
						input = input.replaceAll(url, jsRewriter.rewriteResource(url, response, "other"));
					else 
						input = input.replaceAll(url, jsRewriter.rewriteUrl(url));
					break;
			}
		}
		
		// Rewriting URLs
		String regex = wipConfig.getJsRegex();
		input = jsRewriter.rewrite(regex, input, response);
		
		return input;
	}

	/**
	 * Check if the script from the given URL has to be rewritten
	 * @param url the script URL
	 * @return a boolean indicating if the script has to be rewritten
	 */
	public boolean isIgnoredScript(String url) {
		if (wipConfig.getScriptsToIgnore().contains(url))
			return true;
		else
			return false;
	}
	
}
