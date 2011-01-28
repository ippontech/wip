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

package fr.ippon.wip.http;

/**
 * This class defines static attributes and methods 
 * useful to handle status codes.
 *  
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class StatusCode {

	// OK
	public static final int OK = 200;
	
	// Not Modified
	public static final int NOT_MODIFIED = 304;
	
	// Moved permanently
	public static final int MOVED_PERMANENTLY = 301;
	
	// Moved temporarily
	public static final int MOVED_TEMPORARILY = 302;
	
	// Use proxy
	public static final int USE_PROXY = 305;
	
	
	// Temporary redirect
	public static final int TEMPORARY_REDIRECT = 307;
	
	// Unauthorized
	public static final int UNAUTHORIZED = 401;
	
	
	/**
	 * Check if the given status code matches a redirection.
	 * @param statusCode the status code to check
	 * @return true if it is a redirection code, else false
	 */
	public static boolean isRedirectionCode(int statusCode) {
		boolean isRedirection = false;
		
		if (statusCode == MOVED_PERMANENTLY 
				|| statusCode == MOVED_TEMPORARILY 
				|| statusCode == USE_PROXY 
				|| statusCode == TEMPORARY_REDIRECT)
			isRedirection = true;
		
		return isRedirection;
	}
	
}
