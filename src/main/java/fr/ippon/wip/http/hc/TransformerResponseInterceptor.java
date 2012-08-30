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
import fr.ippon.wip.http.request.RequestBuilder;
import fr.ippon.wip.transformers.*;
import fr.ippon.wip.transformers.pool.CloseableXmlReader;
import fr.ippon.wip.transformers.pool.Pool;
import fr.ippon.wip.transformers.pool.XMLReaderPool;
import fr.ippon.wip.util.WIPUtil;

import org.apache.http.*;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import com.google.common.collect.Iterables;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.xml.transform.TransformerException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of HttpResponseInterceptor in charge of processing all content
 * transformations (HTML, CSS, JavaScript) and clipping.
 * 
 * @author François Prot
 */
class TransformerResponseInterceptor implements HttpResponseInterceptor {

	private static final Logger LOG = Logger.getLogger(TransformerResponseInterceptor.class.getName());
	
	private static final Pool<CloseableXmlReader> xmlReaderPool = new XMLReaderPool(100);

	/**
	 * If httpResponse must be transformed, creates an instance of
	 * WIPTransformer, executes WIPTransformer#transform on the response content
	 * and updates the response entity accordingly.
	 * 
	 * @param httpResponse
	 * @param context
	 * @throws HttpException
	 * @throws IOException
	 */
	public void process(HttpResponse httpResponse, HttpContext context) throws HttpException, IOException {
		PortletRequest portletRequest = HttpClientResourceManager.getInstance().getCurrentPortletRequest();
		PortletResponse portletResponse = HttpClientResourceManager.getInstance().getCurrentPortletResponse();
		WIPConfiguration config = WIPUtil.getConfiguration(portletRequest);
		RequestBuilder request = HttpClientResourceManager.getInstance().getCurrentRequest();

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

		String actualURL;
		RedirectLocations redirectLocations = (RedirectLocations) context.getAttribute("http.protocol.redirect-locations");
		if (redirectLocations != null)
			actualURL = Iterables.getLast(redirectLocations.getAll()).toString();
		else if (context.getAttribute(CachingHttpClient.CACHE_RESPONSE_STATUS) == CacheResponseStatus.CACHE_HIT) {
			actualURL = request.getRequestedURL();
		} else {
			HttpRequest actualRequest = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
			HttpHost actualHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			actualURL = actualHost.toURI() + actualRequest.getRequestLine().getUri();
		}

		// Check if actual URI must be transformed
		if (!config.isProxyURI(actualURL))
			return;

		// a builder for creating a WIPTransformer instance
		TransformerBuilder transformerBuilder = new TransformerBuilder().
				setActualURL(actualURL).
				setMimeType(mimeType).
				setPortletRequest(portletRequest).
				setPortletResponse(portletResponse).
				setResourceType(request.getResourceType()).
				setXmlReaderPool(xmlReaderPool);

		// Creates an instance of Transformer depending on ResourceType and
		// MimeType
		int status = transformerBuilder.build();
		if (status == TransformerBuilder.STATUS_NO_TRANSFORMATION)
			return;

		WIPTransformer transformer = transformerBuilder.getTransformer();
		// Call WIPTransformer#transform method and update the response Entity
		// object
		try {
			String content = EntityUtils.toString(entity);
			String transformedContent = ((AbstractTransformer) transformer).transform(content);
			
			StringEntity transformedEntity;
			if (contentType.getCharset() != null) {
				transformedEntity = new StringEntity(transformedContent, contentType);
			} else {
				transformedEntity = new StringEntity(transformedContent);
			}
			transformedEntity.setContentType(contentType.toString());
			httpResponse.setEntity(transformedEntity);

		} catch (SAXException e) {
			LOG.log(Level.SEVERE, "Could not transform HTML", e);
			throw new IllegalArgumentException(e);
		} catch (TransformerException e) {
			LOG.log(Level.SEVERE, "Could not transform HTML", e);
			throw new IllegalArgumentException(e);
		}
	}
}
