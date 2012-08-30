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

package fr.ippon.wip.http.hc;

import fr.ippon.wip.http.HttpExecutor;
import fr.ippon.wip.http.reponse.Response;
import fr.ippon.wip.http.request.RequestBuilder;
import fr.ippon.wip.state.PortletWindow;
import fr.ippon.wip.util.WIPLogging;
import fr.ippon.wip.util.WIPUtil;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is unique entry point for executing request with Apache
 * HttpComponents It is entirely stateless and thread-safe
 * 
 * @author François Prot
 */
public class HttpClientExecutor implements HttpExecutor {

	private static final Logger LOG = Logger.getLogger(HttpClientExecutor.class.getName());

	private static final Logger ACCESS_LOG = Logger.getLogger(HttpClientExecutor.class.getName() + ".AccessLog");

	private Response createResponse(HttpResponse httpResponse, byte[] responseBody, String url, boolean portalUrlComputed) throws IOException {
		// Create Response object from HttpResponse
		ContentType contentType = ContentType.getOrDefault(httpResponse.getEntity());
		Charset charset = contentType.getCharset();
		String mimeType = httpResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
		int statusCode = httpResponse.getStatusLine().getStatusCode();

		return new Response(responseBody, charset, mimeType, url, statusCode, portalUrlComputed);
	}

	/**
	 * This method must be executed on portlet undeploy to release all Apache
	 * HttpComponents resources.
	 */
	public void destroy() {
		HttpClientResourceManager.getInstance().releaseGlobalResources();
	}

	/**
	 * Send an HTTP request to the remote site and process the returned HTTP
	 * response This method:
	 * <ul>
	 * <li>creates an org.apache.http.HttpRequest</li>
	 * <li>executes it using instances of HttpClient and HttpContext provides by
	 * HttpClientResourceManager</li>
	 * <li>converts the resulting org.apache.http.HttpResponse to a
	 * fr.ippon.wip.http.Response</li>
	 * </ul>
	 * 
	 * @param request
	 *            Contains all the data needed to create an
	 *            org.apache.http.HttpRequest
	 * @param portletRequest
	 *            Gives access to javax.portlet.PortletSession and windowID
	 * @param portletResponse
	 *            Used to create PortletURL if instance of MimeResponse
	 * @return The returned Response instance can reference an InputStream
	 *         linked to a connection from an HttpClient pool It necessary to
	 *         call either Response#dispose, Response#printResponseContent or
	 *         Response#sendResponse to release the underlying HTTP connection.
	 * @throws IOException
	 */
	public Response execute(RequestBuilder request, PortletRequest portletRequest, PortletResponse portletResponse) throws IOException {
		if (WIPUtil.isDebugMode(portletRequest))
			WIPLogging.INSTANCE.logTransform(request.getRequestedURL());

		Response response = null;
		HttpResponse httpResponse = null;
		HttpEntity responseEntity = null;
		HttpClientResourceManager resourceManager = HttpClientResourceManager.getInstance();
		try {
			// Get Apache HttpComponents resources from ResourceManager
			HttpClient client = resourceManager.getHttpClient(portletRequest);
			HttpContext context = resourceManager.initExecutionContext(portletRequest, portletResponse, request);
			HttpUriRequest httpRequest;

			httpRequest = request.buildHttpRequest();

			// Execute the request
			try {
				httpResponse = client.execute(httpRequest, context);
				responseEntity = httpResponse.getEntity();

				// the HttpEntity content may be set as non repeatable, meaning it can be read only once
				byte[] responseBody = (responseEntity == null) ? null : EntityUtils.toByteArray(responseEntity);

				manageAuthentification(portletRequest, context, httpResponse);

				// what if the request was redirected? how to catch the last URL?
				String actualUrl = getActualUrl(portletRequest, context, request, httpResponse);

				// Create Response object from HttpResponse
				response = createResponse(httpResponse, responseBody, actualUrl, portletResponse instanceof MimeResponse);

				StringBuffer buffer = new StringBuffer();
				buffer.append(httpRequest.getMethod() + " " + request.getRequestedURL() + " " + httpResponse.getProtocolVersion() + "\" " + httpResponse.getStatusLine().getStatusCode());
				buffer.append("\n");
				for (Header header : httpResponse.getAllHeaders())
					buffer.append(header.getName() + " : " + header.getValue() + "\n");

				ACCESS_LOG.log(Level.INFO, buffer.toString());

				// logging if enabled
				if (WIPUtil.isDebugMode(portletRequest) && responseBody != null && !response.isBinary())
					WIPLogging.INSTANCE.logTransform(new String(responseBody) + "\n");

			} catch (RuntimeException rte) {
				throw rte;
			}

		} catch (URISyntaxException e) {
			LOG.log(Level.WARNING, "ERROR while creating URI", e);

		} finally {
			if (httpResponse != null && responseEntity != null)
				EntityUtils.consume(responseEntity);

			resourceManager.releaseThreadResources();
		}

		return response;
	}

	private String getActualUrl(PortletRequest portletRequest, HttpContext context, RequestBuilder request, HttpResponse response) {
		boolean cacheUsed = (context.getAttribute(CachingHttpClient.CACHE_RESPONSE_STATUS) == CacheResponseStatus.CACHE_HIT);
		boolean notFoundResponse = (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND);
		
		/*
		 * ExecutionContext.HTTP_REQUEST and ExecutionContext.HTTP_TARGET_HOST are not set when the cache is used
		 * or when the resource is ignored by WIP
		 */
		if (cacheUsed || notFoundResponse)
			return request.getRequestedURL();

		// Get final URL (ie. perhaps redirected)
		HttpUriRequest actualRequest = (HttpUriRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
		HttpHost actualHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
		return (actualRequest.getURI().isAbsolute()) ? actualRequest.getURI().toString() : (actualHost.toURI() + actualRequest.getURI());
	}

	/**
	 * This method updates the CredentialsProvider associated to the current
	 * PortletSession and the windowID with the provided login and password.
	 * Basic and NTLM authentication schemes are supported. This method uses the
	 * current fr.ippon.wip.state.PortletWindow to retrieve the authentication
	 * schemes requested by remote server.
	 * 
	 * @param login
	 * @param password
	 * @param portletRequest
	 *            Used to get current javax.portlet.PortletSession and windowID
	 */
	public void login(String login, String password, PortletRequest portletRequest) {
		HttpClientResourceManager resourceManager = HttpClientResourceManager.getInstance();
		CredentialsProvider credentialsProvider = resourceManager.getCredentialsProvider(portletRequest);
		PortletWindow portletWindow = PortletWindow.getInstance(portletRequest);
		List<String> schemes = portletWindow.getRequestedAuthSchemes();

		if (schemes.contains("Basic")) {
			// Creating basic credentials
			AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, "Basic");
			Credentials credentials = new UsernamePasswordCredentials(login, password);
			credentialsProvider.setCredentials(scope, credentials);
		}
		if (schemes.contains("NTLM")) {
			// Creating ntlm credentials
			AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, "NTLM");
			Credentials credentials = new NTCredentials(login, password, "", "");
			credentialsProvider.setCredentials(scope, credentials);
		}
	}

	/**
	 * This method clears all credentials from the CredentialsProvider
	 * associated to the current PortletSession and the windowID. The current
	 * fr.ippon.wip.state.PortletWindow is also deleted.
	 * 
	 * @param portletRequest
	 *            Used to get current javax.portlet.PortletSession and windowID
	 */
	public void logout(PortletRequest portletRequest) {
		HttpClientResourceManager resourceManager = HttpClientResourceManager.getInstance();
		CredentialsProvider credentialsProvider = resourceManager.getCredentialsProvider(portletRequest);

		// Clear credentials
		credentialsProvider.clear();

		// Clear state
		PortletWindow.clearInstance(portletRequest);
	}

	private void manageAuthentification(PortletRequest portletRequest, HttpContext context, HttpResponse httpResponse) {
		// Check if authentication is requested by remote host
		PortletWindow portletWindow = PortletWindow.getInstance(portletRequest);
		int statusCode = httpResponse.getStatusLine().getStatusCode();

		// TODO: process proxy auth requests HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED (407) ?
		// TODO: also needs a custom implementation of RoutePlanner to select proxy per-application ?
		List<String> schemes = null;
		if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
			// Check what authentication scheme are required
			schemes = new ArrayList<String>();
			
			for (Header authHeader : httpResponse.getHeaders(HttpHeaders.WWW_AUTHENTICATE)) {
				String headerValue = authHeader.getValue();
				schemes.add(headerValue.split(" ")[0]);
			}
		}

		portletWindow.setRequestedAuthSchemes(schemes);

		// Updates authentication state
		AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
		portletWindow.setAuthenticated(authState != null && authState.getCredentials() != null);
	}
}
