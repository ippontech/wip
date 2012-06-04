package fr.ippon.wip.http;

import fr.ippon.wip.portlet.WIPortlet;

import javax.portlet.*;
import java.io.Serializable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 01/06/12
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
public class Request implements Serializable {
    private String requestedURL;
    private HttpMethod httpMethod = HttpMethod.GET;
    private ResourceType resourceType;
    private Map<String, String[]> parameterMap;

    // TODO: add a RAW type (=no transformation)
    public enum ResourceType {HTML, JS, CSS, AJAX, RAW};
    public enum HttpMethod {GET, POST};

    public Request() {
    }

    public Request(PortletRequest portletRequest) {
        requestedURL = portletRequest.getParameter(WIPortlet.LINK_URL_KEY);
        String urlConcat =  portletRequest.getParameter(WIPortlet.URL_CONCATENATION_KEY);
        if (urlConcat != null) {
            requestedURL += urlConcat;
        }
        resourceType = ResourceType.valueOf(portletRequest.getParameter(WIPortlet.RESOURCE_TYPE_KEY));
        if (portletRequest.getParameter(WIPortlet.METHOD_TYPE) != null) {
            httpMethod = HttpMethod.valueOf(portletRequest.getParameter(WIPortlet.METHOD_TYPE).toUpperCase());
            parameterMap = new HashMap<String, String[]>();
            Map<String, String[]> portletParamMap = portletRequest.getParameterMap();
            if (portletParamMap != null) {
                Iterator<String> paramKeyIter = portletParamMap.keySet().iterator();
                while (paramKeyIter.hasNext()) {
                    String key = paramKeyIter.next();
                    if (key.indexOf(WIPortlet.WIP_REQUEST_PARAMS_PREFIX_KEY) != 0) {
                        parameterMap.put(key, portletParamMap.get(key));
                    }
                }
            }
        }
    }

    public String getRequestedURL() {
        return requestedURL;
    }

    public void setRequestedURL(String requestedURL) {
        this.requestedURL = requestedURL;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }
}
