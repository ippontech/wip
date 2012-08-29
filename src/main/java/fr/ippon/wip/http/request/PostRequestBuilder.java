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

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;

import com.google.common.collect.Multimap;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Container class for all data describing a POST HTTP request from a remote host.
 * 
 * @author Yohan Legat
 */
public class PostRequestBuilder extends AbstractRequestBuilder implements RequestBuilder, Serializable {

	/**
	 * Gets url, methodType, resourceType and optional parameters from
	 * portletRequest to create a new Request instance
	 * 
	 * @param portletRequest
	 */
	protected PostRequestBuilder(String url, ResourceType resourceType, Multimap<String, String> parameterMap) {
		super(url, HttpMethod.POST, resourceType, parameterMap);
	}

	public HttpRequestBase buildHttpRequest() throws URISyntaxException {
		URI uri = new URI(getRequestedURL());
		HttpPost postRequest = new HttpPost(uri);
		if (parameterMap == null)
			return postRequest;

		List<NameValuePair> httpParams = new LinkedList<NameValuePair>();
		for (Map.Entry<String, String> entry : parameterMap.entries())
			httpParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));

		HttpEntity formEntity = new UrlEncodedFormEntity(httpParams, ContentType.APPLICATION_FORM_URLENCODED.getCharset());
		postRequest.setEntity(formEntity);

		return postRequest;
	}
}
