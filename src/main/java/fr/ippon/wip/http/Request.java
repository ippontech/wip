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

import fr.ippon.wip.portlet.WIPortlet;

import javax.portlet.PortletRequest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Container class for all data describing an HTTP request from a remote host

 * @author François Prot
 */
public class Request implements Serializable {
    private String requestedURL;
    private HttpMethod httpMethod = HttpMethod.GET;
    private ResourceType resourceType;
    private Map<String, String[]> parameterMap;

    /**
     * Processing and transformations may change depending on ResourceType :
     *
     * <ul>
     *     <li>HTML: for links and forms submitted by a direct client action</li>
     *     <li>JS: for JavaScript files</li>
     *     <li>CSS: for CSS files</li>
     *     <li>AJAX: for URL found in JavaScript content (may be used for Ajax requests or not...)</li>
     *     <li>RAW: for binary content (images, flash applications, ...)</li>
     * </ul>
     */
    public enum ResourceType {
        HTML, JS, CSS, AJAX, RAW
    }

    public enum HttpMethod {GET, POST}

    public Request(String url, HttpMethod httpMethod, ResourceType resourceType, Map<String, String[]> parameterMap) {
        this.requestedURL = url;
        this.httpMethod = httpMethod;
        this.resourceType = resourceType;
        copyParameters(parameterMap);
    }

    /**
     * Gets url, methodType, resourceType and optional parameters from portletRequest to create a new Request instance
     * @param portletRequest
     */
    public Request(PortletRequest portletRequest) {
        // TODO: Manage Content-Type:multipart/form-data
        requestedURL = portletRequest.getParameter(WIPortlet.LINK_URL_KEY);
        String urlConcat = portletRequest.getParameter(WIPortlet.URL_CONCATENATION_KEY);
        if (urlConcat != null) {
            requestedURL += urlConcat;
        }
        resourceType = ResourceType.valueOf(portletRequest.getParameter(WIPortlet.RESOURCE_TYPE_KEY));
        if (portletRequest.getParameter(WIPortlet.METHOD_TYPE) != null) {
            httpMethod = HttpMethod.valueOf(portletRequest.getParameter(WIPortlet.METHOD_TYPE).toUpperCase());
        }
        copyParameters(portletRequest.getParameterMap());
    }

    public String getRequestedURL() {
        return requestedURL;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    private void copyParameters (Map<String, String[]> parameterMap) {
        // Copy parameters and exclude those prefixed by WIPortlet.WIP_REQUEST_PARAMS_PREFIX_KEY
        if (parameterMap != null && parameterMap.size() > 0) {
            this.parameterMap = new HashMap<String, String[]>();
            Iterator<String> paramKeyIter = parameterMap.keySet().iterator();
            while (paramKeyIter.hasNext()) {
                String key = paramKeyIter.next();
                if (key.indexOf(WIPortlet.WIP_REQUEST_PARAMS_PREFIX_KEY) != 0) {
                    this.parameterMap.put(key, parameterMap.get(key));
                }
            }
        }
    }
}
