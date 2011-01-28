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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;

/**
 * Interface to manage WIP cookie mechanisms.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public interface CookiesManager {

	/**
	 * Add cookies to request headers before executing the HTTP method. Get
	 * cookies from the cookies map, check expiry date, check path and possibly
	 * add it.
	 * @param id the user id
	 * @param url the targeted URL
	 * @param method the HTTP method
	 * @throws MalformedURLException
	 */
	public void setCookies(String id, String url, HttpMethod method) throws MalformedURLException;

	/**
	 * Save cookies after executing the method. Look for Set-Cookie header, get
	 * cookie data (name, value, domain...) and save it in cookies map.
	 * @param id the user id
	 * @param h the headers list
	 */
	public void saveCookies(String id, Header[] h);

	/**
	 * Return true if the user specified by the given id has one or more cookies
	 * linked to his session.
	 * @param id The user id
	 */
	public boolean hasCookies(String id);
	
	/**
	 * Clear the cookies list linked to the session of the user specified 
	 * by the given id.
	 * @param id The user id
	 */
	public void clearCookies(String id);

}
