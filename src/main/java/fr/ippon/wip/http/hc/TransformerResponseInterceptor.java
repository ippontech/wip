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

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.http.Request;
import fr.ippon.wip.transformers.*;
import fr.ippon.wip.util.WIPUtil;

import org.apache.http.*;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of HttpResponseInterceptor in charge of processing all content transformations (HTML, CSS, JavaScript)
 * and clipping.
 * @author François Prot
 */
class TransformerResponseInterceptor implements HttpResponseInterceptor {

    private static final Logger LOG = Logger.getLogger(TransformerResponseInterceptor.class.getName());

    /**
     * If httpResponse must be transformed, creates an instance of WIPTransformer,
     *  executes WIPTransformer#transform on the response content and updates the
     *  response entity accordingly.
     *
     * @param httpResponse
     * @param context
     * @throws HttpException
     * @throws IOException
     */
    public void process(HttpResponse httpResponse, HttpContext context) throws HttpException, IOException {
        PortletRequest portletRequest = HttpClientResourceManager.getInstance().getCurrentPortletRequest();
        PortletResponse portletResponse = HttpClientResourceManager.getInstance().getCurrentPortletResponse();
        WIPConfiguration config = WIPUtil.extractConfiguration(portletRequest);
        Request request = HttpClientResourceManager.getInstance().getCurrentRequest();

        if (httpResponse == null) {
            // No response -> no transformation
            LOG.warning("No response to transform.");
            return;
        }

        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) {
            // No entity -> no transformation
            return;
        }

        ContentType contentType = ContentType.getOrDefault(entity);
        String mimeType = contentType.getMimeType();

        // Check if actual URI must be transformed
        String actualURI;
        if(context.getAttribute(CachingHttpClient.CACHE_RESPONSE_STATUS) == CacheResponseStatus.CACHE_HIT) {
        	actualURI = request.getRequestedURL();
        } else {
	        HttpRequest actualRequest = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
	        HttpHost actualHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
	        actualURI = actualHost.toURI() + actualRequest.getRequestLine().getUri();
        }
        
        LOG.log(Level.INFO, "Processing of " + actualURI);
        if (!config.isProxyURI(actualURI)) {
            LOG.log(Level.INFO, "Response doesn't need to be transformed.");
            return;
        }
        
        // Creates an instance of Transformer depending on ResourceType and MimeType
        // May returns directly if no transformation is need
        WIPTransformer transformer = null;
        switch (request.getResourceType()) {
            // Direct link or form submit
            case HTML:
                if (!mimeType.equals("text/html") && !mimeType.equals("application/xhtml+xml")) {
                    // No transformation
                	LOG.log(Level.INFO, "Response won't be transformed.");
                    return;
                } else {
                    // HTML transformation
                    transformer = new HTMLTransformer(portletRequest, portletResponse);
                }
                break;
            case JS:
                // JavaScript transformation
                transformer = new JSTransformer(portletRequest, portletResponse);
                // Empty content
                if (((JSTransformer) transformer).isDeletedScript(actualURI)) {
                    // Send à 404 empty response
                	LOG.log(Level.INFO, "Javascript resource deleted: transformed to 404 empty response.");
                    emtpyResponse(httpResponse);
                    return;
                } else if (((JSTransformer) transformer).isIgnoredScript(actualURI)) {
                	LOG.log(Level.INFO, "Javascript response ignored.");
                    return;
                }
                break;
            case CSS:
                // CSS transformation
                transformer = new CSSTransformer(portletRequest, portletResponse);
                break;
            case AJAX:
                if (mimeType == null) {
                	LOG.log(Level.INFO, "Response won't be transformed: MIME type is null.");
                    return;
                } else if (mimeType.equals("text/html") || mimeType.equals("application/xhtml+xml")) {
                    // HTML transformation
                    transformer = new HTMLTransformer(portletRequest, portletResponse);
                } else if (mimeType.equals("text/javascript") || mimeType.equals("application/javascript")) {
                    // JavaScript transformation
                    transformer = new JSTransformer(portletRequest, portletResponse);
                } else if (mimeType.equals("application/json")) {
                    // JSON transformation
                    transformer = new JSONTransformer();
                } else {
                    // No transformation
                	LOG.log(Level.INFO, "Reponse won't be transformed.");
                    return;
                }
                break;
            case RAW:
            	LOG.log(Level.INFO, "Reponse won't be transformed.");
                // No transformation
                return;
        }
        
        // Call WIPTransformer#transform method and update the response Entity object
        try {
            String transformedContent = transformer.transform(EntityUtils.toString(entity));
            HttpEntity transformedEntity;
            if (contentType.getCharset() != null) {
                transformedEntity = new StringEntity(transformedContent, ContentType.getOrDefault(entity));
            } else {
                transformedEntity = new StringEntity(transformedContent);
            }
            httpResponse.setEntity(transformedEntity);

        } catch (SAXException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        } catch (TransformerException e) {
        	LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void emtpyResponse(HttpResponse httpResponse) {
        EntityUtils.consumeQuietly(httpResponse.getEntity());
        httpResponse.setEntity(new StringEntity("", Charset.defaultCharset()));
        httpResponse.setStatusCode(HttpStatus.SC_NOT_FOUND);
        httpResponse.setReasonPhrase("Deleted by WIP");
    }
}
