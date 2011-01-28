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
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.PortletSession;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import fr.ippon.wip.cache.CacheManager;
import fr.ippon.wip.cache.CacheManagerImpl;
import fr.ippon.wip.cookies.CookiesManager;
import fr.ippon.wip.cookies.CookiesManagerImpl;

/**
 * This class defines useful method for http management like doing
 * a http request, getting HttpMethod attributes.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class HttpManagerImpl implements HttpManager {

	private static HttpManager instance = null; 
	private HttpClient httpClient;
	private MultiThreadedHttpConnectionManager connectionManager;
	private CacheManager cacheManager;
	private CookiesManager cookiesManager;
	
	/**
	 * Get HttpManager singleton instance.
	 * @return instance
	 */
	public static synchronized HttpManager getInstance() {
		if (instance == null)
			instance = new HttpManagerImpl();
		return instance;
	}
	
	/**
	 * Initialize the connection manager and the httpClient object.
	 */
	private HttpManagerImpl() {
		connectionManager = new MultiThreadedHttpConnectionManager();
		httpClient = new HttpClient(connectionManager);
		HttpClientParams params = new HttpClientParams();
		params.setParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
		httpClient.setParams(params);
		cacheManager = CacheManagerImpl.getInstance();
		cookiesManager = CookiesManagerImpl.getInstance();
	}
	
	/**
	 * Do the request only if the response corresponding to the given id has not 
	 * been cached and that response is fresh. A WIPResponse object is returned.
	 * @param id the string session id corresponding to the user id
	 * @param wipRequest the WIPRequest containing the targeted URL
	 * @return the WIPResponse object resulting from the request
	 * @throws IOException
	 */
	public WIPResponse doRequest(String id, WIPRequest wipRequest, String instance) throws IOException {
		PortletSession session = null;
		if (wipRequest.getRequest() != null) {
			session = wipRequest.getRequest().getPortletSession();
			processAuthentication(session);
		}
		
		WIPResponse wipResponse = cacheManager.getCacheEntry(id, wipRequest, instance);
		if (wipResponse != null && wipResponse.isFresh(id, wipRequest))
			return wipResponse; 
			
		// TMP
		System.out.println("\t"+wipRequest.getUrl());
		//////
		
		if (wipRequest.getMethodType().equals("POST"))
			wipResponse = doPostRequest(id, wipRequest, wipResponse, instance);
		else
			wipResponse = doGetRequest(id, wipRequest, wipResponse, instance);
		
		if (session != null) {
			Object authType = wipRequest.getRequest().getPortletSession().getAttribute("authType");
			if (authType != null && !wipRequest.isResource())
				wipResponse.setPrivateResponse();
		}
		return wipResponse;
	}
	
	/**
	 * Create and execute the GET method corresponding to the given WIPRequest.
	 * @param id the string session id
	 * @param wipRequest the WIPRequest containing the targeted URL
	 * @param wipResponse if set, it may contain useful headers to send
	 * @return the WIPResponse object resulting from the request
	 * @throws IOException
	 */
	private WIPResponse doGetRequest(String id, WIPRequest wipRequest, WIPResponse wipResponse, String instance) throws IOException {
		String url = setGetParams(wipRequest.getUrl().replaceAll("\\{", "%7B").replaceAll("\\}", "%7D").replaceAll(" ", "%20"), wipRequest.getParameters());
		HttpMethod method = new GetMethod(url);
		setRequestHeaders(wipRequest, wipResponse, method);
		cookiesManager.setCookies(id, url, method);
		try {
			httpClient.executeMethod(method);
		} finally {	}
		cookiesManager.saveCookies(id, method.getResponseHeaders());
		return handleStatusCode(id, method, wipResponse, instance);
	}
	
	/**
	 * Create and execute a POST method corresponding to the given WIPRequest.
	 * @param id the string session id
	 * @param wipRequest the WIPRequest containing the targeted URL
	 * @param wipResponse if set, it may contain useful headers to send
	 * @return the WIPResponse object resulting from the request
	 * @throws IOException
	 */
	private WIPResponse doPostRequest(String id, WIPRequest wipRequest, WIPResponse wipResponse, String instance) throws IOException {
		HttpMethod method = new PostMethod(wipRequest.getUrl().replaceAll("\\{", "%7B").replaceAll("\\}", "%7D").replaceAll(" ", "%20"));
		method = setPostParams((PostMethod) method, wipRequest.getParameters());
		setRequestHeaders(wipRequest, wipResponse, method);
		cookiesManager.setCookies(id, wipRequest.getUrl(), method);
		try {
			httpClient.executeMethod(method);
		} finally {	}
		cookiesManager.saveCookies(id, method.getResponseHeaders());
		return handleStatusCode(id, method, wipResponse, instance);
	}
	
	/**
	 * Destroy the HttpManager.
	 */
	public void destroy() {
		connectionManager.shutdown();
		connectionManager = null;
		httpClient = null;
	}
	
	/**
	 * Set request parameters in case of a GET method.
	 * @param url the targeted URL
	 * @param params the parameters map 
	 * @return the URL on which parameters have been concatenated.
	 */
	@SuppressWarnings("rawtypes")
	private String setGetParams(String url, Map<String, String[]> params) {
		if (params != null) {
			if (!url.contains("?")) url += "?"; else url += "&";
			Iterator it = params.entrySet().iterator();
			while (it.hasNext()) {
				Entry e = (Entry) it.next();
				if (!isWipParameter((String) e.getKey()))
					url += (String) e.getKey() + "=" + ((String[]) e.getValue())[0].replaceAll(" ", "%20").replaceAll("\\{", "%7B$").replaceAll("\\}", "%7D") + "&";
			}
			url = url.substring(0, url.length()-1);
		} 
		return url;
	}
	
	/**
	 * Set request parameters in case of a POST method.
	 * @param method the POST method on which parameters have to be set
	 * @param params the parameters map
	 * @return the POST method on which parameters have been set
	 */
	@SuppressWarnings("rawtypes")
	private HttpMethod setPostParams(PostMethod method, Map<String, String[]> params) {
		if (params != null) {
			Iterator it = params.entrySet().iterator();
			while (it.hasNext()) {
				Entry e = (Entry) it.next();
				if (!isWipParameter((String) e.getKey()))
					method.setParameter((String) e.getKey(), ((String[]) e.getValue())[0]);
			}
		}
		return method;
	}

	/**
	 * Check if the given string correspond to a WIP parameter that should not be sent.
	 * @param paramName the parameter to check
	 * @return true if it matches a WIP parameter, else false
	 */
	private boolean isWipParameter(String paramName) {
		if (paramName.startsWith("WIP_"))
			return true;
		else 
			return false;
	}
	
	/**
	 * Set request headers thanks to the given WIP request, the possibly
	 * previously cached response that contain the information to send
	 * @param wipRequest the WIPRequest object 
	 * @param wipResponse if set, the previously cached wipResponse
	 * @param method the method on which request headers are added
	 */
	private void setRequestHeaders(WIPRequest wipRequest, WIPResponse wipResponse, HttpMethod method) {
		// Accept-Language
		if (wipRequest.getRequest() != null) {
			Locale locale = wipRequest.getRequest().getLocale();
			if (locale != null)
				method.addRequestHeader("Accept-Language", locale.toString());
			else
				method.addRequestHeader("Accept-Language", "en");
		} else
			method.addRequestHeader("Accept-Language", "en");
		
		// Accept-Charset
		method.addRequestHeader("Accept-Charset", "ISO-8859-1,utf-8");	
		
		// Caching headers
		if (wipResponse != null) {
			String etag = wipResponse.getHeader("Etag"); 
			if (etag != null)
				method.addRequestHeader("If-None-Match", etag);
			String lastModified = wipResponse.getHeader("Last-Modified");
			if (lastModified != null) 
				method.addRequestHeader("If-Modified-Since", lastModified);
		}
	}
	
	/**
	 * Handle response status code.
	 * @param id the user id
	 * @param method the HTTP method previously executed
	 * @param wipResponse if set, the previously cached response
	 * @return the resulting WIP response 
	 * @throws IOException
	 */
	private WIPResponse handleStatusCode(String id, HttpMethod method, WIPResponse wipResponse, String instance) throws IOException {
		WIPResponse ret = new WIPResponse(method, instance);
		int statusCode = ret.getStatusCode();
		if (statusCode == StatusCode.NOT_MODIFIED) {
			// Return the previously cached response
			ret = wipResponse;
		} else if (StatusCode.isRedirectionCode(statusCode)) {
			// Processing redirection
			String location = ret.getHeader("Location");
			if (location != null)
				ret = this.doRequest(id, new WIPRequest(location, null, false), instance);
		} else if (statusCode == StatusCode.UNAUTHORIZED) {
			// An authentication is required
			String tmpHeader = ret.getHeader("WWW-Authenticate"); 
			String header = (tmpHeader != null) ? tmpHeader.toLowerCase() : "";
			if (header.contains("basic")) ret.setAuthType("basic");
			else if (header.contains("ntlm")) ret.setAuthType("ntlm");
			else ret.setAuthType("unknown");
		}
		return ret;
	}
	
	/**
	 * Set credentials to the httpState object if required
	 * @param session The PortletSession containing authentication informations
	 */
	private void processAuthentication(PortletSession session) {
		// Clearing credentials
		httpClient.getState().clearCredentials();
		// Checking if an authentication is required
		if (session.getAttribute("authType") != null) {
			// Getting login and password from session
			String login = (String)session.getAttribute("userLogin");
			String pwd = (String)session.getAttribute("userPassword");
			Credentials creds = null;
			if (session.getAttribute("authType").equals("basic")) {
				// Creating basic credentials
				creds = new UsernamePasswordCredentials(login, pwd);
			} else if (session.getAttribute("authType").equals("ntlm")) {
				// Creating ntlm credentials
				creds = new NTCredentials( login, pwd, "", "" );
			}
			// Setting the credentials
			httpClient.getState().setCredentials(AuthScope.ANY, creds);
		}
	}

	/**
	 * Propagate the add of cache entry.
	 * @param id the possibly given id (in the case of a private cache)
	 * @param request the key that will be used to retrieve the response
	 * @param response the response to cache
	 */
	public void setCacheEntry(String id, WIPRequest wipRequest, WIPResponse wipResponse, String instance) {
		cacheManager.setCacheEntry(id, wipRequest, wipResponse, instance);
	}
	
	/**
	 * Remove all entries in the private cache corresponding to the given id.
	 * @param id the user id corresponding to the cache to remove
	 */
	public void cleanPrivateCache(String id) {
		cacheManager.cleanPrivateCache(id);
	}
	
	/**
	 * Remove all entries in public and private caches.
	 */
	public void cleanCache() {
		cacheManager.cleanPublicCache();
		cacheManager.cleanEntirePrivateCache();
	}
}
