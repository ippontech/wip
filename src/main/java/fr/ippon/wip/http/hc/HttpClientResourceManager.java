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
import fr.ippon.wip.http.Request;
import fr.ippon.wip.util.WIPUtil;

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
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.util.*;

/**
 * This class implements the Singleton pattern and manages all Apache HttpComponents resources
 *
 * @author François Prot
 */
class HttpClientResourceManager {

    private static final HttpClientResourceManager instance = new HttpClientResourceManager();

    private final Map<String, HttpClient> perUserClientMap;
    private final Map<String, CookieStore> perUserCookieStoreMap;
    private final Map<String, CredentialsProvider> perUserWindowCredentialProviderMap;
    private final HttpClient rootClient;
    private final PoolingClientConnectionManager connectionManager;

    private final ThreadLocal<PortletRequest> currentPortletRequest;
    private final ThreadLocal<PortletResponse> currentPortletResponse;
    private final ThreadLocal<Request> currentRequest;

    private static final String USER_WINDOW_KEY_SEPARATOR = "?";

    public static HttpClientResourceManager getInstance() {
        return instance;
    }

    private HttpClientResourceManager() {
        perUserClientMap = Collections.synchronizedMap(new HashMap<String, HttpClient>());
        perUserCookieStoreMap = Collections.synchronizedMap(new HashMap<String, CookieStore>());
        perUserWindowCredentialProviderMap = Collections.synchronizedMap(new HashMap<String, CredentialsProvider>());
        currentPortletRequest = new ThreadLocal<PortletRequest>();
        currentPortletResponse = new ThreadLocal<PortletResponse>();
        currentRequest = new ThreadLocal<Request>();

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

            // TODO add Ehcache configuration
            //Ehcache ehCache = CacheManager.getInstance().addCacheIfAbsent("wip.shared.cached");
            //EhcacheHttpCacheStorage cacheStorage = new EhcacheHttpCacheStorage (ehCache);
            HttpClient sharedCacheClient = new CachingHttpClient(defaultHttpClient);
            HttpClientDecorator decoratedClient = new HttpClientDecorator(sharedCacheClient);
            decoratedClient.addPreProcessor(new LtpaRequestInterceptor());
            decoratedClient.addPostProcessor(new TransformerResponseInterceptor());

            rootClient = decoratedClient;
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize connection manager", e);
        }

    }

    /**
     * Get or create an instance of org.apache.http.client.HttpClient.
     * If private cache is disabled for this portlet windowID, the instance returned is always the same.
     * If it is enabled, a specific instance is returned for each windowID/sessionID.
     * The instance returned wraps multiple decorators on a unique org.apache.http.impl.client.DefaultHttpClient instance:
     * <ol>
     *     <li>org.apache.http.impl.client.cache.CachingHttpClient for shared cache (unique instance)</li>
     *     <li>fr.ippon.wip.http.hc.HttpClientDecorator to inject specific pre-precessor and post-processor
     *     interceptors: LtpaRequestInterceptor and TransformerResponseInterceptor (unique instance)</li>
     *     <li>org.apache.http.impl.client.cache.CachingHttpClient for private cache (optional, one instance per
     *     windowID/sessionID</li>
     * </ol>
     * @param request Gives access to javax.portlet.PortletSession and windowID
     * @return
     */
    public HttpClient getHttpClient(PortletRequest request) {
        String userSessionId = request.getPortletSession().getId();
        HttpClient client;
        synchronized (perUserClientMap) {
            client = perUserClientMap.get(userSessionId);
            if (client == null) {
                WIPConfiguration config = WIPUtil.extractConfiguration(request);
                if (config.isPageCachePrivate()) {
                    client = rootClient;
                } else {
                	CacheConfig cacheConfig = new CacheConfig();
                	cacheConfig.setSharedCache(false);
                    client = new CachingHttpClient(rootClient, cacheConfig);
                }
                perUserClientMap.put(userSessionId, client);
            }
        }
        return client;
    }

    /**
     * Retrieve or create a CredentialsProvider per sessionID/windowID
     * @param request Gives access to javax.portlet.PortletSession and windowID
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
     * Create an HttpContext configured with a CredentialsProvider and a CookieStore according to the current
     * sessionID/windowID.
     *
     * Current PortletRequest, PortletResponse and Request instances are also associated to
     * the current for future usage (@see TransformerResponseInterceptor and LtpaRequestInterceptor).
     *
     * It is necessary to call #releaseThreadResources in a finally clause when HTTP processing is done.
     *
     * @param portletRequest
     * @param portletResponse
     * @param request
     * @return
     */
    public HttpContext initExecutionContext(PortletRequest portletRequest, PortletResponse portletResponse, Request request) {
        HttpContext context = new BasicHttpContext();
        CredentialsProvider credentialsProvider = getCredentialsProvider(portletRequest);
        context.setAttribute(ClientContext.CREDS_PROVIDER, credentialsProvider);
        context.setAttribute(ClientContext.COOKIE_STORE, getCookieStore(portletRequest));

        currentPortletRequest.set(portletRequest);
        currentPortletResponse.set(portletResponse);
        currentRequest.set(request);

        return context;
    }

    /**
     * Releases the PortletRequest, PortletResponse and Request instances associated to the current thread
     */
    public void releaseThreadResources() {
        currentPortletRequest.remove();
        currentPortletResponse.remove();
        currentRequest.remove();
    }

    /**
     * Releases the HttpClient, CookieStore and CredentialsProvider instances associated to this session
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
                    perUserWindowCredentialProviderMap.remove(key);
                }
            }
        }
    }

    /**
     * Shutdown the connection manager on portlet un-deploy
     */
    public void releaseGlobalResources() {
        rootClient.getConnectionManager().shutdown();
    }

    /**
     * Get the PortletRequest instance associated to the current thread
     *
     * #initExecutionContext must have been called before
     * @return
     */
    public PortletRequest getCurrentPortletRequest() {
        return currentPortletRequest.get();
    }

    /**
     * Get the PortletResponse instance associated to the current thread
     *
     * #initExecutionContext must have been called before
     * @return
     */
    public PortletResponse getCurrentPortletResponse() {
        return currentPortletResponse.get();
    }

    /**
     * Get the Request instance associated to the current thread
     *
     * #initExecutionContext must have been called before
     * @return
     */
    public Request getCurrentRequest() {
        return currentRequest.get();
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

    private String getUserWindowId(PortletRequest request) {
        return request.getPortletSession().getId() + USER_WINDOW_KEY_SEPARATOR + request.getWindowID();
    }
}
