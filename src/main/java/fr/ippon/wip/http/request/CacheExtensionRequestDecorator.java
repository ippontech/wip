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

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * A Request decorator adding a stale-if-error header for the request
 * 
 * @author Yohan Legat
 *
 */
public class CacheExtensionRequestDecorator extends RequestDecorator {

	private final int time;
	
	public CacheExtensionRequestDecorator(Request decorated, int time) {
		super(decorated);
		this.time = time;
	}
	
	public CacheExtensionRequestDecorator(Request decorated) {
		this(decorated, 0);
	}
	
	@Override
	public HttpRequestBase buildHttpRequest() throws URISyntaxException {
		HttpRequestBase httpRequest = super.buildHttpRequest();
		httpRequest.addHeader(HttpHeaders.CACHE_CONTROL, "stale-if-error=" + time);
		return httpRequest;
	}
}
