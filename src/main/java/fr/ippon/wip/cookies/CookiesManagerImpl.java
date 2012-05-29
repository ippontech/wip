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

import fr.ippon.wip.util.WIPUtil;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.SM;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.NetscapeDraftHeaderParser;

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

	public void saveCookies(String id, Header[] headers) {
        List<Cookie> cookies = cookiesMap.get(id);
		if (cookies == null) {
            cookies = new ArrayList<Cookie>();
			cookiesMap.put(id, cookies);
		}
		for (Header header : headers) {
			if (header.getName().compareToIgnoreCase(SM.SET_COOKIE) == 0) {
                for (HeaderElement element : header.getElements()) {
                    BasicClientCookie cookie = new BasicClientCookie(element.getName(), element.getValue());
                    for (NameValuePair nvPair : element.getParameters()) {
                          String attName = nvPair.getName();
                          if (attName.compareToIgnoreCase("secure") == 0) {
                              cookie.setSecure (true);
                          } else if (attName.compareToIgnoreCase("expires") == 0) {
                              cookie.setExpiryDate(WIPUtil.getDate(nvPair.getValue()));
                          } else if (attName.compareToIgnoreCase("domain") == 0) {
                              cookie.setDomain(nvPair.getValue());
                          } else if (attName.compareToIgnoreCase("path") == 0) {
                              cookie.setPath(nvPair.getValue());
                          }
                    }
                    cookies.add(cookie);
                }
			}
		}		
	}

	public void saveSingleCookie(String id, String cookieString) {
		if (cookiesMap.get(id) == null) {
			cookiesMap.put(id, new ArrayList<Cookie>());
		}
		Map<String, String> m = new HashMap<String, String>();
		String[] sCookie = cookieString.split(";");
		int index = sCookie[0].indexOf("=", 0);
		if (index > 0) {
			m.put("name", sCookie[0].substring(0, index));
			m.put("value", sCookie[0].substring(index+1));
			m.put("secure", "false");
			for (int j=1; j<sCookie.length; j++) {
				if (sCookie[j].toUpperCase().equals("SECURE"))
					m.put("secure", "true");
				else if (!sCookie[j].equals("")) {
					String[] aux = sCookie[j].split("=");
					if (aux.length>1) 
						m.put(aux[0].toLowerCase(), aux[1]); 
					else 
						m.put(aux[0].toLowerCase(), "");
				}
			}
            BasicClientCookie cookie = new BasicClientCookie(m.get("name"), m.get("value"));
            cookie.setDomain(m.get("domain"));
            cookie.setPath(m.get("path"));
            cookie.setExpiryDate(WIPUtil.getDate(m.get("expires")));
            cookie.setSecure(Boolean.parseBoolean(m.get("secure")));
			cookiesMap.get(id).add(cookie);
		}
	}
	
	public void setCookies(String id, String url, HttpRequest request) throws MalformedURLException {
        List<Cookie> cookies = cookiesMap.get(id);
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // Check expiry date
                if (cookie.getExpiryDate() != null && cookie.getExpiryDate().before(new Date())) {
                    cookies.remove(cookie);
                    continue;
                }
                // Check path
                if (cookie.getPath() != null) {
                    URL u = new URL(url);
                    String baseUrl = u.getProtocol() + "://" + u.getHost() + cookie.getPath();
                    if (!url.startsWith(baseUrl))
                        continue;
                }
                // Check secure
                if (cookie.isSecure() && !url.startsWith("https")) {
                    continue;
                }
                request.addHeader(SM.COOKIE, cookie.getName() + "=" + cookie.getValue());
            }
        }
	}

	public void clearCookies(String id) {
		if (cookiesMap.get(id) != null) {
			cookiesMap.remove(id);
		}		
	}

	public boolean hasCookies(String id) {
		if ((cookiesMap.get(id) == null)||(cookiesMap.get(id).isEmpty()))
			return false;
		else 
			return true;
	}

}
