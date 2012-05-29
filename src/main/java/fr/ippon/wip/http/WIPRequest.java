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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.portlet.PortletRequest;

import fr.ippon.wip.portlet.WIPortlet;

/**
 * The WIPRequest object contains the current URL, the method type and 
 * the parameters required to do the HTTP request. It is shared in session
 * and also used as a key to get cached response.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class WIPRequest implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Define the targeted URL.
	 */
	private String url;
	
	/**
	 * Define the HTTP method type (GET | POST)
	 */
	private String methodType;
	
	/**
	 * A map containing the request parameters.
	 */
	private Map<String, String[]> parameters;
	
	/**
	 * The current portlet request.
	 */
	private PortletRequest request;
	
	/**
	 * The birth time of this request
	 */
	private final long birthTime = System.currentTimeMillis();
	
	/**
	 * Specify if this request is associated to a resource
	 */
	private boolean isResource;

	/**
	 * Create a WIPRequest object from URL, method type and request parameters.
	 * @param url the targeted URL as a string
	 * @param request a map containing the request parameters
	 */
	public WIPRequest(String url, PortletRequest request, boolean isResource) {
		this.url = url;
		this.request = request;
		this.isResource = isResource;
		
		if (request != null)
			this.parameters = request.getParameterMap();
		
		if (request != null && request.getParameter(WIPortlet.METHOD_TYPE) != null)
			this.methodType = request.getParameter(WIPortlet.METHOD_TYPE);
		else 
			this.methodType = "GET";
	}

    public WIPRequest (WIPRequest originalRequest, String location) {
        this.url = location;
        this.request = originalRequest.getRequest();
        this.isResource = originalRequest.isResource();

        if (request != null)
            this.parameters = request.getParameterMap();

        this.methodType = "GET";
    }
	
	/**
	 * Return true if the request is associated to a resource
	 * @return
	 */
	public boolean isResource() {
		return isResource;
	}

	/**
	 * Get targeted URL.
	 * @return the targeted URL as a string
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Get the HTTP method type (get | post)
	 * @return the method type as a string
	 */
	public String getMethodType() {
		return methodType;
	}

	/**
	 * Get the request parameters.
	 * @return a map containing the request parameters
	 */
	public Map<String, String[]> getParameters() {
		return parameters;
	}
	/**
	 * Get the birth time of the request
	 * @return The creation time of the request as long
	 */
	public long getBirthTime() {
		return birthTime;
	}

	public PortletRequest getRequest() {
		return request;
	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public int hashCode() {
		int res = 0;
		res += url.hashCode();
		res += methodType.hashCode();
		if (parameters != null) {
			Iterator it = parameters.entrySet().iterator();
			while (it.hasNext()) {
				Entry e = (Entry) it.next();
				res += ((String[]) e.getValue())[0].hashCode();	
			} 
		}
		return res;
	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		boolean ret = true;
		WIPRequest r = (WIPRequest) obj;
		if (!r.getMethodType().equals(this.methodType)) {
			ret = false;
		} else if (!r.getUrl().equals(this.url)) { 
			ret = false;
		} else {
			Map<String, String[]> tmp = r.getParameters();
			if (tmp != null && this.parameters != null) {
				if (tmp.size() != this.parameters.size()) {
					ret = false;
				} else {
					Iterator it = this.parameters.entrySet().iterator();
					while (it.hasNext()) {
						Entry e = (Entry) it.next();
						String key = (String) e.getKey();
						String value = ((String[]) e.getValue())[0];
						if (!tmp.containsKey(key)) {
							return false;
						} else {
							if (!((String[]) tmp.get(key))[0].equals(value))
								return false;
						}
					}
				}
			}
		}
		return ret;
	}

}
