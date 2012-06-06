package fr.ippon.wip.http.hc;

import org.apache.http.*;
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
 * This class uses the decorator pattern to inject preProcessor (HttpRequestInterceptor)
 * and postProcessor (HttpResponseInterceptor) to the backend HttpClient instance passed
 * to the constructor
 *
 * @author François Prot
 */
class HttpClientDecorator implements HttpClient {
    private final HttpClient backend;

    private final List<HttpRequestInterceptor> preProcessors = new LinkedList<HttpRequestInterceptor>();
    private final List<HttpResponseInterceptor> postProcessors = new LinkedList<HttpResponseInterceptor>();

    /**
     * @param backend Instance of HttpClient that shall be used to execute requests
     */
    public HttpClientDecorator(HttpClient backend) {
        this.backend = backend;
    }

    /**
     * Add a pre-processor interceptor
     * The order of execution interceptors will be the same as the order in which this method is called
     */
    public void addPreProcessor(HttpRequestInterceptor interceptor) {
        preProcessors.add(interceptor);
    }

    /**
     * Add a post-processor interceptor
     * The order of execution interceptors will be the same as the order in which this method is called
     */
    public void addPostProcessor(HttpResponseInterceptor interceptor) {
        postProcessors.add(interceptor);
    }

    /**
     * Delegates to backend instance
     */
    public HttpParams getParams() {
        return backend.getParams();
    }

    /**
     * Delegates to backend instance
     */
    public ClientConnectionManager getConnectionManager() {
        return backend.getConnectionManager();
    }

    HttpHost getHttpHost(HttpUriRequest request) {
        URI uri = request.getURI();
        return new HttpHost(uri.getAuthority());
    }

    /**
     * Calls #execute(HttpHost, HttpRequest, HttpContext)
     */
    public HttpResponse execute(HttpUriRequest request) throws IOException {
        return execute(getHttpHost(request), request, (HttpContext) null);
    }

    /**
     * Calls #execute(HttpHost, HttpRequest, HttpContext)
     */
    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException {
        return execute(getHttpHost(request), request, context);
    }

    /**
     * Calls #execute(HttpHost, HttpRequest, HttpContext)
     */
    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException {
        return execute(target, request, (HttpContext) null);
    }

    /**
     * This method:
     * <ul>
     * <li>invokes each pre-processor</li>
     * <li>delegate execution of the request to the backend HttpClient instance</li>
     * <li>invokes each post-processor</li>
     * </ul>
     */
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
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

    /**
     * Calls #execute(HttpHost, HttpRequest, ResponseHandler, HttpContext)
     */
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
        return execute(getHttpHost(request), request, responseHandler, null);
    }

    /**
     * Calls #execute(HttpHost, HttpRequest, ResponseHandler, HttpContext)
     */
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
        return execute(getHttpHost(request), request, responseHandler, context);
    }

    /**
     * Calls #execute(HttpHost, HttpRequest, ResponseHandler, HttpContext)
     */
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
        return execute(target, request, responseHandler, null);
    }

    /**
     * Calls #execute(HttpHost, HttpRequest, HttpContext) and pass response to responseHandler
     * Always consumes Entity from HttpResponse
     */
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
        HttpResponse response = execute(target, request, context);
        try {
            return responseHandler.handleResponse(response);
        } finally {
            HttpEntity entity = response.getEntity();
            if (entity != null) EntityUtils.consume(entity);
        }
    }
}
