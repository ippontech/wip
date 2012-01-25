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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The WIPConfiguration manager stores the WIPConfiguration object 
 * associated with the different portlet instances. It also manages 
 * the store and the retrieve of configurations.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class WIPConfigurationManager {

	/**
	 * The WIPConfigurationManager singleton.
	 */
	private static WIPConfigurationManager instance = null;
	
	/**
	 * A map that contains the configurations associated to the different intances.
	 */
	private Map<String, WIPConfiguration> wipConfigurations;
	
	/**
	 * A list that contains the name of the configurations that have been saved.
	 */
	private List<String> savedConfigurations;
	
	/**
	 * Get the WIPConfigurationManager singleton.
	 * @return the class singleton
	 */
	public static synchronized WIPConfigurationManager getInstance() {
		if (instance == null)
			instance = new WIPConfigurationManager();
		return instance;
	}
	
	/**
	 * The private constructor that initializes the singleton.
	 */
	private WIPConfigurationManager() {
		wipConfigurations = new HashMap<String, WIPConfiguration>();
		savedConfigurations = new ArrayList<String>();
		
		//get existing config name
		URL url = getClass().getResource("/content/saved-config.xml");
		try {
			URI uri = url.toURI();
			File f = new File(uri);
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f);
			NodeList lst = doc.getFirstChild().getChildNodes();
			
			for(int i = 0; i < lst.getLength(); i++){
				Node n = lst.item(i);
				if(n.getNodeType() == Node.ELEMENT_NODE)
					savedConfigurations.add(n.getNodeName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the WIPConfiguration associated with the given instance name.
	 * @param instance the portlet instance name as a key to retrieve the configuration
	 * @return the configuration associated with the given instance name
	 */
	public WIPConfiguration getConfiguration(String instance) {
		WIPConfiguration config = wipConfigurations.get(instance);
		if (config == null) { 
			config = new WIPConfigurationImpl(instance);
			wipConfigurations.put(instance, config);
		}
		return config;
	}
	
	/**
	 * Get the list of names of the saved configurations.
	 * @return the list of names
	 */
	public List<String> getSavedConfigurations() {
		return savedConfigurations;
	}
	
	/**
	 * Get the saved configuration associated with the given name.
	 * @param name the name of the saved configuration
	 * @return the configuration as a string
	 */
	public String getSavedConfiguration(String name) {
		String ret = readSavedConfigXML();
		if (ret.contains(name)) {
			int start = ret.indexOf("<"+name);
			start = ret.indexOf("<initUrl>", start);
			int end = ret.indexOf("</"+name);
			ret = ret.substring(start, end);
		}
		return ret;
	}
	
	/**
	 * Save the configuration of the given instance under the given name.
	 * @param name the name to save the configuration
	 * @param instance the instance to save
	 */
	public void saveConfiguration(String name, String instance) {
		savedConfigurations.add(name);
		WIPConfiguration config = wipConfigurations.get(instance);
		String s = readSavedConfigXML();
		int index = s.indexOf("</configuration>");
		s = s.substring(0, index) 
				+ "<"+name+">"
				+ config.getConfigAsString()
				+"</"+name+">"
				+ s.substring(index);
		writeSavedConfigXML(s);
	}
	
	/**
	 * Load the saved configuration associated with the given name and load
	 * it on the given instance name.
	 * @param name the name of the configuration to load
	 * @param instance the instance that will receive the configuration
	 */
	public void loadConfiguration(String name, String instance) {
		String s = readSavedConfigXML();
		int start = s.indexOf("<"+name);
		start = s.indexOf("<init", start);
		int end = s.indexOf("</"+name);
		s = s.substring(start, end);
		WIPConfiguration c = new WIPConfigurationImpl(instance, s);
		wipConfigurations.put(instance, c);
	}
	
	/**
	 * Read the XML file that contains the saved configurations.
	 * @return the file content as a string
	 */
	private String readSavedConfigXML() {
		String ret = "";
		URL url = getClass().getResource("/content/saved-config.xml");
		try {
			URI uri = url.toURI();
			File f = new File(uri);
			InputStream is = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			StringWriter sw = new StringWriter();
			String line = "";
			while ((line = br.readLine()) != null) sw.write(line);
			ret = sw.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * Write the XML file that contains the saved configurations.
	 * @param s the string to write.
	 */
	private void writeSavedConfigXML(String s) {
		URL url = getClass().getResource("/content/saved-config.xml");
		try {
			URI uri = url.toURI();
			File f = new File(uri);
			FileWriter w = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(w);
			bw.write(s);
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

