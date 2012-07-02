package fr.ippon.wip.http;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.portlet.WIPortlet;
import fr.ippon.wip.transformers.HTMLTransformer;
import fr.ippon.wip.util.WIPUtil;

import javax.portlet.BaseURL;
import javax.portlet.MimeResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * This class creates portal URL for corresponding to URL of the content returned by a remote host.
 *
 * The sole constructor takes a PortletRequest as parameter in order to create portal URL
 * If the PortletRequest is not an instance of MimeRequest, it is not possible to create portal URL, so temporary
 * URL will be generated for future parsing (Response#computePortalURL)
 *
 * @author François Prot
 */
public class UrlFactory {

    private static final String TEMP_URL_SEPARATOR = "&#128;";
    public static final String TEMP_URL_ENCODED_SEPARATOR = "&amp;#128;";
    private static final String[] TOKENS = {"<", "$"};
    private static final Logger LOG = Logger.getLogger(HTMLTransformer.class.getName());
    private final WIPConfiguration configuration;
    private URL actualUrl;

    /**
     * @param portletRequest To get windowID and retrieve appropriate configuration
     * @throws MalformedURLException 
     */
    public UrlFactory(PortletRequest portletRequest, URL actualUrl) throws MalformedURLException  {
    	this.actualUrl = actualUrl;
        configuration = WIPUtil.getConfiguration(portletRequest);
    }

    /**
     * Create a portal URL from a temporary URL (response transformed in the ACTION phase)
     *
     * @param tempUrl
     * @param mimeResponse To create portal URLs
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
     * @param relativeUrl URL of the remote resource
     * @param method HTTP method for this request
     * @param type Type of resource
     * @param portletResponse To create portal URL if instance of MimeResponse
     * @return
     * @throws MalformedURLException 
     */
    public String createProxyUrl(String relativeUrl, String method, String type, PortletResponse portletResponse) {
        String proxyUrl;
        Request.HttpMethod httpMethod = Request.HttpMethod.valueOf(method);
        Request.ResourceType resourceType = Request.ResourceType.valueOf(type);
        // Convert to absolute URL
        String absoluteUrl = toAbsolute(relativeUrl);

        // Check if url match domains to proxy
        if (!configuration.isProxyURI(absoluteUrl)) {
            return absoluteUrl;
        }
        if (portletResponse instanceof MimeResponse) {
            // Create a portal URL
            BaseURL baseURL;
            if (resourceType == Request.ResourceType.HTML) {
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
            if (resourceType == Request.ResourceType.AJAX) {
                proxyUrl += "&" + WIPortlet.URL_CONCATENATION_KEY + "=";
            }
        } else {
            // Create a temp URL
            proxyUrl = TEMP_URL_SEPARATOR + absoluteUrl + TOKENS[0] + httpMethod.name() + TOKENS[0] + resourceType.name() + TEMP_URL_SEPARATOR;
        }
        
        return proxyUrl;
    }

    /**
     * Transform an relative url to an absolute one.
     * @param relativeUrl the relative url to transform
     * @return the absolute url
     */
    private String toAbsolute(String relativeUrl) {
        if (StringUtils.isEmpty(relativeUrl) || relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://"))
            return relativeUrl;

        try {
			return actualUrl.toURI().resolve(relativeUrl).toString();
			
		} catch (URISyntaxException e) {
			// should not happened
			e.printStackTrace();
			return null;
		}
    }
}

