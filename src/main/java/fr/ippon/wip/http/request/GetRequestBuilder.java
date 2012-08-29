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

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

import com.google.common.collect.Multimap;

/**
 * Container class for all data describing a GET HTTP request from a remote host.
 * 
 * @author Yohan Legat
 *
 */
public class GetRequestBuilder extends AbstractRequestBuilder implements RequestBuilder, Serializable {

	protected GetRequestBuilder(String url, ResourceType resourceType, Multimap<String, String> parameterMap) {
		super(url, HttpMethod.GET, resourceType, parameterMap);
	}

	public HttpRequestBase buildHttpRequest() throws URISyntaxException {
		URI encodedURI = new URI(getRequestedURL());
		URIBuilder uriBuilder = new URIBuilder(encodedURI);
		if (parameterMap != null)
			for (Map.Entry<String, String> entry : parameterMap.entries())
				uriBuilder.addParameter(entry.getKey(), entry.getValue());
		
		return new HttpGet(uriBuilder.build());
	}
}
