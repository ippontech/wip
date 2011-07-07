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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import fr.ippon.wip.transformers.URLTypes;

/**
 * Interface used to manage the configuration of the portlet.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public interface WIPConfiguration {

	/**
	 * Save the current configuration
	 */
	public void save();

	/** 
	 * Get the instance name associated with this configuration.
	 * @return the instance name as a string
	 */
	public String getInstance();
	
	/**
	 * Get the configuration as a string.
	 * @return the configuration as a string
	 */
	public String getConfigAsString();
	
	/**
	 * Set the URL of the distant application to integrate in the portlet
	 * @param initUrl the initial URL
	 */
	public void setInitUrl(URL initUrl);

	/**
	 * Get the initial URL of the portlet, i.e. the URL of the application
	 * launched in the portlet.
	 * @return The URL of the initial URL
	 */
	public URL getInitUrl();

	/**
	 * Same method as the previous one, but returning the URL as a string.
	 * @return The String representation of the initial URL
	 */
	public String getInitUrlAsString();
	
	/**
	 * Set a list of URL to proxy. Every URL resource in the HTML code related
	 * to an URL given in the list will be rewrited to be treated through the portal.
	 * @param domainsToProxy The list of URL
	 */
	public void setDomainsToProxy(List<URL> domainsToProxy);
	
	/**
	 * Get the list of domains who will be proxied by the portlet
	 * @return The list of URL proxied
	 */
	public List<URL> getDomainsToProxy();

	/**
	 * Set the portlet title.
	 * @param title the portlet title
	 */
	public void setPortletTitle(String title);
	
	/**
	 * Get the portlet title.
	 * @return the portlet title
	 */
	public String getPortletTitle();
	
	/**
	 * Set the regular expression used to rewrite javascript code 
	 * @param jsRegex The regular expression
	 */
	public void setJsRegex(String jsRegex);

	/**
	 * Get the regular expression used to rewrite the javascript code
	 * @return The regular expression
	 */
	public String getJsRegex();

	/**
	 * Set the regular expression used to rewrite CSS code
	 * @param cssRegex The regular expression
	 */
	public void setCssRegex(String cssRegex);

	/**
	 * Get the regular expression used to rewrite CSS code
	 * @return The regular expression
	 */
	public String getCssRegex();
	
	/**
	 * Set the id of the div containing the content of the distant application.
	 * This is useful to protect the CSS code of the portal from the CSS
	 * retrieved from the distant application
	 * @param portletDivId  The id
	 */
	public void setPortletDivId(String portletDivId);

	/**
	 * Get the current id of the div containing the content of the distant application.
	 * @return The id
	 */
	public String getPortletDivId();

	/**
	 * Enable or not the add of the WIP prefix before CSS selectors to avoid CSS conflicts.
	 * @param b a boolean indicating if the prefix has to be added
	 */
	public void setAddPrefix(boolean b);
	
	/**
	 * Check if the prefix is added before CSS selectors.
	 * @return a boolean indicating if the prefix is added
	 */
	public boolean getAddPrefix();
	
	/**
	 * Enable or not to replace CSS absolute positioning by relative positioning.
	 * @param b a boolean indicating if the absolute positioning is kept or not
	 */
	public void setAbsolutePositioning(boolean b);
	
	/**
	 * Check if the absolute positioning is replaced.
	 * @return a boolean indicating if the absolute positioning is replaced
	 */
	public boolean getAbsolutePositioning();

	/**
	 * Set the custom CSS to be used to customize the style of the content of the portlet. 
	 * @param customCss The custom CSS code
	 */
	public void setCustomCss(String customCss);

	/**
	 * Get the custom CSS used to customize the style of the content of the portlet.
	 * @return The custom CSS code
	 */
	public String getCustomCss();
	
	/**
	 * Fill a list of domains from a given String containing domains separated by ';'
	 * @param domains The String representation of the list
	 * @return The list of domains
	 * @throws MalformedURLException if one or several domains given in the String are malformed
	 */
	public List<URL> setDomainsFromString(String domains) throws MalformedURLException;

	/**
	 * Get a String containing the given domains separated by ';'
	 * @param domains The list of domains
	 * @return The list of domains in a String representation
	 */
	public String getDomainsAsString(List<URL> domains);
	
	/**
	 * Set the clipping type
	 * @param type the clipping type
	 */
	public void setClippingType(String type);
	
	/**
	 * Get the clipping type (none, basic, xslt)
	 * @return the clipping type  
	 */
	public String getClippingType();
	
	/**
	 * Get the XPath used to proceed the XSLT transformation.
	 * @return the XPath
	 */
	public String getXPath();
	
	/**
	 * Set the XPath with the value defined by the user.
	 * @param xpath the XPath
	 */
	public void setXPath(String xpath);
	
	/**
	 * Set the XSLT code to proceed clipping
	 * @param xslt the XSLT code as a string
	 */
	public void setXsltClipping(String xslt);
	
	/**
	 * Get the XSLT code to proceed clipping
	 * @return the XSLT code as a string
	 */
	public String getXsltClipping();
	
	/**
	 * Set the XSLT code to proceed transformations
	 * @param xslt the XSLT code as a string
	 */
	public void setXsltTransform(String xslt);
	
	/**
	 * Get the XSLT code to proceed transformations
	 * @return the XSLT code as a string
	 */
	public String getXsltTransform();

	/**
	 * Set Ajax URLs to be rewritten during the JS transforming
	 * @param urls the list of URLs to be rewritten
	 */
	public void setJavascriptUrls(List<String> urls);

	/**
	 * Get Ajax URLs to be rewritten during the JS transforming
	 * @return the list of URLs to be rewritten
	 */
	public Map<String, URLTypes> getJavascriptUrls();
	
	/**
	 * Set the URLs of the scripts that will not be transsformed
	 * @param urls the list of URLs
	 */
	public void setScriptsToIgnore(List<String> urls);

	/**
	 * Get the URLs of the scripts that will not be transformed
	 * @return the list of URLs
	 */
	public List<String> getScriptsToIgnore();
	
	/**
	 * Enable or disable URLs rewriting.
	 * @param bool true to enable, else false
	 */
	public void setEnableUrlRewriting(boolean bool);
	
	/**
	 * Check if URLs rewriting is enable or disable.
	 * @return true if enable, else false
	 */
	public boolean getEnableUrlRewriting();
	
	/**
	 * Enable or disable CSS retrieving.
	 * @param bool true to enable, else false
	 */
	public void setEnableCssRetrieving(boolean bool);
	
	/**
	 * Check if CSS retrieving is enable or disable.
	 * @return true if enable, else false
	 */
	public boolean getEnableCssRetrieving();
	
	/**
	 * Enable or disable CSS rewriting.
	 * @param bool true to enable, else false
	 */
	public void setEnableCssRewriting(boolean bool);
	
	/**
	 * Check if CSS rewriting is enable or disable.
	 * @return true if enable, else false
	 */
	public boolean getEnableCssRewriting();
	
	
	// CACHE CONFIGURATION
	
	/**
	 * Check if cache is enable or disable.
	 * @return true if enable, else false
	 */
	public boolean getEnableCache();

	/**
	 * Enable or disable caching.
	 * @param enable true to enable, else false
	 */
	public void setEnableCache(boolean enable);

	/**
	 * Check if pages have to be cached as private.
	 * @return true if private, else false
	 */
	public boolean getPageCachePrivate();
	
	/**
	 * Force the pages to be cached as private or not.
	 * @param b true to force private, else false
	 */
	public void setPageCachePrivate(boolean b);
	
	/**
	 * Check if resources have to be cached as public.
	 * @return true if public, else false
	 */
	public boolean getResourceCachePublic();
	
	/**
	 * Force the resources to be cached as public or not.
	 * @param b true to force public, else false
	 */
	public void setResourceCachePublic(boolean b);
	
	/**
	 * Check if the page caching is forced.
	 * @return true if forced, else false
	 */
	public boolean getForcePageCaching();
	
	/**
	 * Force page caching.
	 * @param b true to force caching, else false
	 */
	public void setForcePageCaching(boolean b);
	
	/**
	 * Check if the resource caching is forced.
	 * @return true if forced, else false
	 */
	public boolean getForceResourceCaching();
	
	/**
	 * Force resource caching.
	 * @param b true to force caching, else false
	 */
	public void setForceResourceCaching(boolean b);
	
	/**
	 * Get the timeout for page caching.
	 * @return the timeout
	 */
	public int getPageCacheTimeout();
	
	/**
	 * Set the timeout for page caching.
	 * @param timeout the timeout to set
	 */
	public void setPageCacheTimeout(int timeout);
	
	/**
	 * Get the timeout for resource caching.
	 * @return the timeout
	 */
	public int getResourceCacheTimeout();
	
	/**
	 * Set the timeout for resource caching.
	 * @param timeout the timeout to set
	 */
	public void setResourceCacheTimeout(int timeout);

	/**
	 * Get the date rate to determinate the freshness according to
	 * creation date, current date and last modification date. 
	 * @return the cache date rate
	 */
	public int getCacheDateRate();
		
	/**
	 * Enable or disable LTPA SSO authentication
	 * @param b if enable then true else false
	 */
	public void setLtpaSsoAuthentication(boolean b);
	
	/**
	 * Is LTPA SSO authentication enabled
	 * @return if enable then true else false
	 */
	public boolean getLtpaSsoAuthentication();
	
	/**
	 * Set the LTPA secret provider class name
	 * @param name the class name
	 */
	public void setLtpaSecretProviderClassName(String name);
	
	/**
	 * Get the LTPA secret provider class name
	 * @return the class name
	 */
	public String getLtpaSecretProviderClassName();
	
	/**
	 * Set the credential provider class name
	 * @param name the class name
	 */
	public void setCredentialProviderClassName(String name);
	
	/**
	 * Get the credential provider class name
	 * @return the class name
	 */
	public String getCredentialProviderClassName();
	
}
