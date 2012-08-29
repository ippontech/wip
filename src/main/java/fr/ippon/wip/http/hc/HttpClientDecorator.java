/*
 *	Copyright 2010,2011 Ippon Technologies 
 *  
 *	This file is part of Web Integration Portlet (WIP).
 *	Web Integration Portlet (WIP) is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Web Integration Portlet (WIP) is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Web Integration Portlet (WIP).  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ippon.wip.http.hc;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Stopwatch;

import fr.ippon.wip.config.WIPConfiguration;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class uses the decorator pattern to inject preProcessor (HttpRequestInterceptor)
 * and postProcessor (HttpResponseInterceptor) to the backend HttpClient instance passed
 * to the constructor
 *
 * @author François Prot
 */
public class HttpClientDecorator implements HttpClient {
	
	private static final Logger LOG = Logger.getLogger(HttpClientDecorator.class.getName());
	
	private static final HttpResponse NotFoundResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_FOUND, "Deleted by WIP");
	
    private final HttpClient backend;

    private final List<HttpRequestInterceptor> preProcessors = new LinkedList<HttpRequestInterceptor>();
    private final List<HttpResponseInterceptor> postProcessors = new LinkedList<HttpResponseInterceptor>();

    public static ThreadLocal<Long> timeProcess = new ThreadLocal<Long>();
    
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
//        	WIPConfiguration configuration = (WIPConfiguration) context.getAttribute("WIP_CONFIGURATION");
//        	
//        	HttpRequestBase base = (HttpRequestBase) request;
//        	if(isDeletedScript(configuration, base.getURI().toString()))
//        		return NotFoundResponse;
        	
            for (HttpRequestInterceptor preProcessor : preProcessors) {
                preProcessor.process(request, context);
            }
            
            Stopwatch stopwatch = new Stopwatch().start();
            HttpResponse response = backend.execute(target, request, context);
            timeProcess.set(stopwatch.elapsedMillis());
            
            for (HttpResponseInterceptor postProcessor : postProcessors) {
                postProcessor.process(response, context);
            }
            
            return response;
        } catch (HttpException he) {
            throw new RuntimeException(he);
        }
    }
    
	/**
	 * Check if the script from the given URL has to be deleted
	 * 
	 * @param url
	 *            the script URL
	 * @return a boolean indicating if the script has to be deleted
	 */
	private boolean isDeletedScript(WIPConfiguration configuration, String url) {
		for (String regex : configuration.getScriptsToDelete()) {
			try {
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(url);
				if (m.find())
					return true;
			} catch (PatternSyntaxException e) {
				LOG.log(Level.WARNING, "Could not parse deletedScript regex: ", e);
			}
		}

		return false;
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
