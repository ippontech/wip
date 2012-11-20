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

package fr.ippon.wip.http;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.http.request.PostRequestBuilder;
import fr.ippon.wip.http.request.RequestBuilder;
import fr.ippon.wip.portlet.WIPortlet;
import fr.ippon.wip.util.WIPUtil;

import javax.portlet.BaseURL;
import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

/**
 * This class creates portal URL for corresponding to URL of the content
 * returned by a remote host.
 * 
 * The sole constructor takes a PortletRequest as parameter in order to create
 * portal URL If the PortletRequest is not an instance of MimeRequest, it is not
 * possible to create portal URL, so temporary URL will be generated for future
 * parsing (Response#computePortalURL)
 * 
 * @author François Prot
 * @author Yohan Legat
 */
public class UrlFactory {

	private static final String TEMP_URL_SEPARATOR = "&#128;";
	
	public static final String TEMP_URL_ENCODED_SEPARATOR = "&amp;#128;";
	
	private static final String[] TOKENS = { "<", "$" };
	
	private static final Logger LOG = Logger.getLogger(UrlFactory.class.getName());
	
	private final WIPConfiguration configuration;
	
	private String actualUrl;

	/**
	 * @param portletRequest
	 *            To get windowID and retrieve appropriate configuration
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 */
	public UrlFactory(PortletRequest portletRequest, String actualUrl) {
		this.actualUrl = actualUrl;
		configuration = WIPUtil.getConfiguration(portletRequest);
	}

	/**
	 * Create a portal URL from a temporary URL (response transformed in the
	 * ACTION phase)
	 * 
	 * @param tempUrl
	 * @param mimeResponse
	 *            To create portal URLs
	 * @return
	 * @throws MalformedURLException
	 */
	public String convertTempToPortalUrl(String tempUrl, MimeResponse mimeResponse) throws MalformedURLException {
		String[] tokens = tempUrl.split(TOKENS[0]);
		if (tokens.length >= 3) {
			return createProxyUrl(tokens[0], tokens[1], tokens[2], mimeResponse);
		}
		throw new IllegalArgumentException("tempUrl is not valid");
	}

	/**
	 * Create a proxy URL.
	 * 
	 * If portletResponse is an instance of MimeResponse, creates portal URL,
	 * else creates temporary URL.
	 * 
	 * @param relativeUrl
	 *            URL of the remote resource
	 * @param method
	 *            HTTP method for this request
	 * @param type
	 *            Type of resource
	 * @param portletResponse
	 *            To create portal URL if instance of MimeResponse
	 * @return
	 * @throws MalformedURLException
	 */
	public String createProxyUrl(String relativeUrl, String method, String type, PortletResponse portletResponse) {
		String proxyUrl;
		PostRequestBuilder.HttpMethod httpMethod = PostRequestBuilder.HttpMethod.valueOf(method);
		PostRequestBuilder.ResourceType resourceType = PostRequestBuilder.ResourceType.valueOf(type);

		// Convert to absolute URL
		String absoluteUrl;
		absoluteUrl = toAbsolute(relativeUrl);

		// Check if url match domains to proxy
		if (!configuration.isProxyURI(absoluteUrl)) {
			return absoluteUrl;
		}
		if (portletResponse instanceof MimeResponse) {
			// Create a portal URL
			BaseURL baseURL;
            boolean isAction = (resourceType == PostRequestBuilder.ResourceType.HTML);
            // TEST
            for (Map.Entry<String, RequestBuilder.ResourceType> entry : configuration.getJavascriptResourcesMap().entrySet()) {
                if (entry.getValue() == RequestBuilder.ResourceType.AJAX && absoluteUrl.contains(entry.getKey())) {
                    isAction = false;
                    break;
                }
            }
			if (isAction) {
				// Create an ActionURL
				baseURL = ((MimeResponse) portletResponse).createActionURL();
			} else {
				// Create a ResourceURL
				baseURL = ((MimeResponse) portletResponse).createResourceURL();
			}
			// Set common parameters
			baseURL.setParameter(WIPortlet.LINK_URL_KEY, absoluteUrl);
			baseURL.setParameter(WIPortlet.METHOD_TYPE, method);
			baseURL.setParameter(WIPortlet.RESOURCE_TYPE_KEY, type);
			// Get portlet URL as String
			proxyUrl = baseURL.toString();
			// Append concatenation key for AJAX URLs (hack !)
			if (resourceType == PostRequestBuilder.ResourceType.AJAX) {
				proxyUrl += "&" + WIPortlet.URL_CONCATENATION_KEY + "=";
			}
		} else {
			// Create a temp URL
			proxyUrl = TEMP_URL_SEPARATOR + absoluteUrl + TOKENS[0] + httpMethod.name() + TOKENS[0] + resourceType.name() + TEMP_URL_SEPARATOR;
		}

		return proxyUrl;
	}

	public String getActualUrl() {
		return actualUrl;
	}

	public void setActualUrl(String actualUrl) {
		this.actualUrl = actualUrl;
	}

	/**
	 * Transform an relative url to an absolute one.
	 * 
	 * @param relativeUrl
	 *            the relative url to transform
	 * @return the absolute url
	 * @throws URISyntaxException
	 */
	private String toAbsolute(String relativeUrl) {
		try {
			// wrong comportement of URI.resolve when relativeUrl is empty, so
			// we catch this case scenario
			if (relativeUrl.isEmpty())
				return actualUrl;

			URI relativeUri = URI.create(relativeUrl);
			if (relativeUri.isAbsolute())
				return relativeUrl;

			return URI.create(actualUrl).resolve(relativeUri).toString();

		} catch (IllegalArgumentException e) {
			LOG.log(Level.WARNING, "Illegal URI: " + relativeUrl);
			return relativeUrl;
		}
	}
}
