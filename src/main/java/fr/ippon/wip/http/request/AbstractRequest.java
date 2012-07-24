package fr.ippon.wip.http.request;

import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import fr.ippon.wip.portlet.WIPortlet;

/**
 * Abstract container class for all data describing an HTTP request from a remote host.
 * 
 * @author François Prot
 * @author Yohan Legat
 */
public abstract class AbstractRequest implements Request {

	protected String requestedURL;

	protected HttpMethod httpMethod;

	protected ResourceType resourceType;

	protected Map<String, List<String>> parameterMap;
	
	private static final Predicate<String> filterMapPredicate = new Predicate<String>() {
		
		public boolean apply(String key) {
			return !key.startsWith(WIPortlet.WIP_REQUEST_PARAMS_PREFIX_KEY);
		}
	};

	protected AbstractRequest(String url, HttpMethod httpMethod, ResourceType resourceType, Map<String, List<String>> parameterMap) {
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
	private void copyParameters(Map<String, List<String>> parameterMap) {
		if (parameterMap == null || parameterMap.size() == 0)
			return;

		this.parameterMap = Maps.filterKeys(parameterMap, filterMapPredicate);
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
