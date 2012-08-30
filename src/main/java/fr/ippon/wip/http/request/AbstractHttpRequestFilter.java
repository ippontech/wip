package fr.ippon.wip.http.request;

import java.nio.charset.Charset;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;

public abstract class AbstractHttpRequestFilter {
	
	public static final HttpResponse NOT_FOUND_RESPONSE = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_FOUND, "Deleted by WIP");
	static {
		NOT_FOUND_RESPONSE.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_PLAIN.getMimeType());
		NOT_FOUND_RESPONSE.setEntity(new StringEntity("", Charset.defaultCharset()));
	}
	
	public abstract boolean filter(HttpHost target, HttpRequest request, HttpContext context);
	
}
