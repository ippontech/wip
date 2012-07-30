package fr.ippon.wip.http.request;

import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpRequestBase;

public class RequestDecorator implements Request {

	private Request decorated;
	
	public RequestDecorator(Request decorated) {
		this.decorated = decorated;
	}
	
	public HttpRequestBase buildHttpRequest() throws URISyntaxException {
		return decorated.buildHttpRequest();
	}

	public HttpMethod getHttpMethod() {
		return decorated.getHttpMethod();
	}

	public String getRequestedURL() {
		return decorated.getRequestedURL();
	}

	public ResourceType getResourceType() {
		return decorated.getResourceType();
	}
}
