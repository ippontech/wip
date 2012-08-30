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

package fr.ippon.wip.transformers;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import fr.ippon.wip.http.request.RequestBuilder.ResourceType;
import fr.ippon.wip.transformers.pool.CloseableXmlReader;
import fr.ippon.wip.transformers.pool.Pool;

/**
 * A builder for building transformers.
 * 
 * @author Yohan Legat
 *
 */
public class TransformerBuilder {

	private String mimeType;

	private ResourceType resourceType;

	private PortletRequest portletRequest;

	private PortletResponse portletResponse;

	private String actualURL;

	private Pool<CloseableXmlReader> xmlReaderPool;

	private int status;

	private WIPTransformer transformer;

	public static final int STATUS_TRANSFORMATION = 0;

	public static final int STATUS_NO_TRANSFORMATION = 1;

	public TransformerBuilder() {
	}

	public int build() {
		status = STATUS_TRANSFORMATION;
		transformer = null;

		switch (resourceType) {
		// Direct link or form submit
		case HTML:
			transformer = buildHtmlTransformer();
			break;
		case JS:
			transformer = buildJavascriptTransformer();
			break;
		case CSS:
			transformer = buildCSSTransformer();
			break;
		case AJAX:
			transformer = buildAJAXTransformer();
			break;
		case RAW:
			status = STATUS_NO_TRANSFORMATION;
			break;
		default:
			status = STATUS_NO_TRANSFORMATION;
			break;
		}

		return status;
	}

	private WIPTransformer buildAJAXTransformer() {
		if (mimeType == null) {
			status = STATUS_NO_TRANSFORMATION;
			return null;
		}

		// HTML transformation
		if (mimeType.equals("text/html") || mimeType.equals("application/xhtml+xml"))
			return new HTMLTransformer(portletRequest, portletResponse, actualURL, xmlReaderPool.acquire());

		// JavaScript transformation
		if (mimeType.equals("text/javascript") || mimeType.equals("application/javascript"))
			return new JSTransformer(portletRequest, portletResponse, actualURL);

		// JSON transformation
		if (mimeType.equals("application/json"))
			return new JSONTransformer(portletRequest, actualURL);

		status = STATUS_NO_TRANSFORMATION;
		return null;
	}

	private WIPTransformer buildCSSTransformer() {
		return new CSSTransformer(portletRequest, portletResponse, actualURL);
	}

	private WIPTransformer buildHtmlTransformer() {
		// No transformation
		if (!mimeType.equals("text/html") && !mimeType.equals("application/xhtml+xml")) {
			status = STATUS_NO_TRANSFORMATION;
			return null;
		}

		// HTML transformation
		return new HTMLTransformer(portletRequest, portletResponse, actualURL, xmlReaderPool.acquire());
	}

	private WIPTransformer buildJavascriptTransformer() {
		// JavaScript transformation
		JSTransformer transformer = new JSTransformer(portletRequest, portletResponse, actualURL);

		if (((JSTransformer) transformer).isIgnoredScript(actualURL)) {
			status = STATUS_NO_TRANSFORMATION;
			return null;
		}

		return transformer;
	}

	public String getActualURL() {
		return actualURL;
	}

	public String getMimeType() {
		return mimeType;
	}

	public PortletRequest getPortletRequest() {
		return portletRequest;
	}

	public PortletResponse getPortletResponse() {
		return portletResponse;
	}

	public ResourceType getResourceType() {
		return resourceType;
	}

	public int getStatus() {
		return status;
	}

	public WIPTransformer getTransformer() {
		return transformer;
	}

	public Pool<CloseableXmlReader> getXmlReaderPool() {
		return xmlReaderPool;
	}

	public TransformerBuilder setActualURL(String actualURL) {
		this.actualURL = actualURL;
		return this;
	}

	public TransformerBuilder setMimeType(String mimeType) {
		this.mimeType = mimeType;
		return this;
	}

	public TransformerBuilder setPortletRequest(PortletRequest portletRequest) {
		this.portletRequest = portletRequest;
		return this;
	}

	public TransformerBuilder setPortletResponse(PortletResponse portletResponse) {
		this.portletResponse = portletResponse;
		return this;
	}

	public TransformerBuilder setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
		return this;
	}

	public void setTransformer(WIPTransformer transformer) {
		this.transformer = transformer;
	}

	public TransformerBuilder setXmlReaderPool(Pool<CloseableXmlReader> xmlReaderPool) {
		this.xmlReaderPool = xmlReaderPool;
		return this;
	}
}
