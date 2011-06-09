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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import fr.ippon.wip.transformers.URLTypes;

/**
 * TODO
 * 
 * An implementation of the WIPConfiguration interface using apache commons
 * configuration based on a XML file. This class is a singleton.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class WIPConfigurationImpl implements WIPConfiguration {

	/**
	 * Apache commons configuration object to manager XML configuration.
	 */
	private XMLConfiguration config;

	/**
	 * The name of the portlet instance that use the WIPConfiguration object.
	 */
	private String instance;
	
	/**
	 * Initialize the XMLConfiguration object and save the instance name.
	 */
	public WIPConfigurationImpl(String instance) {
		try {
			URL url = getClass().getResource("/content/wip-config.xml");
			try {
				URI uri = url.toURI();
				File f = new File(uri);
				
				InputStream is = new FileInputStream(f);
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				StringWriter sw = new StringWriter();
				String line = "";
				while ((line = br.readLine()) != null) sw.write(line);
				
				String s = sw.toString();
				if (!s.contains(instance)) {
					int defaultStart = s.indexOf("<initUrl>");
					int defaultEnd = s.indexOf("</wipDefault"); 
					int insertIndex = s.indexOf("</configuration>");
					s = s.substring(0, insertIndex) 
							+ "<"+instance+">"
							+ s.substring(defaultStart, defaultEnd)
							+ "</"+instance+">"
							+ "</configuration>";
					FileWriter w = new FileWriter(f);
					BufferedWriter bw = new BufferedWriter(w);
					bw.write(s);
					bw.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			this.instance = instance;
			this.config = new XMLConfiguration(url);
			this.config.setDelimiterParsingDisabled(true);
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}
	}
	
	public WIPConfigurationImpl(String instance, String conf) {
		try {
			URL url = getClass().getResource("/content/wip-config.xml");
			try {
				URI uri = url.toURI();
				File f = new File(uri);
				
				InputStream is = new FileInputStream(f);
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				StringWriter sw = new StringWriter();
				String line = "";
				while ((line = br.readLine()) != null) sw.write(line);
				
				String s = sw.toString();
				int start = s.indexOf("<"+instance);
				start = s.indexOf("<init", start);
				int end = s.indexOf("</"+instance);
				s = s.substring(0, start)+conf+s.substring(end);
				
				FileWriter w = new FileWriter(f);
				BufferedWriter bw = new BufferedWriter(w);
				bw.write(s);
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			this.instance = instance;
			this.config = new XMLConfiguration(url);
			this.config.setDelimiterParsingDisabled(true);
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}
	}

	public void save() {
		try {
			config.save();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	public String getInstance() {
		return instance;
	}
	
	public String getConfigAsString() {
		String ret = "";
		URL url = getClass().getResource("/content/wip-config.xml");
		try {
			URI uri = url.toURI();
			File f = new File(uri);
			InputStream is = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			StringWriter sw = new StringWriter();
			String line = "";
			while ((line = br.readLine()) != null) sw.write(line);
			String s = sw.toString();
			if (s.contains(instance)) {
				int start = s.indexOf("<"+instance);
				start = s.indexOf("<initUrl>", start);
				int end = s.indexOf("</"+instance);
				ret = s.substring(start, end);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return ret;
	}
	
	// GENERAL CONFIG
	
	public void setInitUrl(URL initUrl) {
		config.setProperty(instance+".initUrl", initUrl.toExternalForm().toString());
	}

	public URL getInitUrl() {
		URL url = null;
		try {
			url = new URL(config.getString(instance+".initUrl"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
	}

	public String getInitUrlAsString() {
		return config.getString(instance+".initUrl");
	}

	public void setDomainsToProxy(List<URL> domainsToProxy) {
		config.setProperty(instance+".domainsToProxy", getDomainsAsString(domainsToProxy));
	}

	public List<URL> getDomainsToProxy() {
		List<URL> list = null;
		try {
			list = setDomainsFromString(config.getString(instance+".domainsToProxy"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public String getDomainsAsString(List<URL> domains) {
		String domainsAsString = "";
		for (URL u : domains)
			domainsAsString += u.toExternalForm().toString() + ";";
		if (domainsAsString != "")
			domainsAsString = domainsAsString.substring(0, domainsAsString.length() - 1);
		return domainsAsString;
	}

	public List<URL> setDomainsFromString(String string) throws MalformedURLException {
		ArrayList<URL> list = new ArrayList<URL>();
		if (!string.equals("")) {
			for (String s : string.split(";")) {
				list.add(new URL(s));
			}
		} else if (!getInitUrl().equals("")) {
			URL init = new URL(getInitUrl().getProtocol() + "://" + getInitUrl().getHost());
			list.add(init);
		}
		return list;
	}
	

	// CSS REWRITING CONFIG
	
	public void setCssRegex(String cssRegex) {
		config.setProperty(instance+".cssRegex", cssRegex);
	}

	public String getCssRegex() {
		return config.getString(instance+".cssRegex");
	}

	public void setPortletDivId(String portletDivId) {
		config.setProperty(instance+".portletDivId", portletDivId);
	}

	public String getPortletDivId() {
		return config.getString(instance+".portletDivId");
	}

	public String getCustomCss() {
		return config.getString(instance+".customCss");
	}

	public void setCustomCss(String customCss) {
		config.setProperty(instance+".customCss", customCss);
	}

	public void setAddPrefix(boolean b) {
		config.setProperty(instance+".addPrefix", b);
	}
	
	public boolean getAddPrefix() {
		return config.getBoolean(instance+".addPrefix");
	}
	
	public void setAbsolutePositioning(boolean b) {
		config.setProperty(instance+".absolutePositioning", b);
	}
	
	public boolean getAbsolutePositioning() {
		return config.getBoolean(instance+".absolutePositioning");
	}

	// JS REWRITING CONFIG
	
	public void setJsRegex(String jsRegex) {
		config.setProperty(instance+".jsRegex", jsRegex);
	}

	public String getJsRegex() {
		return config.getString(instance+".jsRegex");
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, URLTypes> getJavascriptUrls() {
		Map<String, URLTypes> map = new HashMap<String, URLTypes>();
		List<String> l = config.getList(instance+".javascriptUrls");
		if (l.size() == 1 && l.get(0).equals("")) l = new ArrayList<String>();
		for (int i = 0; i<l.size(); i++) {
			String tmp[] = l.get(i).split("::::");
			if (tmp.length == 2) {
				String url = tmp[0];
				URLTypes type = URLTypes.valueOf(tmp[1]);
				map.put(url, type);
			}
		}
		return map;
	}
	
	public void setJavascriptUrls(List<String> urls) {
		config.setProperty(instance+".javascriptUrls", urls);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getScriptsToIgnore() {
		List<String> l = config.getList(instance+".scriptsToIgnore");
		if (l.size() == 1 && l.get(0).equals("")) l = new ArrayList<String>();
		return l;
	}
	
	public void setScriptsToIgnore(List<String> urls) {
		config.setProperty(instance+".scriptsToIgnore", urls);
	}

	
	// CLIPPING CONFIG
	
	public String getXPath() {
		return config.getString(instance+".xPath");
	}
	
	public void setXPath(String xpath) {
		config.setProperty(instance+".xPath", xpath);
	}

	public String getClippingType() {
		return config.getString(instance+".clippingType");
	}

	public void setClippingType(String type) {
		config.setProperty(instance+".clippingType", type);
	}

	public String getXsltClipping() {
		String result = "";
		if (!config.getString(instance+".xsltClipping").equals("")) {
			result = config.getString(instance+".xsltClipping");
		} else {
			try {
				BufferedInputStream is = (BufferedInputStream) getClass().getResource("/xslt/clipping.xslt").getContent();
				StringWriter writer = new StringWriter();
				InputStreamReader streamReader = new InputStreamReader(is);
				BufferedReader buffer = new BufferedReader(streamReader);
				String line = "";
				while ((line=buffer.readLine())!=null){
					writer.write(line + "\n"); 
				}
				result = writer.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public void setXsltClipping(String xslt) {
		config.setProperty(instance+".xsltClipping", xslt);
	}

	public String getXsltTransform() {
		String result = "";
		if (!config.getString(instance+".xsltTransform").equals("")) {
			result = config.getString(instance+".xsltTransform");
		} else {
			try {
				BufferedInputStream is = new BufferedInputStream(getClass().getResource("/xslt/default.xslt").openConnection().getInputStream());
				StringWriter writer = new StringWriter();
				InputStreamReader streamReader = new InputStreamReader(is);
				BufferedReader buffer = new BufferedReader(streamReader);
				String line = "";
				while ((line=buffer.readLine())!=null){
					writer.write(line + "\n"); 
				}
				result = writer.toString();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public void setXsltTransform(String xslt) {
		config.setProperty(instance+".xsltTransform", xslt);
	}

	public boolean getEnableUrlRewriting() {
		return config.getBoolean(instance+".enableUrlRewriting");
	}

	public void setEnableUrlRewriting(boolean bool) {
		config.setProperty(instance+".enableUrlRewriting", bool);	
	}
	
	public boolean getEnableCssRetrieving() {
		return config.getBoolean(instance+".enableCssRetrieving");
	}

	public void setEnableCssRetrieving(boolean bool) {
		config.setProperty(instance+".enableCssRetrieving", bool);	
	}
	
	public boolean getEnableCssRewriting() {
		return config.getBoolean(instance+".enableCssRewriting");
	}

	public void setEnableCssRewriting(boolean bool) {
		config.setProperty(instance+".enableCssRewriting", bool);	
	}
	
	
	// CACHE CONFIGURATION
	
	public boolean getEnableCache() {
		return config.getBoolean(instance+".enableCache");
	}
	
	public void setEnableCache(boolean enable) {
		config.setProperty(instance+".enableCache", enable);
	}
	
	public boolean getPageCachePrivate() {
		return config.getBoolean(instance+".pageCachePrivate");
	}
	
	public void setPageCachePrivate(boolean b) {
		config.setProperty(instance+".pageCachePrivate", b);
	}
	
	public boolean getResourceCachePublic() {
		return config.getBoolean(instance+".resourceCachePublic");
	}
	
	public void setResourceCachePublic(boolean b) {
		config.setProperty(instance+".resourceCachePublic", b);
	}
	
	public boolean getForcePageCaching() {
		return config.getBoolean(instance+".forcePageCaching");
	}
	
	public void setForcePageCaching(boolean b) {
		config.setProperty(instance+".forcePageCaching", b);
	}
	
	public boolean getForceResourceCaching() {
		return config.getBoolean(instance+".forceResourceCaching");
	}
	
	public void setForceResourceCaching(boolean b) {
		config.setProperty(instance+".forceResourceCaching", b);
	}
	
	public int getPageCacheTimeout() { 
		return config.getInt(instance+".pageCacheTimeout");
	}
	
	public void setPageCacheTimeout(int timeout) {
		config.setProperty(instance+".pageCacheTimeout", timeout);
	}
	
	public int getResourceCacheTimeout() { 
		return config.getInt(instance+".resourceCacheTimeout");
	}
	
	public void setResourceCacheTimeout(int timeout) {
		config.setProperty(instance+".resourceCacheTimeout", timeout);
	}
	
	public int getCacheDateRate() {
		return config.getInt(instance+".cacheDateRate");
	}

	public void setLtpaSsoAuthentication(boolean b) {
		config.setProperty(instance+".ltpaSsoAuthentication", b);
	}

	public boolean getLtpaSsoAuthentication() {
		return config.getBoolean(instance+".ltpaSsoAuthentication");
	}

	public void setLtpaSecretProviderClassName(String name) {
		config.setProperty(instance+".ltpaSecretProviderClassName", name);
		
	}

	public String getLtpaSecretProviderClassName() {
		return config.getString(instance+".ltpaSecretProviderClassName");
	}

	public void setCredentialProviderClassName(String name) {
		config.setProperty(instance+".credentialProviderClassName", name);
	}

	public String getCredentialProviderClassName() {
		return config.getString(instance+".credentialProviderClassName");
	}

}