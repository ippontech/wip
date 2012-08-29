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

package fr.ippon.wip.http.request;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import fr.ippon.wip.portlet.WIPortlet;

/**
 * Abstract container class for all data describing an HTTP request from a remote host.
 * 
 * @author François Prot
 * @author Yohan Legat
 */
public abstract class AbstractRequestBuilder implements RequestBuilder {

	protected String requestedURL;

	protected HttpMethod httpMethod;

	protected ResourceType resourceType;

	protected Multimap<String, String> parameterMap;
	
	private static final Predicate<String> filterMapPredicate = new Predicate<String>() {
		
		public boolean apply(String key) {
			return !key.startsWith(WIPortlet.WIP_REQUEST_PARAMS_PREFIX_KEY);
		}
	};

	protected AbstractRequestBuilder(String url, HttpMethod httpMethod, ResourceType resourceType, Multimap<String, String> parameterMap) {
		this.requestedURL = url;
		this.httpMethod = httpMethod;
		this.resourceType = resourceType;
		copyParameters(parameterMap);
	}

	/**
	 * Copy parameters and exclude those prefixed by
	 * WIPortlet.WIP_REQUEST_PARAMS_PREFIX_KEY
	 * 
	 * @param parameterMap
	 */
	private void copyParameters(Multimap<String, String> parameterMap) {
		if (parameterMap == null || parameterMap.size() == 0)
			return;

		this.parameterMap = Multimaps.filterKeys(parameterMap, filterMapPredicate);
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public String getRequestedURL() {
		return requestedURL;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}
}
