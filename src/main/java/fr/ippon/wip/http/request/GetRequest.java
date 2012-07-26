package fr.ippon.wip.http.request;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

/**
 * Container class for all data describing a GET HTTP request from a remote host.
 * 
 * @author Yohan Legat
 *
 */
public class GetRequest extends AbstractRequest implements Request, Serializable {

	protected GetRequest(String url, ResourceType resourceType, Map<String, List<String>> parameterMap) {
		super(url, HttpMethod.GET, resourceType, parameterMap);
	}

	public HttpRequestBase buildHttpRequest() throws URISyntaxException {
		URI encodedURI = new URI(getRequestedURL());
		URIBuilder uriBuilder = new URIBuilder(encodedURI);
		if (parameterMap != null)
			for (Map.Entry<String, List<String>> entry : parameterMap.entrySet())
				for (String value : entry.getValue())
					uriBuilder.addParameter(entry.getKey(), value);
		
		return new HttpGet(uriBuilder.build());
	}
}
