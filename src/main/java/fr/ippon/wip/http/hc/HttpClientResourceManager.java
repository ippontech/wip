package fr.ippon.wip.http.hc;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;
import fr.ippon.wip.http.Request;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.cache.HttpCacheStorage;
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
import org.apache.http.impl.client.cache.BasicHttpCacheStorage;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 29/05/12
 * Time: 21:48
 * To change this template use File | Settings | File Templates.
 */
public class HttpClientResourceManager {

    private static HttpClientResourceManager instance;

    private Map<String, HttpClient> perUserClientMap;
    private Map<String, CookieStore> perUserCookieStoreMap;
    private Map<String, CredentialsProvider> perUserWindowCredentialProviderMap;
    private HttpClient rootClient;
    private PoolingClientConnectionManager connectionManager;

    private ThreadLocal<PortletRequest> currentPortletRequest;
    private ThreadLocal<PortletResponse> currentPortletResponse;
    private ThreadLocal<Request> currentRequest;

    public static final String USER_WINDOW_KEY_SEPARATOR = "?";

    public synchronized static HttpClientResourceManager getInstance () {
        if (instance == null) {
            instance = new HttpClientResourceManager ();
        }
        return instance;
    }

    private HttpClientResourceManager () {
        perUserClientMap = new HashMap<String,HttpClient>();
        perUserCookieStoreMap = new HashMap<String, CookieStore>();
        perUserWindowCredentialProviderMap = new HashMap<String, CredentialsProvider>();
        currentPortletRequest = new ThreadLocal<PortletRequest>();
        currentPortletResponse = new ThreadLocal<PortletResponse>();
        currentRequest = new ThreadLocal<Request>();

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
            throw new RuntimeException("Could not initialize connection manager", e);
        }

    }

    public synchronized HttpClient getHttpClient (PortletRequest request) {
        String userSessionId = request.getPortletSession().getId();
        HttpClient client = perUserClientMap.get(userSessionId);
        if (client == null) {
            WIPConfiguration config = WIPConfigurationManager.getInstance().getConfiguration(request.getWindowID());
            if (config.getPageCachePrivate()) {
                client = getRootClient();
            } else {
                CacheConfig cacheConfig = new CacheConfig();
                cacheConfig.setSharedCache(false);
                client = new CachingHttpClient(getRootClient(), cacheConfig);
            }
            perUserClientMap.put(userSessionId, client);
        }
        return client;
    }

    public CredentialsProvider getCredentialsProvider(PortletRequest request) {
        CredentialsProvider credentialsProvider = perUserWindowCredentialProviderMap.get(getUserWindowId (request));

        if (credentialsProvider == null) {
            credentialsProvider = new BasicCredentialsProvider();
            perUserWindowCredentialProviderMap.put(getUserWindowId(request), credentialsProvider);
        }

        return credentialsProvider;
    }

    public synchronized HttpContext initExecutionContext (PortletRequest portletRequest, PortletResponse portletResponse, Request request) {
        String userSessionWindowId = getUserWindowId (portletRequest);
        HttpContext context = new BasicHttpContext();
        CredentialsProvider credentialsProvider = perUserWindowCredentialProviderMap.get(userSessionWindowId);
        if (credentialsProvider == null) {
            credentialsProvider = new BasicCredentialsProvider();
            perUserWindowCredentialProviderMap.put(userSessionWindowId, credentialsProvider);
        }
        context.setAttribute(ClientContext.CREDS_PROVIDER, credentialsProvider);
        context.setAttribute(ClientContext.COOKIE_STORE, getCookieStore(portletRequest));

        currentPortletRequest.set(portletRequest);
        currentPortletResponse.set(portletResponse);
        currentRequest.set(request);

        return context;
    }

    public void releaseThreadResources() {
        currentPortletRequest.remove();
        currentPortletResponse.remove();
        currentRequest.remove();
    }

    public synchronized void releaseSessionResources(String sessionId) {
        perUserClientMap.remove(sessionId);
        perUserCookieStoreMap.remove(sessionId);
        Iterator<String> contextKeyIter = perUserWindowCredentialProviderMap.keySet().iterator();
        while (contextKeyIter.hasNext()) {
            String key = contextKeyIter.next();
            if (key.indexOf(sessionId + USER_WINDOW_KEY_SEPARATOR) == 0) {
                perUserWindowCredentialProviderMap.remove(key);
            }
        }
    }

    public static synchronized void releaseGlobalResources () {
        if (instance != null && instance.rootClient != null) {
            instance.rootClient.getConnectionManager().shutdown();
        }
        instance = null;
    }
    public PortletRequest getCurrentPortletRequest () {
        return currentPortletRequest.get();
    }

    public PortletResponse getCurrentPortletResponse () {
        return currentPortletResponse.get();
    }

    public Request getCurrentRequest () {
        return currentRequest.get();
    }

    private synchronized CookieStore getCookieStore (PortletRequest request) {
        String userSessionId = request.getPortletSession().getId();
        CookieStore store = perUserCookieStoreMap.get(userSessionId);
        if (store == null) {
            store = new BasicCookieStore();
            perUserCookieStoreMap.put(userSessionId, store);
        }
        return store;
    }

    private synchronized HttpClient getRootClient () {
        if (rootClient == null) {
            DefaultHttpClient defaultHttpClient = new DefaultHttpClient(connectionManager);

            CacheConfig cacheConfig = new CacheConfig();

            // TODO add Ehcache configuration
            //Ehcache ehCache = CacheManager.getInstance().addCacheIfAbsent("wip.shared.cached");
            //EhcacheHttpCacheStorage cacheStorage = new EhcacheHttpCacheStorage (ehCache);
            HttpCacheStorage cacheStorage = new BasicHttpCacheStorage(cacheConfig);

            HttpClient sharedCacheClient = new CachingHttpClient(defaultHttpClient, cacheStorage, cacheConfig);

            HttpClientDecorator decoratedClient = new HttpClientDecorator(sharedCacheClient);
            decoratedClient.addPreProcessor(new LtpaRequestInterceptor());
            decoratedClient.addPostProcessor(new TransformerResponseInterceptor());

            rootClient = decoratedClient;
        }
        return rootClient;
    }

    private String getUserWindowId(PortletRequest request) {
        return request.getPortletSession().getId() + USER_WINDOW_KEY_SEPARATOR + request.getWindowID();
    }
}
