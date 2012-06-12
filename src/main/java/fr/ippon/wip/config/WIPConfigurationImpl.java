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

import fr.ippon.wip.http.Request;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * An implementation of the WIPConfiguration interface using apache commons
 * configuration based on a XML file. This class is a singleton.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class WIPConfigurationImpl implements WIPConfiguration {

	private static final Logger LOG = Logger.getLogger(WIPConfigurationImpl.class.getName());

	/**
	 * Apache commons configuration object to manager XML configuration.
	 */
	private XMLConfiguration config;

	private boolean readOnly;

	private String name;

	public WIPConfigurationImpl(File configFile) {
		this(configFile, false);
	}

	public WIPConfigurationImpl(File configFile, boolean readOnly) {
		try {
			name = configFile.getName();
			name = name.substring(0, name.lastIndexOf("."));

			this.readOnly = readOnly;
			this.config = new XMLConfiguration(configFile);
			this.config.setDelimiterParsingDisabled(true);

		} catch (ConfigurationException e) {
			LOG.log(Level.SEVERE, "Could not instanciate XMLConfiguration", e);
		}
	}

	public boolean getAbsolutePositioning() {
		return config.getBoolean("absolutePositioning");
	}

	// GENERAL CONFIG

	public boolean getAddPrefix() {
		return config.getBoolean("addPrefix");
	}

	public int getCacheDateRate() {
		return config.getInt("cacheDateRate");
	}

	public String getClippingType() {
		return config.getString("clippingType");
	}

	public String getConfigAsString() {
		try {
			Source source = new DOMSource(config.getDocument());
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString();

		} catch (Exception e) {
			return StringUtils.EMPTY;
		}
	}

	public String getCredentialProviderClassName() {
		return config.getString("credentialProviderClassName");
	}

	public String getCssRegex() {
		return config.getString("cssRegex");
	}

	public String getCustomCss() {
		return config.getString("customCss");
	}

	public String getDomainsAsString(List<URL> domains) {
		String domainsAsString = "";
		for (URL u : domains)
			domainsAsString += u.toExternalForm() + ";";
		if (domainsAsString.length() > 0)
			domainsAsString = domainsAsString.substring(0, domainsAsString.length() - 1);
		return domainsAsString;
	}

	public List<URL> getDomainsToProxy() {
		return setDomainsFromString(config.getString("domainsToProxy"));
	}

	// CSS REWRITING CONFIG

	public boolean getEnableCache() {
		return config.getBoolean("enableCache");
	}

	public boolean getEnableCssRetrieving() {
		return config.getBoolean("enableCssRetrieving");
	}

	public boolean getEnableCssRewriting() {
		return config.getBoolean("enableCssRewriting");
	}

	public boolean getEnableUrlRewriting() {
		return config.getBoolean("enableUrlRewriting");
	}

	public boolean getForcePageCaching() {
		return config.getBoolean("forcePageCaching");
	}

	public boolean getForceResourceCaching() {
		return config.getBoolean("forceResourceCaching");
	}

	public URL getInitUrl() {
		URL url = null;
		String path = config.getString("initUrl");
		try {
			url = new URL(path);
		} catch (MalformedURLException e) {
			LOG.warning("Malformed init URL: " + path);
		}
		return url;
	}

	public String getInitUrlAsString() {
		return config.getString("initUrl");
	}

	@SuppressWarnings("unchecked")
	public Map<String, Request.ResourceType> getJavascriptUrls() {
		Map<String, Request.ResourceType> map = new HashMap<String, Request.ResourceType>();
		List<String> l = config.getList("javascriptUrls");
		if (l.size() == 1 && l.get(0).equals(""))
			l = new ArrayList<String>();
		for (String input : l) {
			String tmp[] = input.split("::::");
			if (tmp.length == 2) {
				String url = tmp[0];
				Request.ResourceType type = Request.ResourceType.valueOf(tmp[1]);
				map.put(url, type);
			}
		}
		return map;
	}

	public String getJsRegex() {
		return config.getString("jsRegex");
	}

	// JS REWRITING CONFIG

	public String getLtpaSecretProviderClassName() {
		return config.getString("ltpaSecretProviderClassName");
	}

	public boolean getLtpaSsoAuthentication() {
		return config.getBoolean("ltpaSsoAuthentication");
	}

	public String getName() {
		return name;
	}

	public boolean getPageCachePrivate() {
		return config.getBoolean("pageCachePrivate");
	}

	public int getPageCacheTimeout() {
		return config.getInt("pageCacheTimeout");
	}

	public String getPortletDivId() {
		return config.getString("portletDivId");
	}

	public String getPortletTitle() {
		return config.getString("portletTitle");
	}

	public boolean getResourceCachePublic() {
		return config.getBoolean("resourceCachePublic");
	}

	// CLIPPING CONFIG

	public int getResourceCacheTimeout() {
		return config.getInt("resourceCacheTimeout");
	}

	@SuppressWarnings("unchecked")
	public List<String> getScriptsToDelete() {
		List<String> l = config.getList("scriptsToDelete");
		if (l.size() == 1 && l.get(0).equals(""))
			l = new ArrayList<String>();
		return l;
	}

	@SuppressWarnings("unchecked")
	public List<String> getScriptsToIgnore() {
		List<String> l = config.getList("scriptsToIgnore");
		if (l.size() == 1 && l.get(0).equals(""))
			l = new ArrayList<String>();
		return l;
	}

	public String getXPath() {
		return config.getString("xPath");
	}

	public String getXsltClipping() {
		String result = "";
		if (!config.getString("xsltClipping").equals("")) {
			result = config.getString("xsltClipping");
		} else {
			try {
				BufferedInputStream is = (BufferedInputStream) getClass().getResource("/xslt/clipping.xslt").getContent();
				StringWriter writer = new StringWriter();
				InputStreamReader streamReader = new InputStreamReader(is);
				BufferedReader buffer = new BufferedReader(streamReader);
				String line;
				while ((line = buffer.readLine()) != null) {
					writer.write(line + "\n");
				}
				result = writer.toString();
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Could not load default XSLT file for clipping", e);
			}
		}
		return result;
	}

	public String getXsltTransform() {
		String result = "";
		if (!config.getString("xsltTransform").equals("")) {
			result = config.getString("xsltTransform");
		} else {
			try {
				BufferedInputStream is = new BufferedInputStream(getClass().getResource("/xslt/default.xslt").openConnection().getInputStream());
				StringWriter writer = new StringWriter();
				InputStreamReader streamReader = new InputStreamReader(is);
				BufferedReader buffer = new BufferedReader(streamReader);
				String line;
				while ((line = buffer.readLine()) != null) {
					writer.write(line + "\n");
				}
				result = writer.toString();
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Could not load default XSLT file", e);
			}
		}
		return result;
	}

	public boolean isProxyURI(String uri) {
		for (URL baseURL : getDomainsToProxy()) {
			if (uri.startsWith(baseURL.toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Save this configuration into its file.
	 */
	public void save() {
		try {
			if (!readOnly)
				config.save();

		} catch (ConfigurationException e) {
			LOG.log(Level.SEVERE, "Could not save XMLConfiguration", e);
		}
	}

	public void setAbsolutePositioning(boolean b) {
		config.setProperty("absolutePositioning", b);
	}

	public void setAddPrefix(boolean b) {
		config.setProperty("addPrefix", b);
	}

	public void setClippingType(String type) {
		config.setProperty("clippingType", type);
	}

	public void setCredentialProviderClassName(String name) {
		config.setProperty("credentialProviderClassName", name);
	}

	public void setCssRegex(String cssRegex) {
		config.setProperty("cssRegex", cssRegex);
	}

	public void setCustomCss(String customCss) {
		config.setProperty("customCss", customCss);
	}

	// CACHE CONFIGURATION

	public List<URL> setDomainsFromString(String string) {
		ArrayList<URL> list = new ArrayList<URL>();
		if (!string.equals("")) {
			for (String s : string.split(";")) {
				try {
					list.add(new URL(s));
				} catch (MalformedURLException e) {
					LOG.warning("Malformed URL for domain to proxy: " + s);
				}
			}
		} else if (!getInitUrl().toExternalForm().equals("")) {
			URL init = null;
			String path = getInitUrl().getProtocol() + "://" + getInitUrl().getHost();
			try {
				init = new URL(path);
			} catch (MalformedURLException e) {
				LOG.warning("Malformed URL for domain to proxy: " + path);
			}
			list.add(init);
		}
		return list;
	}

	public void setDomainsToProxy(List<URL> domainsToProxy) {
		config.setProperty("domainsToProxy", getDomainsAsString(domainsToProxy));
	}

	public void setEnableCache(boolean enable) {
		config.setProperty("enableCache", enable);
	}

	public void setEnableCssRetrieving(boolean bool) {
		config.setProperty("enableCssRetrieving", bool);
	}

	public void setEnableCssRewriting(boolean bool) {
		config.setProperty("enableCssRewriting", bool);
	}

	public void setEnableUrlRewriting(boolean bool) {
		config.setProperty("enableUrlRewriting", bool);
	}

	public void setForcePageCaching(boolean b) {
		config.setProperty("forcePageCaching", b);
	}

	public void setForceResourceCaching(boolean b) {
		config.setProperty("forceResourceCaching", b);
	}

	public void setInitUrl(URL initUrl) {
		config.setProperty("initUrl", initUrl.toExternalForm());
	}

	public void setJavascriptUrls(List<String> urls) {
		config.setProperty("javascriptUrls", urls);
	}

	public void setJsRegex(String jsRegex) {
		config.setProperty("jsRegex", jsRegex);
	}

	public void setLtpaSecretProviderClassName(String name) {
		config.setProperty("ltpaSecretProviderClassName", name);

	}

	public void setLtpaSsoAuthentication(boolean b) {
		config.setProperty("ltpaSsoAuthentication", b);
	}

	public void setPageCachePrivate(boolean b) {
		config.setProperty("pageCachePrivate", b);
	}

	public void setPageCacheTimeout(int timeout) {
		config.setProperty("pageCacheTimeout", timeout);
	}

	public void setPortletDivId(String portletDivId) {
		config.setProperty("portletDivId", portletDivId);
	}

	public void setPortletTitle(String title) {
		config.setProperty("portletTitle", title);
	}

	public void setResourceCachePublic(boolean b) {
		config.setProperty("resourceCachePublic", b);
	}

	public void setResourceCacheTimeout(int timeout) {
		config.setProperty("resourceCacheTimeout", timeout);
	}

	public void setScriptsToDelete(List<String> urls) {
		config.setProperty("scriptsToDelete", urls);
	}

	public void setScriptsToIgnore(List<String> urls) {
		config.setProperty("scriptsToIgnore", urls);
	}

	public void setXPath(String xpath) {
		config.setProperty("xPath", xpath);
	}

	public void setXsltClipping(String xslt) {
		config.setProperty("xsltClipping", xslt);
	}

	public void setXsltTransform(String xslt) {
		config.setProperty("xsltTransform", xslt);
	}
}