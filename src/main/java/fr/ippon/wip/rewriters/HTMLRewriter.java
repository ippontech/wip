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

package fr.ippon.wip.rewriters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.MimeResponse;
import javax.portlet.PortletResponse;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceResponse;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;
import fr.ippon.wip.portlet.WIPortlet;

/**
 * A rewriter used to rewrite html content
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 * 
 */
public class HTMLRewriter extends WIPRewriter {

	/**
	 * This constructor only calls the super class's constructor
	 * @param currentUrl The URL of the page of the distant application currently displayed in the portlet
	 */
	public HTMLRewriter(String currentUrl) {
		super(currentUrl);
	}

	/**
	 * This method is used to rewrite the href attribute of a link into an
	 * ActionURL
	 * @param link  The original value of the link
	 * @param response An empty PortletReponse used to create ActionURLs
	 * @return An ActionURL in its String representation, or the link in its
	 * 		absolute form if no rewriting should be performed, as specified
	 *      in the configuration
	 */
	public String rewriteLink(String link, PortletResponse response) {
		// CUSTOM --------------------------------------------------------------------------
		if (link.contains("{") && !link.contains("downloadUrl") && !link.contains("viewUrl"))
			return link;
		// ---------------------------------------------------------------------------------
		
		WIPConfiguration wipConfig = WIPConfigurationManager.getInstance().getConfiguration(response.getNamespace());
		
		boolean toRewrite = true;
		try {
			@SuppressWarnings("unused")
			URL testLink = new URL(link);
			// Link is an absolute URL
			toRewrite = false;
			for (URL u : wipConfig.getDomainsToProxy()) {
				if (link.startsWith(u.toExternalForm().toString())) {
					// Link should be rewrited only if its domain is in the configuration list
					toRewrite = true;
				}
			}
		} catch (MalformedURLException e) {
			// Link is a relative URL
		}
		if (wipConfig.getEnableUrlRewriting()) {
			if (toRewrite) {
				// Creating an ActionURL and setting parameters
				PortletURL pUrl = ((MimeResponse) response).createActionURL();
				// Setting parameters
				Map<String, String[]> parameters = new HashMap<String, String[]>();
				String[] tab1 = {"GET"};
				String[] tab2 = {toAbsolute(link)};
				parameters.put(WIPortlet.METHOD_TYPE, tab1);
				parameters.put(WIPortlet.LINK_URL_KEY, tab2);
				pUrl.setParameters(parameters);
				return pUrl.toString();
			} else {
				// Return a String to tell the XSLT stylesheet that this link should not be rewrited
				return "external";
			}
		} else {
			// Simply return the absolute form of the link
			return toAbsolute(link);
		}
	}

	/**
	 * This method is used to rewrite the action attribute of a form
	 * @param link The original value of the attribute
	 * @param response An empty PortletResponse used to build ActionURLs
	 * @param method A String to specify if the form's method is GET or POST
	 * @return An ActionURL in its String representation, containing the required 
	 * 		parameters to execute the form from the processAction method of the 
	 * 		WIPortlet class
	 */
	public String rewriteForm(String link, PortletResponse response,
			String method) {
		PortletURL pUrl;
		if (response instanceof RenderResponse) 
			pUrl = ((RenderResponse) response).createActionURL();
		else
			pUrl = ((ResourceResponse) response).createActionURL();
		// Setting parameters
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		String[] tab1 = {method};
		String[] tab2 = {toAbsolute(link)};
		parameters.put(WIPortlet.METHOD_TYPE, tab1);
		parameters.put(WIPortlet.LINK_URL_KEY, tab2);
		pUrl.setParameters(parameters);
		return pUrl.toString();
	}

}
