package fr.ippon.wip.http.request;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpRequestBase;

/**
 * Interface for POST and GET request.
 * 
 * @author Yohan Legat
 *
 */
public interface Request {

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
