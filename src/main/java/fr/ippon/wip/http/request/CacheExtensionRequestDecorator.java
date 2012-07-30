package fr.ippon.wip.http.request;

import java.net.URISyntaxException;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpRequestBase;

public class CacheExtensionRequestDecorator extends RequestDecorator {

	private final int time;
	
	public CacheExtensionRequestDecorator(Request decorated, int time) {
		super(decorated);
		this.time = time;
	}
	
	public CacheExtensionRequestDecorator(Request decorated) {
		this(decorated, 0);
	}
	
	@Override
	public HttpRequestBase buildHttpRequest() throws URISyntaxException {
		HttpRequestBase httpRequest = super.buildHttpRequest();
		httpRequest.addHeader(HttpHeaders.CACHE_CONTROL, "stale-if-error=" + time);
		return httpRequest;
	}
}
