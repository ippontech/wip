package fr.ippon.wip.http.request;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.PortletRequest;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.portlet.PortletFileUpload;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
	
	private static final Function<String[], List<String>> arrayToListFunction = new Function<String[], List<String>>() {

		public List<String> apply(String[] input) {
			return Lists.newArrayList(input);
		}
	};
	
	/**
	 * Return a request instance. The request type will be PostRequest if the
	 * resource type is POST, GetRequest otherwise.
	 * 
	 * @param portletRequest
	 * @return
	 */
	public Request getRequest(PortletRequest portletRequest) {
		boolean isMultipart = false;
		if (portletRequest instanceof ActionRequest)
			isMultipart = PortletFileUpload.isMultipartContent((ActionRequest) portletRequest);

		// javascript client may have append data to the WIP generated URL, so we make sure we get the appropriate resource type
		final String resourceParameter = portletRequest.getParameter(WIPortlet.RESOURCE_TYPE_KEY);
		ResourceType resourceType = Iterables.filter(Lists.newArrayList(ResourceType.values()), new Predicate<ResourceType>() {

			public boolean apply(ResourceType input) {
				return resourceParameter.startsWith(input.name());
			}
			
		}).iterator().next();

		String requestedURL = portletRequest.getParameter(WIPortlet.LINK_URL_KEY);
		String urlConcat = portletRequest.getParameter(WIPortlet.URL_CONCATENATION_KEY);
		if (!Strings.isNullOrEmpty(urlConcat))
			requestedURL += urlConcat;

		HttpMethod httpMethod;
		if (isMultipart)
			httpMethod = HttpMethod.POST;
		else if (portletRequest.getParameter(WIPortlet.METHOD_TYPE) != null)
			httpMethod = HttpMethod.valueOf(portletRequest.getParameter(WIPortlet.METHOD_TYPE).toUpperCase());
		else
			httpMethod = HttpMethod.GET;

		Map<String, List<String>> map = Maps.newHashMap(Maps.transformValues(portletRequest.getParameterMap(), arrayToListFunction));
		
		return getRequest(portletRequest, requestedURL, resourceType, httpMethod, map, isMultipart);
	}

	/**
	 * Return a request instance. The request type will be PostRequest if the
	 * resource type is POST, GetRequest otherwise.
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
	private Request getRequest(PortletRequest portletRequest, String requestedURL, ResourceType resourceType, HttpMethod httpMethod, Map<String, List<String>> parameterMap, boolean isMultipart) {
		URI uri = URI.create(requestedURL);
		String query = uri.getQuery();

		if (!Strings.isNullOrEmpty(query)) {
			// hack; can't figure why separators are sometime "&" or "&amp;"...
			query = query.replaceAll("amp;", "");
			
			requestedURL = "http://" + uri.getHost() + uri.getPath();
			if (parameterMap == null)
				parameterMap = Maps.newHashMap();

			updateParameterMap(parameterMap, query);
		}

		if (isMultipart) {
			try {
				return new MultipartRequest(requestedURL, resourceType, (ActionRequest) portletRequest, parameterMap);
			} catch (FileUploadException e) {
				e.printStackTrace();
				return null;
			}
		}
		else if (httpMethod == HttpMethod.POST)
			return new PostRequest(requestedURL, resourceType, parameterMap);
		else
			return new GetRequest(requestedURL, resourceType, parameterMap);
	}

	private void updateParameterMap(Map<String, List<String>> parameterMap, String query) {
		String[] splittedElement;
		List<String> values;
		String key, value;
		
		for(String element : Splitter.on("&").split(query)) {
			splittedElement = element.split("=");
			if(splittedElement.length != 2)
				continue;
			
			key = splittedElement[0];
			value = splittedElement[1];
			values = parameterMap.get(key);
			
			if(values == null) {
				values = Lists.newArrayList();
				parameterMap.put(key,  values);
			}
			
			values.add(value);
		}
	}
	
	public Request getRequest(String requestedURL, ResourceType resourceType, HttpMethod httpMethod, Map<String, List<String>> parameterMap) {
		return getRequest(null, requestedURL, resourceType, httpMethod, parameterMap, false);
	}
}
