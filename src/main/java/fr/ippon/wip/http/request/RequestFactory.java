package fr.ippon.wip.http.request;

import java.security.InvalidParameterException;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import org.apache.commons.fileupload.portlet.PortletFileUpload;

import fr.ippon.wip.portlet.WIPortlet;

import static fr.ippon.wip.http.request.Request.ResourceType;
import static fr.ippon.wip.http.request.Request.HttpMethod;

/**
 * A factory for building Request instance.
 * 
 * @author Yohan Legat
 *
 */
public enum RequestFactory {

	/**
	 * The singleton instance
	 */
	INSTANCE;

	/**
	 * Return a request instance. The request type will be PostRequest if the resource type is POST, GetRequest
	 * otherwise.
	 * 
	 * @param portletRequest
	 * @return
	 */
	public Request getRequest(PortletRequest portletRequest) {
		boolean isMultipart = false;
		if (portletRequest instanceof ActionRequest)
			isMultipart = PortletFileUpload.isMultipartContent((ActionRequest) portletRequest);

		if (isMultipart)
			throw new InvalidParameterException("Multipart forms are not managed yet.");

		ResourceType resourceType = ResourceType.valueOf(portletRequest.getParameter(WIPortlet.RESOURCE_TYPE_KEY));

		String requestedURL = portletRequest.getParameter(WIPortlet.LINK_URL_KEY);
		String urlConcat = portletRequest.getParameter(WIPortlet.URL_CONCATENATION_KEY);
		if (urlConcat != null) {
			requestedURL += urlConcat;
		}

		HttpMethod httpMethod;
		if (portletRequest.getParameter(WIPortlet.METHOD_TYPE) != null)
			httpMethod = HttpMethod.valueOf(portletRequest.getParameter(WIPortlet.METHOD_TYPE).toUpperCase());
		else
			httpMethod = HttpMethod.GET;

		return getRequest(requestedURL, resourceType, httpMethod, portletRequest.getParameterMap());
	}

	/**
	 * Return a request instance. The request type will be PostRequest if the resource type is POST, GetRequest otherwise.
	 * 
	 * @param requestedURL
	 *            request url
	 * @param resourceType
	 *            resource type, if any
	 * @param httpMethod
	 *            http method, if any
	 * @param parameterMap
	 *            parameters map, if any
	 * @return a implementation of Request
	 */
	public Request getRequest(String requestedURL, ResourceType resourceType, HttpMethod httpMethod, Map<String, String[]> parameterMap) {
		if (httpMethod == HttpMethod.POST)
			return new PostRequest(requestedURL, resourceType, parameterMap);
		else
			return new GetRequest(requestedURL, resourceType, parameterMap);
	}
}
