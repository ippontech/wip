/*
 *	Copyright 2010,2011 Ippon Technologies 
 *  
 *	This file is part of Wip Portlet.
 *	Wip Portlet is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Wip Portlet is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Wip Portlet.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ippon.wip.util;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.httpclient.Cookie;

/**
 * Wip Utility Class
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */

public class WIPUtil {
	
	public static String getTmpPath(URL url) {
		String surl = url.toExternalForm();
		if (!surl.startsWith("/")) {
			String[] aux = surl.split(":");
			surl = aux[1];
		}
		return surl;
	}
	
	public static Date getDate(String s) {
		Date date = null;
		if (s != null) {
			try {
				date = (new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z")).parse(s);
			} catch (ParseException e) {
				try {
					date = (new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US)).parse(s);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		}
		return date;
	}
	
	public static Cookie[] toArray(List<Cookie> l) {
		Cookie[] c = new Cookie[l.size()];
		for (int i=0; i<l.size(); i++)
			c[i] = l.get(i);
		return c;
	}
}
