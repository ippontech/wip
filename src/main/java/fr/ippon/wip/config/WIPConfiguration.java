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

package fr.ippon.wip.config;

import java.util.ArrayList;
import java.util.HashMap;

import fr.ippon.wip.http.request.PostRequestBuilder;

import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * The configuration parameters of a wip portlet.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 * @author Yohan Legat
 */

public class WIPConfiguration implements Cloneable {

	public static final String SEPARATOR = ";";

	// if someday we use a database...
	private int id;

	private String name;

	private String credentialProviderClassName;

	private String ltpaSecretProviderClassName;

	private boolean ltpaSsoAuthentication;

	private boolean forceResourceCaching;

	private boolean forcePageCaching;

	private boolean pageCachePrivate;

	private boolean enableCache;

	private boolean enableCssRewriting;

	private boolean enableCssRetrieving;

	private boolean enableUrlRewriting;

	private List<String> scriptsToDelete;

	private List<String> scriptsToIgnore;

	private String xsltClipping;

	private String xPath;

	private String clippingType;

	private String customCss;

	private boolean absolutePositioning;

	private boolean addPrefix;

	private String portletDivId;

	private String cssRegex;

	private String jsRegex;

	private String portletTitle;

	private String initUrl;

	private List<String> domainsToProxy;

	private String xsltTransform;

	private List<String> javascriptUrls;

	private Map<String, PostRequestBuilder.ResourceType> javascriptResourcesMap;

	// versioning
	private long timestamp;

	public WIPConfiguration() {
		timestamp = 0;
		javascriptUrls = new ArrayList<String>();
		scriptsToDelete = new ArrayList<String>();
		scriptsToIgnore = new ArrayList<String>();
		domainsToProxy = new ArrayList<String>();
	}

	@Override
	public Object clone() {
		WIPConfiguration clone = null;
		try {
			clone = (WIPConfiguration) super.clone();
			clone.setScriptsToDelete(new ArrayList<String>(getScriptsToDelete()));
			clone.setScriptsToIgnore(new ArrayList<String>(getScriptsToIgnore()));
			clone.setDomainsToProxy(new ArrayList<String>(getDomainsToProxy()));
			clone.setJavascriptUrls(new ArrayList<String>(getJavascriptUrls()));
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return clone;
	}

	/**
	 * Get the clipping type (none, basic, xslt)
	 * 
	 * @return the clipping type
	 */
	public String getClippingType() {
		return clippingType;
	}

	/**
	 * Get the credential provider class name
	 * 
	 * @return the class name
	 */
	public String getCredentialProviderClassName() {
		return credentialProviderClassName;
	}

	/**
	 * Get the regular expression used to rewrite CSS code
	 * 
	 * @return The regular expression
	 */
	public String getCssRegex() {
		return cssRegex;
	}

	/**
	 * Get the custom CSS used to customize the style of the content of the
	 * portlet.
	 * 
	 * @return The custom CSS code
	 */
	public String getCustomCss() {
		return customCss;
	}

	/**
	 * Get the list of domains who will be proxied by the portlet
	 * 
	 * @return The list of URL proxied
	 */
	public List<String> getDomainsToProxy() {
		return domainsToProxy;
	}

	public int getId() {
		return id;
	}

	/**
	 * Get the initial URL of the portlet as a string, i.e. the URL of the
	 * application launched in the portlet.
	 * 
	 * @return the initial URL
	 */
	public String getInitUrl() {
		return initUrl;
	}

	/**
	 * Associate AJAX resources with their URLs.
	 * 
	 * @return the association between AJAX resources and their URLs as a map
	 */
	public synchronized Map<String, PostRequestBuilder.ResourceType> getJavascriptResourcesMap() {
		if (javascriptResourcesMap == null) {
			javascriptResourcesMap = new HashMap<String, PostRequestBuilder.ResourceType>();
			List<String> javascriptUrlsClone = new ArrayList<String>(javascriptUrls);

			if (javascriptUrlsClone.size() == 1 && javascriptUrlsClone.get(0).equals(""))
				javascriptUrlsClone = new ArrayList<String>();

			for (String input : javascriptUrlsClone) {
				String tmp[] = input.split("::::");
				if (tmp.length == 2) {
					String url = tmp[0];
					PostRequestBuilder.ResourceType type = PostRequestBuilder.ResourceType.valueOf(tmp[1]);
					javascriptResourcesMap.put(url, type);
				}
			}
		}

		return javascriptResourcesMap;
	}

	/**
	 * Get Ajax URLs to be rewritten during the JS transforming
	 * 
	 * @return the list of URLs to be rewritten
	 */
	public List<String> getJavascriptUrls() {
		return javascriptUrls;
	}

	/**
	 * Get the regular expression used to rewrite the javascript code
	 * 
	 * @return The regular expression
	 */
	public String getJsRegex() {
		return jsRegex;
	}

	/**
	 * Get the LTPA secret provider class name
	 * 
	 * @return the class name
	 */
	public String getLtpaSecretProviderClassName() {
		return ltpaSecretProviderClassName;
	}

	/**
	 * Get the configuration name.
	 * 
	 * @return the configuration name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the current id of the div containing the content of the distant
	 * application.
	 * 
	 * @return The id
	 */
	public String getPortletDivId() {
		return portletDivId;
	}

	/**
	 * Get the portlet title.
	 * 
	 * @return the portlet title
	 */
	public String getPortletTitle() {
		return portletTitle;
	}

	/**
	 * Get the URLs of the scripts that will be deleted
	 * 
	 * @return the list of URLs
	 */
	public List<String> getScriptsToDelete() {
		return scriptsToDelete;
	}

	/**
	 * Get the URLs of the scripts that will not be transformed
	 * 
	 * @return the list of URLs
	 */
	public List<String> getScriptsToIgnore() {
		return scriptsToIgnore;
	}

	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Get the XPath used to proceed the XSLT transformation.
	 * 
	 * @return the XPath
	 */
	public String getXPath() {
		return xPath;
	}

	/**
	 * Get the XSLT code to proceed clipping
	 * 
	 * @return the XSLT code as a string
	 */
	public String getXsltClipping() {
		return xsltClipping;
	}

	/**
	 * Get the XSLT code to proceed transformations
	 * 
	 * @return the XSLT code as a string
	 */
	public String getXsltTransform() {
		return xsltTransform;
	}

	/**
	 * Check if the absolute positioning is replaced.
	 * 
	 * @return a boolean indicating if the absolute positioning is replaced
	 */
	public boolean isAbsolutePositioning() {
		return absolutePositioning;
	}

	/**
	 * Check if the prefix is added before CSS selectors.
	 * 
	 * @return a boolean indicating if the prefix is added
	 */
	public boolean isAddPrefix() {
		return addPrefix;
	}

	/**
	 * Check if cache is enable or disable.
	 * 
	 * @return true if enable, else false
	 */
	public boolean isEnableCache() {
		return enableCache;
	}

	/**
	 * Check if CSS retrieving is enable or disable.
	 * 
	 * @return true if enable, else false
	 */
	public boolean isEnableCssRetrieving() {
		return enableCssRetrieving;
	}

	/**
	 * Check if CSS rewriting is enable or disable.
	 * 
	 * @return true if enable, else false
	 */
	public boolean isEnableCssRewriting() {
		return enableCssRewriting;
	}

	/**
	 * Check if URLs rewriting is enable or disable.
	 * 
	 * @return true if enable, else false
	 */
	public boolean isEnableUrlRewriting() {
		return enableUrlRewriting;
	}

	/**
	 * Check if the page caching is forced.
	 * 
	 * @return true if forced, else false
	 */
	public boolean isForcePageCaching() {
		return forcePageCaching;
	}

	/**
	 * Check if the resource caching is forced.
	 * 
	 * @return true if forced, else false
	 */
	public boolean isForceResourceCaching() {
		return forceResourceCaching;
	}

	/**
	 * Is LTPA SSO authentication enabled
	 * 
	 * @return if enable then true else false
	 */
	public boolean isLtpaSsoAuthentication() {
		return ltpaSsoAuthentication;
	}

	/**
	 * Check if pages have to be cached as private.
	 * 
	 * @return true if private, else false
	 */
	public boolean isPageCachePrivate() {
		return pageCachePrivate;
	}

	/**
	 * Check if the given uri has to be proxied
	 * 
	 * @param uri
	 * @return true if the uri has to be proxied
	 */
	public boolean isProxyURI(final String uri) {
		if(uri.endsWith("css"))
			return true;
		
		return Iterables.any(getDomainsToProxy(), new Predicate<String>() {

			public boolean apply(String domain) {
				return uri.startsWith(domain);
			}
		});
	}

	/**
	 * Enable or not to replace CSS absolute positioning by relative
	 * positioning.
	 * 
	 * @param absolutePositioning
	 *            a boolean indicating if the absolute positioning is kept or
	 *            not
	 */
	public void setAbsolutePositioning(boolean absolutePositioning) {
		this.absolutePositioning = absolutePositioning;
	}

	/**
	 * Enable or not the add of the WIP prefix before CSS selectors to avoid CSS
	 * conflicts.
	 * 
	 * @param addPrefix
	 *            a boolean indicating if the prefix has to be added
	 */
	public void setAddPrefix(boolean addPrefix) {
		this.addPrefix = addPrefix;
	}

	/**
	 * Set the clipping type
	 * 
	 * @param clippingType
	 *            the clipping type
	 */
	public void setClippingType(String clippingType) {
		this.clippingType = clippingType;
	}

	/**
	 * Set the credential provider class name
	 * 
	 * @param ltpaSecretProviderClassName
	 *            the class name
	 */
	public void setCredentialProviderClassName(String ltpaSecretProviderClassName) {
		this.credentialProviderClassName = ltpaSecretProviderClassName;
	}

	/**
	 * Set the regular expression used to rewrite CSS code
	 * 
	 * @param cssRegex
	 *            The regular expression
	 */
	public void setCssRegex(String cssRegex) {
		this.cssRegex = cssRegex;
	}

	/**
	 * Set the custom CSS to be used to customize the style of the content of
	 * the portlet.
	 * 
	 * @param customCss
	 *            The custom CSS code
	 */
	public void setCustomCss(String customCss) {
		this.customCss = customCss;
	}

	/**
	 * Set a list of URL to proxy. Every URL resource in the HTML code related
	 * to an URL given in the list will be rewrited to be treated through the
	 * portal.
	 * 
	 * @param domainsToProxy
	 *            The list of URL
	 */
	public void setDomainsToProxy(List<String> domainsToProxy) {
		this.domainsToProxy = new ArrayList<String>();
		for(String domain : domainsToProxy)
			this.domainsToProxy.add(domain.trim());
	}

	/**
	 * Enable or disable caching.
	 * 
	 * @param enableCache
	 *            true to enable, else false
	 */
	public void setEnableCache(boolean enableCache) {
		this.enableCache = enableCache;
	}

	/**
	 * Enable or disable CSS retrieving.
	 * 
	 * @param enableCssRetrieving
	 *            true to enable, else false
	 */
	public void setEnableCssRetrieving(boolean enableCssRetrieving) {
		this.enableCssRetrieving = enableCssRetrieving;
	}

	/**
	 * Enable or disable CSS rewriting.
	 * 
	 * @param enableCssRewriting
	 *            true to enable, else false
	 */
	public void setEnableCssRewriting(boolean enableCssRewriting) {
		this.enableCssRewriting = enableCssRewriting;
	}

	/**
	 * Enable or disable URLs rewriting.
	 * 
	 * @param enableUrlRewriting
	 *            true to enable, else false
	 */
	public void setEnableUrlRewriting(boolean enableUrlRewriting) {
		this.enableUrlRewriting = enableUrlRewriting;
	}

	/**
	 * Force page caching.
	 * 
	 * @param forcePageCaching
	 *            true to force caching, else false
	 */
	public void setForcePageCaching(boolean forcePageCaching) {
		this.forcePageCaching = forcePageCaching;
	}

	/**
	 * Force resource caching.
	 * 
	 * @param forceResourceCaching
	 *            true to force caching, else false
	 */
	public void setForceResourceCaching(boolean forceResourceCaching) {
		this.forceResourceCaching = forceResourceCaching;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Set the URL of the distant application to integrate in the portlet
	 * 
	 * @param initUrl
	 *            the initial URL
	 */
	public void setInitUrl(String initUrl) {
		this.initUrl = initUrl;
	}

	/**
	 * Set Ajax URLs to be rewritten during the JS transforming
	 * 
	 * @param javascriptUrls
	 *            the list of URLs to be rewritten
	 */
	public void setJavascriptUrls(List<String> javascriptUrls) {
		this.javascriptUrls = new ArrayList<String>();
		for(String url : javascriptUrls)
			this.javascriptUrls.add(url.trim());
	}

	/**
	 * Set the regular expression used to rewrite javascript code
	 * 
	 * @param jsRegex
	 *            The regular expression
	 */
	public void setJsRegex(String jsRegex) {
		this.jsRegex = jsRegex;
	}

	/**
	 * Set the LTPA secret provider class name
	 * 
	 * @param ltpaSecretProviderClassName
	 *            the class name
	 */
	public void setLtpaSecretProviderClassName(String ltpaSecretProviderClassName) {
		this.ltpaSecretProviderClassName = ltpaSecretProviderClassName;
	}

	/**
	 * Enable or disable LTPA SSO authentication
	 * 
	 * @param ltpaSsoAuthentication
	 *            if enable then true else false
	 */
	public void setLtpaSsoAuthentication(boolean ltpaSsoAuthentication) {
		this.ltpaSsoAuthentication = ltpaSsoAuthentication;
	}

	/**
	 * Set the name of the configuration
	 * 
	 * @param the
	 *            name of the configuration
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Force the pages to be cached as private or not.
	 * 
	 * @param pageCachePrivate
	 *            true to force private, else false
	 */
	public void setPageCachePrivate(boolean pageCachePrivate) {
		this.pageCachePrivate = pageCachePrivate;
	}

	/**
	 * Set the id of the div containing the content of the distant application.
	 * This is useful to protect the CSS code of the portal from the CSS
	 * retrieved from the distant application
	 * 
	 * @param portletDivId
	 *            The id
	 */
	public void setPortletDivId(String portletDivId) {
		this.portletDivId = portletDivId;
	}

	/**
	 * Set the portlet title.
	 * 
	 * @param portletTitle
	 *            the portlet title
	 */
	public void setPortletTitle(String portletTitle) {
		this.portletTitle = portletTitle;
	}

	/**
	 * Set the URLs of the scripts that will be deleted
	 * 
	 * @param scriptsToDelete
	 *            the list of URLs
	 */
	public void setScriptsToDelete(List<String> scriptsToDelete) {
		this.scriptsToDelete = new ArrayList<String>();
		for(String script : scriptsToDelete)
			this.scriptsToDelete.add(script.trim());
	}

	/**
	 * Set the URLs of the scripts that will not be transformed
	 * 
	 * @param scriptsToIgnore
	 *            the list of URLs
	 */
	public void setScriptsToIgnore(List<String> scriptsToIgnore) {
		this.scriptsToIgnore = new ArrayList<String>();
		for(String script : scriptsToIgnore)
			this.scriptsToIgnore.add(script.trim());
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Set the XPath with the value defined by the user.
	 * 
	 * @param xpath
	 *            the XPath
	 */
	public void setXPath(String xpath) {
		this.xPath = xpath;
	}

	/**
	 * Set the XSLT code to proceed clipping
	 * 
	 * @param xsltClipping
	 *            the XSLT code as a string
	 */
	public void setXsltClipping(String xsltClipping) {
		this.xsltClipping = xsltClipping;
	}

	/**
	 * Set the XSLT code to proceed transformations
	 * 
	 * @param xsltTransform
	 *            the XSLT code as a string
	 */
	public void setXsltTransform(String xsltTransform) {
		this.xsltTransform = xsltTransform;
	}
}
