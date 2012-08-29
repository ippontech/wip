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

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Interface for POST and GET request.
 * 
 * @author Yohan Legat
 *
 */
public interface RequestBuilder {

	public enum HttpMethod {
		GET, POST
	}

	/**
	 * Processing and transformations may change depending on ResourceType :
	 * 
	 * <ul>
	 * <li>HTML: for links and forms submitted by a direct client action</li>
	 * <li>JS: for JavaScript files</li>
	 * <li>CSS: for CSS files</li>
	 * <li>AJAX: for URL found in JavaScript content (may be used for Ajax
	 * requests or not...)</li>
	 * <li>RAW: for binary content (images, flash applications, ...)</li>
	 * </ul>
	 */
	public enum ResourceType {
		HTML, JS, CSS, AJAX, RAW
	}

	public HttpRequestBase buildHttpRequest() throws URISyntaxException;

	public HttpMethod getHttpMethod();

	public String getRequestedURL();

	public ResourceType getResourceType();

}
