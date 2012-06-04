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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.portlet.PortletSession;

import fr.ippon.wip.cache.CacheManager;
import fr.ippon.wip.cache.CacheManagerImpl;
import fr.ippon.wip.cookies.CookiesManager;
import fr.ippon.wip.cookies.CookiesManagerImpl;
import fr.ippon.wip.portlet.WIPortlet;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 * This class defines useful method for http management like doing
 * a http request, getting HttpMethod attributes.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class HttpManagerImpl implements HttpManager {

    private static final Logger LOG = Logger.getLogger(HttpManagerImpl.class.getName());

    private static HttpManager instance = null;
	private AbstractHttpClient httpClient;
	private PoolingClientConnectionManager connectionManager;
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
        try {
            SSLSocketFactory ssf = new SSLSocketFactory (new TrustSelfSignedStrategy(), new AllowAllHostnameVerifier());
            Scheme httpsScheme = new Scheme("https", 443, ssf);
            PlainSocketFactory psf = new PlainSocketFactory ();
            Scheme httpScheme = new Scheme("http", 80, psf);
            SchemeRegistry registry = new SchemeRegistry ();
            registry.register(httpsScheme);
            registry.register(httpScheme);
            connectionManager = new PoolingClientConnectionManager(registry);
            connectionManager.setDefaultMaxPerRoute(10);
            connectionManager.setMaxTotal(100);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize HTTPS", e);
        }
		httpClient = new DefaultHttpClient(connectionManager);
        httpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
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

        // Get HttpContext from portlet session
        PortletSession session = wipRequest.getRequest().getPortletSession();
        HttpContext context  = (HttpContext)session.getAttribute("wip.http.context");
        if (context == null) {
            context = new BasicHttpContext();
            session.setAttribute("wip.http.context", context);
        }

        if (wipRequest.getRequest() != null) {
			processAuthentication(session);
		}


        WIPResponse wipResponse = cacheManager.getCacheEntry(id, wipRequest, instance);
		if (wipResponse != null && wipResponse.isFresh(id, wipRequest))
			return wipResponse;

        LOG.fine ("Processing WIP request: " + wipRequest.getUrl());
		
		if (wipRequest.getMethodType().equals("POST"))
			wipResponse = doPostRequest(id, wipRequest, wipResponse, instance, context);
		else
			wipResponse = doGetRequest(id, wipRequest, wipResponse, instance, context);
		
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
     * @param context, maintains HttpClient execution context
	 * @return the WIPResponse object resulting from the request
	 * @throws IOException
	 */
	private WIPResponse doGetRequest(String id, WIPRequest wipRequest, WIPResponse wipResponse, String instance, HttpContext context) throws IOException {
		String url = setGetParams(wipRequest.getUrl().replaceAll("\\{", "%7B").replaceAll("\\}", "%7D").replaceAll(" ", "%20"), wipRequest.getParameters());
        HttpUriRequest request = new HttpGet(url);
		setRequestHeaders(wipRequest, wipResponse, request);
		cookiesManager.setCookies(id, url, request);
        HttpResponse response = null;
		try {
            response = httpClient.execute(request, context);
            LOG.warning("Send: " + request.getURI().toString());
		} finally {	}
		cookiesManager.saveCookies(id, response.getAllHeaders());
		return handleStatusCode(id, response, wipRequest, wipResponse, instance);
	}
	
	/**
	 * Create and execute a POST method corresponding to the given WIPRequest.
	 * @param id the string session id
	 * @param wipRequest the WIPRequest containing the targeted URL
	 * @param wipResponse if set, it may contain useful headers to send
     * @param context, maintains HttpClient execution context
	 * @return the WIPResponse object resulting from the request
	 * @throws IOException
	 */
	private WIPResponse doPostRequest(String id, WIPRequest wipRequest, WIPResponse wipResponse, String instance, HttpContext context) throws IOException {
        HttpPost postRequest = new HttpPost(wipRequest.getUrl().replaceAll("\\{", "%7B").replaceAll("\\}", "%7D").replaceAll(" ", "%20"));
		setPostParams(postRequest, wipRequest.getParameters());
		setRequestHeaders(wipRequest, wipResponse, postRequest);
		cookiesManager.setCookies(id, wipRequest.getUrl(), postRequest);
        HttpResponse response = null;
		try {
            response = httpClient.execute(postRequest, context);
            LOG.warning("Send: " + postRequest.getURI().toString());
		} finally {	}
		cookiesManager.saveCookies(id, response.getAllHeaders());
		return handleStatusCode(id, response, wipRequest, wipResponse, instance);
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
	 * @param postRequest the POST request on which parameters have to be set
	 * @param params the parameters map
	 * @return the POST method on which parameters have been set
	 */
	@SuppressWarnings("rawtypes")
	private void setPostParams(HttpPost postRequest, Map<String, String[]> params) {
		if (params != null) {
			Iterator it = params.entrySet().iterator();
			while (it.hasNext()) {
				Entry e = (Entry) it.next();
				if (!isWipParameter((String) e.getKey()))
                    postRequest.getParams().setParameter((String) e.getKey(), ((String[]) e.getValue())[0]);
			}
		}
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
	 * @param request the method on which request headers are added
	 */
	private void setRequestHeaders(WIPRequest wipRequest, WIPResponse wipResponse, HttpRequest request) {
		// Accept-Language
		if (wipRequest.getRequest() != null) {
			Locale locale = wipRequest.getRequest().getLocale();
			if (locale != null)
				request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, locale.toString());
			else
                request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en");
		} else
            request.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en");
		
		// Accept-Charset
        request.addHeader(HttpHeaders.ACCEPT_CHARSET, "ISO-8859-1,utf-8");
		
		// Caching headers
		if (wipResponse != null) {
			String etag = wipResponse.getHeader("Etag"); 
			if (etag != null)
                request.addHeader(HttpHeaders.IF_NONE_MATCH, etag);
			String lastModified = wipResponse.getHeader("Last-Modified");
			if (lastModified != null)
                request.addHeader(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
		}
	}
	
	/**
	 * Handle response status code.
	 * @param id the user id
	 * @param response the HTTP response previously executed
	 * @param wipResponse if set, the previously cached response
	 * @return the resulting WIP response 
	 * @throws IOException
	 */
	private WIPResponse handleStatusCode(String id, HttpResponse response, WIPRequest wipRequest, WIPResponse wipResponse, String instance) throws IOException {
		WIPResponse ret = new WIPResponse(response, instance);
		int statusCode = ret.getStatusCode();
		if (statusCode == StatusCode.NOT_MODIFIED) {
			// Return the previously cached response
			ret = wipResponse;
		} else if (StatusCode.isRedirectionCode(statusCode)) {
			// Processing redirection
			String location = ret.getHeader("Location");
			if (location != null) {
				ret = this.doRequest(id, new WIPRequest(wipRequest, location), instance);
            }
		} else if (statusCode == StatusCode.UNAUTHORIZED) {
			// An authentication is required
			Header[] authHeaders = ret.getHttpResponse().getHeaders("WWW-Authenticate");
            for (Header authHeader : authHeaders) {
                String authString = authHeader.getValue().toLowerCase();
			    if (authString.contains("ntlm")) {
                    ret.setAuthType("ntlm");
                    return ret;
                }
			    else if (authString.contains("basic")) {
                    ret.setAuthType("basic");
                    return ret;
                }
            }
			ret.setAuthType("unknown");
		}
		return ret;
	}
	
	/**
	 * Set credentials to the httpState object if required
	 * @param session The PortletSession containing authentication informations
	 */
	private void processAuthentication(PortletSession session) {
		// Checking if an authentication is required
		if (session.getAttribute("authType") != null) {
            // Getting login and password from session
            AuthScheme scheme = null;
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
            httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);
            session.removeAttribute("authType");
		} else {
            // Clear credentials
            httpClient.getCredentialsProvider().clear();
        }
	}

	/**
	 * Save a cookie in the cookies map.
	 * @param id the key in cookies map
	 * @param cookie the cookie to save
	 */
	public void saveSingleCookie(String id, String cookie) {
		cookiesManager.saveSingleCookie(id, cookie);
	}
	
	/**
	 * Propagate the add of cache entry.
	 * @param id the possibly given id (in the case of a private cache)
	 * @param wipRequest the key that will be used to retrieve the response
	 * @param wipResponse the response to cache
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
