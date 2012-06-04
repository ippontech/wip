package fr.ippon.wip.http;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;
import fr.ippon.wip.portlet.WIPortlet;
import fr.ippon.wip.state.PortletWindow;

import javax.portlet.*;

/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 04/06/12
 * Time: 09:34
 * To change this template use File | Settings | File Templates.
 */
public class UrlFactory {
    public static final String TEMP_URL_SEPARATOR = "&#128;";
    public static final String TEMP_URL_ENCODED_SEPARATOR = "&amp;#128;";
    private static final String[] TOKENS = {"<", "$"};

    private String currentUrl;
    private WIPConfiguration configuration;

    public UrlFactory (PortletRequest portletRequest) {
        configuration = WIPConfigurationManager.getInstance().getConfiguration(portletRequest.getWindowID());
        PortletWindow window = PortletWindow.getInstance(portletRequest);
        currentUrl = window.getCurrentURI();
    }

    public String createPortalUrl (String tempUrl, MimeResponse mimeResponse) {
        String[] tokens = tempUrl.split(TOKENS[0]);
        if (tokens.length >= 3) {
            return createProxyUrl (tokens[0], tokens[1], tokens[2], mimeResponse);
        }
        throw new IllegalArgumentException("tempUrl is not valid");
    }

    public static String createTempUrl (String requestedUrl, Request.HttpMethod httpMethod, Request.ResourceType resourceType) {
        String tempUrl = requestedUrl + TOKENS[0];
        tempUrl += httpMethod.name() + TOKENS[0];
        tempUrl += resourceType.name();

        return TEMP_URL_SEPARATOR + tempUrl + TEMP_URL_SEPARATOR;
    }

    public String createProxyUrl (String url, String method, String type, PortletResponse response) {
        String proxyUrl = null;
        Request.HttpMethod httpMethod = Request.HttpMethod.valueOf(method);
        Request.ResourceType resourceType = Request.ResourceType.valueOf(type);
        // Convert to absolute URL
        String absoluteUrl = toAbsolute(url);
        // Check if url match domains to proxy
        if (!configuration.isProxyURI(absoluteUrl)) {
            return absoluteUrl;
        }
        if (response instanceof MimeResponse) {
            // Create a portal URL
            BaseURL baseURL;
            if (resourceType == Request.ResourceType.HTML) {
                // Create an ActionURL
                baseURL = ((MimeResponse) response).createActionURL();
            } else {
                // Create a ResourceURL
                baseURL = ((MimeResponse) response).createResourceURL();
            }
            // Set common parameters
            baseURL.setParameter(WIPortlet.LINK_URL_KEY, absoluteUrl);
            baseURL.setParameter(WIPortlet.METHOD_TYPE, method);
            baseURL.setParameter(WIPortlet.RESOURCE_TYPE_KEY, type);
            // Get portlet URL as String
            proxyUrl = baseURL.toString();
            // Append concatenation key for AJAX URLs (hack !)
            if (resourceType == Request.ResourceType.AJAX) {
                proxyUrl += "&" +WIPortlet.URL_CONCATENATION_KEY +"=";
            }
        } else {
            // Create a temp URL
            proxyUrl = createTempUrl(absoluteUrl, httpMethod, resourceType);
        }
        return proxyUrl;
    }

    private String toAbsolute (String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        } else {
            if (url.startsWith("/")) {
                // Add protocol & host/port
                int firstSlashIndex = currentUrl.indexOf("/", "https://".length());
                if (firstSlashIndex < 0) {
                    firstSlashIndex = currentUrl.length();
                }
                return currentUrl.substring(0, firstSlashIndex) + url;
            } else {
                // Add base URL
                int lastSlashIndex = currentUrl.lastIndexOf("/", "https://".length());
                if (lastSlashIndex < 0) {
                    lastSlashIndex = currentUrl.length();
                }
                return currentUrl.substring(0, lastSlashIndex) + "/" + url;
            }
        }
    }
}

