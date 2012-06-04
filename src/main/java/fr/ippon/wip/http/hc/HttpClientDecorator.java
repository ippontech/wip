package fr.ippon.wip.http.hc;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 29/05/12
 * Time: 22:52
 * To change this template use File | Settings | File Templates.
 */
public class HttpClientDecorator implements HttpClient {
    private final HttpClient backend;

    private final List<HttpRequestInterceptor> preProcessors = new LinkedList<HttpRequestInterceptor>();
    private final List<HttpResponseInterceptor> postProcessors = new LinkedList<HttpResponseInterceptor>();

    public HttpClientDecorator(HttpClient backend) {
        this.backend = backend;
    }

    public void addPreProcessor (HttpRequestInterceptor interceptor) {
        preProcessors.add(interceptor);
    }

    public void addPostProcessor (HttpResponseInterceptor interceptor) {
        postProcessors.add(interceptor);
    }

    public HttpParams getParams() {
        return backend.getParams();
    }

    public ClientConnectionManager getConnectionManager() {
        return backend.getConnectionManager();
    }

    HttpHost getHttpHost(HttpUriRequest request) {
        URI uri = request.getURI();
        return new HttpHost(uri.getAuthority());
    }

    public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
        return execute (getHttpHost(request), request, (HttpContext)null);
    }

    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
        return execute (getHttpHost(request), request, context);
    }

    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
        return execute (target, request, (HttpContext)null);
    }

    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
        try {
            for (HttpRequestInterceptor preProcessor : preProcessors) {
                preProcessor.process(request, context);
            }
            HttpResponse response = backend.execute(target, request, context);
            for (HttpResponseInterceptor postProcessor : postProcessors) {
                postProcessor.process(response, context);
            }
            return response;
        } catch (HttpException he) {
            throw new RuntimeException(he);
        }
    }

    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return execute (getHttpHost(request), request, responseHandler, null);
    }

    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        return execute (getHttpHost(request), request, responseHandler, context);
    }

    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return execute (target, request, responseHandler, null);
    }

    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        HttpResponse response = execute(target, request, context);
        try {
            return responseHandler.handleResponse(response);
        } finally {
            HttpEntity entity = response.getEntity();
            if (entity != null) EntityUtils.consume(entity);
        }
    }
}
