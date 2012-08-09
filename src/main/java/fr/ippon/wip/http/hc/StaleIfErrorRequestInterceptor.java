package fr.ippon.wip.http.hc;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

/**
 * An interceptor for adding a stale-if-error parameter to the current request.
 * 
 * @author Yohan Legat
 *
 */
public class StaleIfErrorRequestInterceptor implements HttpRequestInterceptor {

	private int time;
	
	public StaleIfErrorRequestInterceptor(int time) {
		this.time = time;
	}
	
	public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
		request.addHeader(HttpHeaders.CACHE_CONTROL, "stale-if-error=" + time);
	}

}
