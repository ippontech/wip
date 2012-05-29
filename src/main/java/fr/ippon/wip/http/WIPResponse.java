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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.xml.sax.SAXException;

import fr.ippon.wip.cache.CacheManagerImpl;
import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;
import fr.ippon.wip.transformers.CSSTransformer;
import fr.ippon.wip.transformers.HTMLTransformer;
import fr.ippon.wip.transformers.JSONTransformer;
import fr.ippon.wip.transformers.JSTransformer;
import fr.ippon.wip.util.WIPUtil;

/**
 * The WIPResponse object contains useful information obtained as a 
 * result of the HTTP request: headers, status code, response body...
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class WIPResponse implements java.io.Serializable {

    private static final Logger LOG = Logger.getLogger(WIPResponse.class.getName());

    private static final long serialVersionUID = 1L;
	
	/**
	 * A reference to the WIP configuration.
	 */
	private WIPConfiguration wipConfig;
	
	/**
	 * The HTTP method previously created to do the request.
	 */
	private transient HttpResponse httpResponse;
	
	/**
	 * A map containing the response headers.
	 */
	private Map<String, String> headers;
	
	/**
	 * The returned status code.
	 */
	private int statusCode;
	
	/**
	 * The remote response body as a string.
	 */
	private String remoteResponse;

	/**
	 * The remote response body as a byte array.
	 */
	public byte[] remoteResponseAsBytes;
	
	/**
	 * A boolean indicating if the response has been transformed.
	 */
	private boolean transformedResponse;
	
	/**
	 * A boolean indicating if the response is private or public.
	 */
	private boolean privateResponse;
	
	/**
	 * The authentication type.
	 */
	private String authType;
	
	/**
	 * The creation date of the response
	 */
	private long date;
	
	/**
	 * Create a WIPResponse object from the given HTTP method (after execution)
	 * @param response the given HTTP response
	 * @throws IOException
	 */
	public WIPResponse(HttpResponse response, String instance) throws IOException {
		httpResponse = response;
		statusCode = response.getStatusLine().getStatusCode();
		remoteResponse = null;
		headers = setHeaders(response.getAllHeaders());
		transformedResponse = false;
		privateResponse = false;
		authType = "none";
		wipConfig = WIPConfigurationManager.getInstance().getConfiguration(instance);
		date = (new Date()).getTime();
	}
	
	/**
	 * Private method that converted to given array of headers into a map.
	 * @param headersList the response headers as an array 
	 * @return the response headers as a map
	 */
	private Map<String, String> setHeaders(Header[] headersList) {
		Map<String, String> m = new HashMap<String, String>();
		for (int i=0; i<headersList.length; i++)
			m.put(headersList[i].getName().toLowerCase(), headersList[i].getValue());
		return m;
	}

	/**
	 * Process HTML transformation.
	 * @param request the portlet request used to get the session
	 * @param response the portlet response used to create a portlet URL.
	 * @param currentUrl the current URL used as a reference to rewrite URLs
	 * @throws IOException
	 */
	public void transformHTML(PortletRequest request, PortletResponse response, String currentUrl) throws IOException {
		PortletSession session = request.getPortletSession();
		boolean authenticated = (session.getAttribute("authType")!=null) ? true : false;
		HTMLTransformer transformer = new HTMLTransformer(response, currentUrl, authenticated);
		try {
			remoteResponse = transformer.transform(getRemoteResponse());
			transformedResponse = true;
		} catch (Exception e) {
            LOG.log(Level.INFO, "Error transforming HTML content", e);
		}
	}
	
	/**
	 * Process CSS transformation.
	 * @param request the portlet request used to get the session
	 * @param response the portlet response used to create a portlet URL.
	 * @param currentUrl the current URL used as a reference to rewrite URLs
	 * @throws IOException
	 */
	public void transformCSS(PortletRequest request, PortletResponse response, String currentUrl) throws IOException {
		PortletSession session = request.getPortletSession();
		boolean authenticated = (session.getAttribute("authType")!=null) ? true : false;
		CSSTransformer transformer = new CSSTransformer(response, currentUrl, authenticated);
		try {
			remoteResponse = transformer.transform(getRemoteResponse());
			transformedResponse = true;
		} catch (SAXException e) {
            LOG.log(Level.INFO, "Error transforming CSS content", e);
		}
	}
	
	/**
	 * Process JS transformation.
	 * @param request the portlet request used to get the session
	 * @param response the portlet response used to create a portlet URL.
	 * @param url the current URL used as a reference to rewrite URLs
	 * @throws IOException
	 */
	public void transformJS(PortletRequest request, PortletResponse response, String url) throws IOException {
		PortletSession session = request.getPortletSession();
		boolean authenticated = (session.getAttribute("authType")!=null) ? true : false;
		JSTransformer transformer = new JSTransformer(response, url, authenticated);
		if (transformer.isDeletedScript(url)) {
			remoteResponse = "";
		}else if (!transformer.isIgnoredScript(url)) {
			try {
				remoteResponse = transformer.transform(getRemoteResponse());
				transformedResponse = true;
			} catch (SAXException e) {
                LOG.log(Level.INFO, "Error transforming JS content", e);
			}
		}
	}
	
	public void transformJSON() throws IOException {
		JSONTransformer transformer = new JSONTransformer();
		try {
			remoteResponse = transformer.transform(getRemoteResponse());
		} catch (SAXException e) {
            LOG.log(Level.INFO, "Error transforming JSON content", e);
		}
	}

	/**
	 * Get the remote response.
	 * @return the string remote response
	 * @throws IOException
	 */
	public String getRemoteResponse() throws IOException {
        if (remoteResponse == null) {
            remoteResponse = IOUtils.toString(httpResponse.getEntity().getContent());
        }
        return remoteResponse;
    }
	
	/**
	 * Get the response status code.
	 * @return the response status code 
	 */
	public int getStatusCode() {
		return statusCode;
	}
	
	/** 
	 * Get the header corresponding to the given key in the headers map.
	 * @param key the map key as a string
	 * @return the header corresponding to the given key
	 */
	public String getHeader(String key) {
		return headers.get(key.toLowerCase());
	}

	/**
	 * Get the HTTP method object.
	 * @return the HTTP method
	 */
	public HttpResponse getHttpResponse() {
		return httpResponse;
	}

	/**
	 * Get the content-type header.
	 * @return the string corresponding to the content type
	 */
	public String getContentType() {
		String contentType = getHeader("Content-Type");
		if (contentType != null) {
			String[] aux = contentType.split(";");
			if (aux.length > 0) contentType = aux[0];
		}
		return contentType;
	}
	
	/**
	 * Check if the response has been transformed.
	 * @return true if the response has been transformed, else false
	 */
	public boolean isTransformedResponse() {
		return transformedResponse;
	}
	
	/**
	 * Check if the response is private or public to decide the cache to use.
	 * @return true if the response is private, false if it is public
	 */
	public boolean isPrivateResponse() {
		return privateResponse;
	}
	
	/**
	 * Set the response as a private.
	 */
	public void setPrivateResponse() {
		privateResponse = true;
	}
	
	/**
	 * Get the authentication type (NTML | Basic | None | Unknown).
	 * @return the authentication type as a string 
	 */
	public String getAuthType() {
		return authType;
	}

	/**
	 * Set the authentication type (in case of a 401 status code)
	 * @param authType the authentication type.
	 */
	public void setAuthType(String authType) {
		this.authType = authType;
		if (!authType.equals("none"))
			headers.put("cache-control", "no-cache");
	}
	
	/**
	 * Check the freshness of the cached response to indicate if it 
	 * can be got from cache or not. It considers the cache configuration 
	 * and the response headers (Cache-Control, Expires...)
	 * @param id the user id who cached the response (if private)
	 * @param wipRequest the corresponding key in the cache
	 * @return true if the response is fresh, else false
	 */
	public boolean isFresh(String id, WIPRequest wipRequest) {
		boolean fresh = true;

		// Check if the cache configuration involves to force caching
		if (wipRequest.isResource()) {
			if (wipConfig.getForceResourceCaching()) {
				// check time
				long now = (new Date()).getTime();
				long timeout = wipConfig.getResourceCacheTimeout();
				if (timeout == -1 || date + timeout*1000 > now)
					return true;
				else 
					fresh = false;
			}
		} else {
			if (wipConfig.getForcePageCaching()) {
				// check time
				long now = (new Date()).getTime();
				long timeout = wipConfig.getPageCacheTimeout();
				if (timeout == -1 || date + timeout*1000 > now) 
					return true;
				else 
					fresh = false;
			}
		}

		if (fresh) {
			// Check Cache-Control header
			String cacheControl = this.getHeader("Cache-Control");
			
			if (cacheControl != null) {
				// Start checking directives: private, no-cache, no-store, must-revalidate
				// HTTP/1.1 specification - 14.9.1 : 
				// Private directive: response for a single user and must not be cached by a shared cache
				// No-cache, no-store, must-revalidate, proxy-revalidate: must not use the cached response
				String[] directives = cacheControl.split(",");
				for (int i=0; i<directives.length; i++) {
					if (!directives[i].contains("=")) {
						if (directives[i].equals("private"))
							privateResponse = true;
						if (directives[i].equals("no-cache") || directives[i].equals("no-store") || directives[i].equals("must-revalidate") || directives[i].equals("proxy-revalidate"))
							fresh = false;
					} 
				} 
				
				// If still considered as fresh, check directives: s-maxage, max-age (field-name private, field-name no-cache)
				// HTTP/1.1 specification - 14.9.3 :
				// The maximum age specified by s-maxage overrides the maximum age 
				// specified by either the max-age directive or the Expires header
				if (fresh) {
					for (int i=0; i<directives.length; i++) {
						if (directives[i].contains("=")) {
							String[] aux = directives[i].split("=");
							if (!privateResponse && aux[0].equals("s-maxage") && aux[1] != null) {
								long maxAge = Long.parseLong(aux[1])*100;
								long responseDate = WIPUtil.getDate(this.getHeader("Date")).getTime();
								long currentDate = (new Date()).getTime();
								if (currentDate - responseDate > maxAge) fresh = false;
							} else if (aux[0].equals("max-age") && aux[1] != null) {
								long maxAge = Long.parseLong(aux[1])*100;
								long responseDate = WIPUtil.getDate(this.getHeader("Date")).getTime();
								long currentDate = (new Date()).getTime();
								if (currentDate - responseDate > maxAge) fresh = false;
							} else if (aux[0].equals("private")) {
								privateResponse = true;
							} else if (aux[0].equals("no-cache")) {
								privateResponse = true;
							}
						}
					}
				}
			}
			
			// If still considered as fresh, check headers: expires, last-modified/date
			// HTTP/1.1 specification 14.21 & 14.29
			if (fresh) {
				String expires = this.getHeader("Expires");
				if (expires != null) {
					if (WIPUtil.getDate(expires).before(new Date()))
						fresh = false;
				} else {
					String lastModified = this.getHeader("Last-Modified");
					String responseDate = this.getHeader("Date");
					if (lastModified != null && responseDate != null) {
						long rate = wipConfig.getCacheDateRate();
						long lastModif = WIPUtil.getDate(lastModified).getTime();
						long respDate = WIPUtil.getDate(responseDate).getTime();
						long currentDate = (new Date()).getTime();
						if ((currentDate - respDate)*100 > (respDate - lastModif)*rate)
							fresh = false;
					}
				}
			}
		}
		// If the response is stale, remove the cache entry
		if (!fresh) 
			CacheManagerImpl.getInstance().removeCacheEntry(id, wipRequest, wipConfig.getInstance());
		
		return fresh;
	}

	
	public byte[] getBinaryContent() throws IOException {
		return IOUtils.toByteArray(httpResponse.getEntity().getContent());
	}
}
