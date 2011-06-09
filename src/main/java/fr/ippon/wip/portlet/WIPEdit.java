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

package fr.ippon.wip.portlet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletSession;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;

/**
 * This class is a pseudo portlet whose aim is to save modifications of the
 * configuration of the WIPortlet. Its method is called by WIPortlet in
 * processAction, when the portlet is in edit mode.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class WIPEdit {

	/**
	 * A pseudo processAction method, replacing the WIPortlet's one in edit mode.
	 * @param request The ActionRequest sent to WIPortlet in edit mode
	 * @param response The ActionResponse sent to WIPortlet in edit mode
	 */
	public static void processAction(ActionRequest request, ActionResponse response) {
		if (request.getParameter("editPage") != null && !request.getParameter("editPage").equals("")) {
			request.getPortletSession().setAttribute("editPage", request.getParameter("editPage"));
		} else if (request.getParameter("form") != null) {
			switch (Integer.valueOf(request.getParameter("form"))) {
				case 1 : handleGeneralSettings(request, response); break;
				case 2 : handleClipping(request, response); break;
				case 3 : handleHtmlRewriting(request, response); break;
				case 4 : handleCSSRewriting(request, response); break;
				case 5 : handleJSRewriting(request, response); break;
				case 6 : handleCaching(request, response); break;
				case 7 : handleLTPAAuthentication(request, response); break;
			}
			// Removing the portlet's current url attribute to take the config changes in consideration
			request.getPortletSession().removeAttribute(WIPortlet.WIP_REQUEST_KEY);
		} else if (request.getParameter("configPage") != null) {
			request.getPortletSession().setAttribute("configPage", request.getParameter("configPage"));
		} else if (request.getParameter("changeConfig") != null) {
			WIPConfigurationManager.getInstance().loadConfiguration(request.getParameter("changeConfig"), response.getNamespace());
		} else if (request.getParameter("saveConfig") != null) {
			if (request.getParameter("saveConfig").equals("")) {
				request.getPortletSession().setAttribute("saveConfig", "");
			} else {
				WIPConfigurationManager.getInstance().saveConfiguration(request.getParameter("saveConfig"), response.getNamespace());
			}
		} else if (request.getParameter("source") != null) {
			String source = request.getParameter("source");
			String url = "";
			if (source.equals("other"))
				url = request.getParameter("url");
			if (url.equals(""))
				url = WIPConfigurationManager.getInstance().getConfiguration(response.getNamespace()).getInitUrlAsString();
			request.getPortletSession().setAttribute("source", url);
		} else if (request.getParameter("back") != null) {
			request.getPortletSession().setAttribute("back", request.getParameter("back"));
		}
	}

	/**
	 * Handle general settings: get settings in request parameters 
	 * from the configuration form. Save them in the portlet configuration.
	 * @param request The ActionRequest sent to WIPortlet in edit mode
	 * @param response The ActionResponse sent to WIPortlet in edit mode
	 */
	private static void handleGeneralSettings(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPConfigurationManager.getInstance().getConfiguration(response.getNamespace());
		ResourceBundle rb = ResourceBundle.getBundle("content.Language", request.getLocale());
		Map<String, String> errors = new HashMap<String, String>();

		// Getting the parameters from the request
		String tmpInitUrl = request.getParameter("initUrl");
		URL initUrl = buildURLIfNotEmpty("initUrl", tmpInitUrl, errors, rb);
		
		String tmpDomainsToProxy = request.getParameter("domainsToProxy");
		List<URL> domainsToProxy = buildURLList("domainsToProxy", tmpDomainsToProxy, errors, rb, wipConfig);
		
		String tmpEnableUrlRewriting = request.getParameter("enableUrlRewriting");
		boolean enableUrlRewriting = true;
		if (tmpEnableUrlRewriting == null) enableUrlRewriting = false;
		
		// Saving the new configuration
		try {
			if (initUrl != null)
				wipConfig.setInitUrl(initUrl);
			if (domainsToProxy != null) 
				wipConfig.setDomainsToProxy(domainsToProxy);
			wipConfig.setEnableUrlRewriting(enableUrlRewriting);
			wipConfig.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Sending errors to the portlet session
		request.getPortletSession().setAttribute("errors", errors, PortletSession.APPLICATION_SCOPE);
		
		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "generalsettings");
	}

	/**
	 * Handle clipping configuration: get settings in request parameters
	 * from the configuration form. Save them in the portlet configuartion.
	 * @param request The ActionRequest sent to WIPortlet in edit mode
	 * @param response The ActionResponse sent to WIPortlet in edit mode
	 */
	private static void handleClipping(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPConfigurationManager.getInstance().getConfiguration(response.getNamespace());
		ResourceBundle rb = ResourceBundle.getBundle("content.Language", request.getLocale());
		Map<String, String> errors = new HashMap<String, String>();

		// Getting the parameters from the request
		String clippingType = request.getParameter("clippingType");
		
		if (clippingType.equals("xpath")) {
			String xPath = request.getParameter("xPath");
			if (xPath.equals("")) {
				errors.put("xPath", rb.getString("wip.errors.xpath.empty"));
			} else {
				try {
					wipConfig.setXPath(xPath);
					wipConfig.setClippingType(clippingType);
					wipConfig.save();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (clippingType.equals("xslt")) {
			String xsltClipping = request.getParameter("xsltClipping");
			try {
				wipConfig.setXsltClipping(xsltClipping);
				wipConfig.setClippingType(clippingType);
				wipConfig.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				wipConfig.setClippingType(clippingType);
				wipConfig.save();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Sending errors to the portlet session
		request.getPortletSession().setAttribute("errors", errors, PortletSession.APPLICATION_SCOPE);
		
		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "clipping");
	}

	/**
	 * Handle HTML rewriting configuration: get settings in request parameters
	 * from the configuration form. Save them in the portlet configuartion.
	 * @param request The ActionRequest sent to WIPortlet in edit mode
	 * @param response The ActionResponse sent to WIPortlet in edit mode
	 */
	private static void handleHtmlRewriting(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPConfigurationManager.getInstance().getConfiguration(response.getNamespace());

		// Getting the parameters from the request
		String xsltTransform = request.getParameter("xsltTransform");
		
		// Saving the new configuration
		try {
			wipConfig.setXsltTransform(xsltTransform);
			wipConfig.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "htmlrewriting");
	}

	/**
	 * Handle CSS rewriting configuration: get settings in request parameters from the
	 * configuration form. Save them in the portlet configuartion.
	 * @param request The ActionRequest sent to WIPortlet in edit mode
	 * @param response The ActionResponse sent to WIPortlet in edit mode
	 */
	private static void handleCSSRewriting(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPConfigurationManager.getInstance().getConfiguration(response.getNamespace());
		Map<String, String> errors = new HashMap<String, String>();

		// Getting the parameters from the request
		String customCss = request.getParameter("customCss");
		String cssRegex = request.getParameter("cssRegex");
		String portletDivId = request.getParameter("portletDivId");
		
		String tmpEnableCssRetrieving = request.getParameter("enableCssRetrieving");
		boolean enableCssRetrieving = true;
		if (tmpEnableCssRetrieving == null) enableCssRetrieving = false;
		
		String tmpAbsolutePositioning = request.getParameter("absolutePositioning");
		boolean absolutePositioning = true;
		if (tmpAbsolutePositioning == null) absolutePositioning = false;
		
		String tmpAddPrefix = request.getParameter("addPrefix");
		boolean addPrefix = true;
		if (tmpAddPrefix == null) addPrefix = false;
		
		String tmpEnableCssRewriting = request.getParameter("enableCssRewriting");
		boolean enableCssRewriting = true;
		if (tmpEnableCssRewriting == null) enableCssRewriting = false;

		// Saving the new configuration
		try {
			wipConfig.setCssRegex(cssRegex);
			wipConfig.setAbsolutePositioning(absolutePositioning);
			wipConfig.setAddPrefix(addPrefix);
			wipConfig.setPortletDivId(portletDivId);
			wipConfig.setEnableCssRetrieving(enableCssRetrieving);
			wipConfig.setEnableCssRewriting(enableCssRewriting);
			wipConfig.setCustomCss(customCss);
			wipConfig.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Sending errors to the portlet session
		request.getPortletSession().setAttribute("errors", errors, PortletSession.APPLICATION_SCOPE);
		
		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "cssrewriting");
	}

	/**
	 * Handle JS rewriting configuration: get settings in request parameters
	 * from the configuration form. Save them in the portlet configuartion.
	 * @param request The ActionRequest sent to WIPortlet in edit mode
	 * @param response The ActionResponse sent to WIPortlet in edit mode
	 */
	private static void handleJSRewriting(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPConfigurationManager.getInstance().getConfiguration(response.getNamespace());
		Map<String, String> errors = new HashMap<String, String>();

		// Getting the parameters from the request
		String jsRegex = request.getParameter("jsRegex");
		
		String tmpJavascriptUrls = request.getParameter("javascriptUrls");
		String[] l1 = tmpJavascriptUrls.split(";");
		List<String> javascriptUrls = new ArrayList<String>(); 
		for (int i=0; i<l1.length; i++) javascriptUrls.add(l1[i]);
		
		String tmpScriptsToIgnore = request.getParameter("scriptUrls");
		String[] l3 = tmpScriptsToIgnore.split(";");
		List<String> scriptsToIgnore = new ArrayList<String>(); 
		for (int i=0; i<l3.length; i++) scriptsToIgnore.add(l3[i]);
		
		// Saving the new configuration
		try {
			wipConfig.setJsRegex(jsRegex);
			wipConfig.setJavascriptUrls(javascriptUrls);
			wipConfig.setScriptsToIgnore(scriptsToIgnore);
			wipConfig.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Sending errors to the portlet session
		request.getPortletSession().setAttribute("errors", errors, PortletSession.APPLICATION_SCOPE);
		
		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "jsrewriting");
	}

	private static void handleCaching(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPConfigurationManager.getInstance().getConfiguration(response.getNamespace());
		Map<String, String> errors = new HashMap<String, String>();

		// Getting the parameters from the request
		String tmpEnableCache = request.getParameter("enableCache");
		boolean enableCache = true;
		if (tmpEnableCache == null) enableCache = false;
		
		try {
			wipConfig.setEnableCache(enableCache);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (enableCache) {
			String tmpPageCachePrivate = request.getParameter("pageCachePrivate");
			boolean pageCachePrivate = true;
			if (tmpPageCachePrivate == null) pageCachePrivate = false;
			
			String tmpResourceCachePublic = request.getParameter("resourceCachePublic");
			boolean resourceCachePublic = true;
			if (tmpResourceCachePublic == null) resourceCachePublic = false;
			
			String tmpForcePageCaching = request.getParameter("forcePageCaching");
			boolean forcePageCaching = true;
			if (tmpForcePageCaching == null) forcePageCaching = false;
			
			String tmpForceResourceCaching = request.getParameter("forceResourceCaching");
			boolean forceResourceCaching = true;
			if (tmpForceResourceCaching == null) forceResourceCaching = false;
			
			String tmpPageCacheTimeout = request.getParameter("pageCacheTimeout");
			int pageCacheTimeout = 0;
			if (tmpPageCacheTimeout != null) pageCacheTimeout = Integer.parseInt(tmpPageCacheTimeout);
			
			String tmpResourceCacheTimeout = request.getParameter("resourceCacheTimeout");
			int resourceCacheTimeout = 0;
			if (tmpResourceCacheTimeout != null) resourceCacheTimeout = Integer.parseInt(tmpResourceCacheTimeout);
			
			try {
				wipConfig.setPageCachePrivate(pageCachePrivate);
				wipConfig.setResourceCachePublic(resourceCachePublic);
				wipConfig.setForcePageCaching(forcePageCaching);
				wipConfig.setForceResourceCaching(forceResourceCaching);
				wipConfig.setPageCacheTimeout(pageCacheTimeout);
				wipConfig.setResourceCacheTimeout(resourceCacheTimeout);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Sending errors to the portlet session
		request.getPortletSession().setAttribute("errors", errors, PortletSession.APPLICATION_SCOPE);
		
		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "caching");
	}
	
	private static void handleLTPAAuthentication(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPConfigurationManager.getInstance().getConfiguration(response.getNamespace());
		Map<String, String> errors = new HashMap<String, String>();

		// Getting the parameters from the request
		String tmpLtpaSsoAuthentication = request.getParameter("ltpaSsoAuthentication");
		boolean ltpaSsoAuthentication = true;
		if (tmpLtpaSsoAuthentication == null) ltpaSsoAuthentication = false;
		String ltpaSecretProviderClassName = request.getParameter("ltpaSecretProviderClassName");
		String credentialProviderClassName = request.getParameter("credentialProviderClassName");
		
		// Saving the new configuration
		try {
			wipConfig.setLtpaSsoAuthentication(ltpaSsoAuthentication);
			wipConfig.setLtpaSecretProviderClassName(ltpaSecretProviderClassName);
			wipConfig.setCredentialProviderClassName(credentialProviderClassName);
			wipConfig.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Sending errors to the portlet session
		request.getPortletSession().setAttribute("errors", errors, PortletSession.APPLICATION_SCOPE);
		
		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "ltpaauth");
	}
	
	/**
	 * This class will try to build an URL from a string and store an error if the URL is malformed or empty
	 * @param varName The name of the variable, used to map errors correctly
	 * @param urlAsString The String to convert into a list
	 * @param errors The map containing the error messages
	 * @param rb The resource bundle used to set the error message
	 * @return The URL built from the given String
	 */
	private static URL buildURLIfNotEmpty(String varName, String urlAsString, Map<String, String> errors, ResourceBundle rb) {
		URL result = null;
		if (!urlAsString.equals("")) {
			try {
				result = new URL(urlAsString);
			} catch (MalformedURLException e1) {
				errors.put(varName, rb.getString("wip.errors." + varName + ".malformed"));
			}
		} else {
			errors.put(varName, rb.getString("wip.errors." + varName + ".empty"));
		}
		return result;
	}
	
	/**
	 * This class will try to build a list of URLs from a string and store an error if an URL is malformed
	 * @param varName The name of the variable, used to map errors correctly
	 * @param urlListAsString The String to convert into a list
	 * @param errors The map containing the error messages
	 * @param rb The resource bundle used to set the error message
	 * @return A list of the URLs contained in the given String
	 */
	private static List<URL> buildURLList(String varName,
			String urlListAsString, Map<String, String> errors,
			ResourceBundle rb, WIPConfiguration wipConfig) {
		List<URL> result = new ArrayList<URL>();
		if (!urlListAsString.equals("")) {
			try {
				result = wipConfig.setDomainsFromString(urlListAsString);
			} catch (MalformedURLException e1) {
				errors.put(varName, rb.getString("wip.errors." + varName + ".malformed"));
			}
		}
		return result;
	}
	
}
