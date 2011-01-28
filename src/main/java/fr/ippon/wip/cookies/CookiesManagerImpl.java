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

package fr.ippon.wip.cookies;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;

import fr.ippon.wip.util.WIPUtil;

/**
 * Implementation of the CookiesManager interface. It is a singleton that
 * defines a cookies map, indexed by user session id, to store user cookies.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class CookiesManagerImpl implements CookiesManager {
	
	private HashMap<String, List<Cookie>> cookiesMap;
	
	/**
	 * CookiesManager singleton
	 */
	private static CookiesManager instance = null;
	
	/**
	 * Get the unique instance of the class.
	 * @return the cookies manager singleton
	 */
	public static synchronized CookiesManager getInstance() {
		if (instance == null)
			instance = new CookiesManagerImpl();
		return instance;
	}
	
	/**
	 * The private constructor to instantiate the singleton.
	 */
	private CookiesManagerImpl() {
		cookiesMap = new HashMap<String, List<Cookie>>();
	}

	public void saveCookies(String id, Header[] h) {
		if (cookiesMap.get(id) == null) {
			cookiesMap.put(id, new ArrayList<Cookie>());
		}
		for (int i=0; i<h.length; i++) {
			if (h[i].getName().compareTo("Set-Cookie") == 0) {
				String value = h[i].getValue();
				String[] cookie = value.split(";");
				Map<String, String> m = new HashMap<String, String>();
				String[] aux = cookie[0].split("=");
				m.put("name", aux[0]);
				m.put("value", aux[1]);
				m.put("secure", "false");
				for (int j=1; j<cookie.length; j++) {
					if (cookie[j].toUpperCase().equals("SECURE"))
						m.put("secure", "true");
					else if (!cookie[j].equals("")) {
						aux = cookie[j].split("=");
						if (aux.length>1) m.put(aux[0].toLowerCase(), aux[1]); else m.put(aux[0].toLowerCase(), "");
					}
				}
				cookiesMap.get(id).add(new Cookie(m.get("domain"), m.get("name"), m.get("value"), m.get("path"), WIPUtil.getDate(m.get("expires")), Boolean.parseBoolean(m.get("secure"))));
			}
		}		
	}

	public void setCookies(String id, String url, HttpMethod method) throws MalformedURLException {
		List<Cookie> c = cookiesMap.get(id);
		if (c != null) {
			for (int i=0; i<c.size(); i++) {
				// Checking expiration date
				Date date = c.get(i).getExpiryDate();
				if (date != null && date.before(new Date())) {
					c.remove(i);
				} else {
					// Checking path
					String path = c.get(i).getPath();
					if (path == null) {
						method.setRequestHeader("Cookie", c.get(i).getName()+"="+c.get(i).getValue());
					} else {
						URL u = new URL(url);
						String baseUrl = u.getProtocol() + "://" + u.getHost() + path;
						if (url.startsWith(baseUrl)) 
							method.setRequestHeader("Cookie", c.get(i).getName()+"="+c.get(i).getValue());
					}
				}
			}
		}		
	}

	public void clearCookies(String id) {
		if (cookiesMap.get(id) != null) {
			cookiesMap.get(id).clear();
		}		
	}

	public boolean hasCookies(String id) {
		if ((cookiesMap.get(id) == null)||(cookiesMap.get(id).isEmpty()))
			return false;
		else 
			return true;
	}

}
