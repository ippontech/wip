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

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.http.request.IgnoreHttpRequestFilter;
import fr.ippon.wip.http.request.RequestBuilder;
import fr.ippon.wip.util.WIPUtil;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.client.cache.ehcache.EhcacheHttpCacheStorage;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * This class implements the Singleton pattern and manages all Apache
 * HttpComponents resources
 * 
 * @author François Prot
 */
public class HttpClientResourceManager {

	private static final HttpClientResourceManager instance = new HttpClientResourceManager();

	private static final Logger LOG = Logger.getLogger(HttpClientResourceManager.class.getName());

	public static HttpClientResourceManager getInstance() {
		return instance;
	}

	private double heuristicCacheRatio = 0.0;

	private final Map<String, HttpClient> perUserClientMap;

	private final Map<String, CookieStore> perUserCookieStoreMap;
	private final Map<String, CredentialsProvider> perUserWindowCredentialProviderMap;
	private final HttpClient rootClient;
	private final PoolingClientConnectionManager connectionManager;
	private final ThreadLocal<PortletRequest> currentPortletRequest;

	private final ThreadLocal<PortletResponse> currentPortletResponse;

	private final ThreadLocal<RequestBuilder> currentRequest;
	private static final String USER_WINDOW_KEY_SEPARATOR = "?";

	private int staleIfErrorTime = 0;

	private CacheManager cacheManager;

	private HttpClientResourceManager() {
		perUserClientMap = Collections.synchronizedMap(new HashMap<String, HttpClient>());
		perUserCookieStoreMap = Collections.synchronizedMap(new HashMap<String, CookieStore>());
		perUserWindowCredentialProviderMap = Collections.synchronizedMap(new HashMap<String, CredentialsProvider>());
		currentPortletRequest = new ThreadLocal<PortletRequest>();
		currentPortletResponse = new ThreadLocal<PortletResponse>();
		currentRequest = new ThreadLocal<RequestBuilder>();

		try {
			SSLSocketFactory ssf = new SSLSocketFactory(new TrustSelfSignedStrategy(), new AllowAllHostnameVerifier());
			Scheme httpsScheme = new Scheme("https", 443, ssf);
			PlainSocketFactory psf = new PlainSocketFactory();
			Scheme httpScheme = new Scheme("http", 80, psf);
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(httpsScheme);
			registry.register(httpScheme);
			connectionManager = new PoolingClientConnectionManager(registry);
			connectionManager.setDefaultMaxPerRoute(10);
			connectionManager.setMaxTotal(100);

			DefaultHttpClient defaultHttpClient = new DefaultHttpClient(connectionManager);

			// automatically redirects all HEAD, GET and POST requests
			defaultHttpClient.setRedirectStrategy(new LaxRedirectStrategy());

			CacheConfig cacheConfig = createAndConfigureCache();

			URL ehCacheConfig = getClass().getResource("/ehcache.xml");
			cacheManager = CacheManager.create(ehCacheConfig);
			Ehcache ehcache = cacheManager.getEhcache("public");
			EhcacheHttpCacheStorage httpCacheStorage = new EhcacheHttpCacheStorage(ehcache);

			CachingHttpClient sharedCacheClient = new CachingHttpClient(defaultHttpClient, httpCacheStorage, cacheConfig);
			HttpClientDecorator decoratedClient = new HttpClientDecorator(sharedCacheClient);

			decoratedClient.addPreProcessor(new LtpaRequestInterceptor());
			decoratedClient.addPreProcessor(new StaleIfErrorRequestInterceptor(staleIfErrorTime));
			decoratedClient.addFilter(new IgnoreHttpRequestFilter());

			decoratedClient.addPostProcessor(new TransformerResponseInterceptor());

			rootClient = decoratedClient;

		} catch (Exception e) {
			throw new RuntimeException("Could not initialize connection manager", e);
		}

	}

	private CookieStore getCookieStore(PortletRequest request) {
		String userSessionId = request.getPortletSession().getId();
		CookieStore store;
		synchronized (perUserCookieStoreMap) {
			store = perUserCookieStoreMap.get(userSessionId);
			if (store == null) {
				store = new BasicCookieStore();
				perUserCookieStoreMap.put(userSessionId, store);
			}
		}
		return store;
	}

	/**
	 * Retrieve or create a CredentialsProvider per sessionID/windowID
	 * 
	 * @param request
	 *            Gives access to javax.portlet.PortletSession and windowID
	 * @return
	 */
	public CredentialsProvider getCredentialsProvider(PortletRequest request) {
		CredentialsProvider credentialsProvider;
		synchronized (perUserWindowCredentialProviderMap) {
			credentialsProvider = perUserWindowCredentialProviderMap.get(getUserWindowId(request));

			if (credentialsProvider == null) {
				credentialsProvider = new BasicCredentialsProvider();
				perUserWindowCredentialProviderMap.put(getUserWindowId(request), credentialsProvider);
			}
		}
		return credentialsProvider;
	}

	/**
	 * Get the PortletRequest instance associated to the current thread
	 * 
	 * #initExecutionContext must have been called before
	 * 
	 * @return
	 */
	public PortletRequest getCurrentPortletRequest() {
		return currentPortletRequest.get();
	}

	/**
	 * Get the PortletResponse instance associated to the current thread
	 * 
	 * #initExecutionContext must have been called before
	 * 
	 * @return
	 */
	public PortletResponse getCurrentPortletResponse() {
		return currentPortletResponse.get();
	}

	/**
	 * Get the Request instance associated to the current thread
	 * 
	 * #initExecutionContext must have been called before
	 * 
	 * @return
	 */
	public RequestBuilder getCurrentRequest() {
		return currentRequest.get();
	}

	public double getHeuristicCacheRatio() {
		return heuristicCacheRatio;
	}

	/**
	 * Get or create an instance of org.apache.http.client.HttpClient. If
	 * private cache is disabled for this portlet windowID, the instance
	 * returned is always the same. If it is enabled, a specific instance is
	 * returned for each windowID/sessionID. The instance returned wraps
	 * multiple decorators on a unique
	 * org.apache.http.impl.client.DefaultHttpClient instance:
	 * <ol>
	 * <li>org.apache.http.impl.client.cache.CachingHttpClient for shared cache
	 * (unique instance)</li>
	 * <li>fr.ippon.wip.http.hc.HttpClientDecorator to inject specific
	 * pre-precessor and post-processor interceptors: LtpaRequestInterceptor and
	 * TransformerResponseInterceptor (unique instance)</li>
	 * <li>org.apache.http.impl.client.cache.CachingHttpClient for private cache
	 * (optional, one instance per windowID/sessionID</li>
	 * </ol>
	 * 
	 * @param request
	 *            Gives access to javax.portlet.PortletSession and windowID
	 * @return
	 */
	public HttpClient getHttpClient(PortletRequest request) {
		String userSessionId = request.getPortletSession().getId();
		HttpClient client;
		synchronized (perUserClientMap) {
			client = perUserClientMap.get(userSessionId);
			if (client == null) {
				WIPConfiguration config = WIPUtil.getConfiguration(request);
				if (!config.isPageCachePrivate()) {
					client = rootClient;
				} else {
					CacheConfig cacheConfig = createAndConfigureCache();
					
					URL ehCacheConfig = getClass().getResource("/ehcache.xml");
					cacheManager = CacheManager.create(ehCacheConfig);
					Ehcache ehcache = cacheManager.getEhcache("private");
					EhcacheHttpCacheStorage httpCacheStorage = new EhcacheHttpCacheStorage(ehcache);

					client = new CachingHttpClient(rootClient, httpCacheStorage, cacheConfig);
				}
				
				perUserClientMap.put(userSessionId, client);
			}
		}
		return client;
	}
	
	private CacheConfig createAndConfigureCache() {
		CacheConfig cacheConfig = new CacheConfig();
		cacheConfig.setSharedCache(false);
		cacheConfig.setHeuristicCachingEnabled(true);
		cacheConfig.setHeuristicCoefficient((float) heuristicCacheRatio);
		cacheConfig.setHeuristicDefaultLifetime(60);
		cacheConfig.setMaxObjectSize(4000000);
		
		return cacheConfig;
	}

	public HttpClient getRootClient() {
		return rootClient;
	}

	public int getStaleIfErrorTime() {
		return staleIfErrorTime;
	}

	private String getUserWindowId(PortletRequest request) {
		return request.getPortletSession().getId() + USER_WINDOW_KEY_SEPARATOR + request.getWindowID();
	}

	/**
	 * Create an HttpContext configured with a CredentialsProvider and a
	 * CookieStore according to the current sessionID/windowID.
	 * 
	 * Current PortletRequest, PortletResponse and Request instances are also
	 * associated to the current for future usage (@see
	 * TransformerResponseInterceptor and LtpaRequestInterceptor).
	 * 
	 * It is necessary to call #releaseThreadResources in a finally clause when
	 * HTTP processing is done.
	 * 
	 * @param portletRequest
	 * @param portletResponse
	 * @param request
	 * @return
	 */
	public HttpContext initExecutionContext(PortletRequest portletRequest, PortletResponse portletResponse, RequestBuilder request) {
		HttpContext context = new BasicHttpContext();
		CredentialsProvider credentialsProvider = getCredentialsProvider(portletRequest);
		context.setAttribute(ClientContext.CREDS_PROVIDER, credentialsProvider);
		context.setAttribute(ClientContext.COOKIE_STORE, getCookieStore(portletRequest));
		context.setAttribute("WIP_CONFIGURATION", WIPUtil.getConfiguration(portletRequest));

		currentPortletRequest.set(portletRequest);
		currentPortletResponse.set(portletResponse);
		currentRequest.set(request);

		return context;
	}

	/**
	 * Shutdown the connection manager on portlet un-deploy
	 */
	public void releaseGlobalResources() {
		rootClient.getConnectionManager().shutdown();
		cacheManager.shutdown();
	}

	/**
	 * Releases the HttpClient, CookieStore and CredentialsProvider instances
	 * associated to this session
	 * 
	 * @param sessionId
	 */
	public void releaseSessionResources(String sessionId) {
		perUserClientMap.remove(sessionId);
		perUserCookieStoreMap.remove(sessionId);

		Set<String> keySet = perUserWindowCredentialProviderMap.keySet();
		synchronized (perUserWindowCredentialProviderMap) {
			Iterator<String> contextKeyIter = keySet.iterator();
			while (contextKeyIter.hasNext()) {
				String key = contextKeyIter.next();

				if (key.indexOf(sessionId + USER_WINDOW_KEY_SEPARATOR) == 0) {
					contextKeyIter.remove();
				}
			}
		}
	}

	/**
	 * Releases the PortletRequest, PortletResponse and Request instances
	 * associated to the current thread
	 */
	public void releaseThreadResources() {
		currentPortletRequest.remove();
		currentPortletResponse.remove();
		currentRequest.remove();
	}

	public void setHeuristicCacheRation(double heuristicCacheRatio) {
		this.heuristicCacheRatio = heuristicCacheRatio;
	}

	public void setStaleIfErrorTime(int staleIfErrorTime) {
		this.staleIfErrorTime = staleIfErrorTime;
	}
}
