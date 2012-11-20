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

package fr.ippon.wip.http.reponse;

import fr.ippon.wip.state.PortletWindow;
import fr.ippon.wip.transformers.ActionToRenderTransformer;
import fr.ippon.wip.util.WIPUtil;
import org.apache.commons.io.IOUtils;

import javax.portlet.*;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;

/**
 * Container class for all date describing an HTTP response from a remote host
 * 
 * @author François Prot
 */
public class Response {

	private byte[] content;

	private Charset charset;

	private String mimeType;

	private String url;

	private int responseCode;

	private boolean portalURLComputed;

	public Response(byte[] content, Charset charset, String mimeType, String url, int responseCode, boolean portalURLComputed) {
		this.content = content;
		this.mimeType = mimeType;
		this.url = url;
		this.responseCode = responseCode;
		this.portalURLComputed = portalURLComputed;
		this.charset = (charset != null) ? charset : Charset.defaultCharset();
	}

	/**
	 * This method is an ugly but necessary hack in order to permit content
	 * transformation in the ACTION phase when it is not possible to create
	 * PortletURL.
	 * 
	 * This method parse the response content to find all links to rewrite into
	 * PortletURL
	 * 
	 * @param portletRequest
	 * @param mimeResponse
	 */
	private void computePortalURL(PortletRequest portletRequest, MimeResponse mimeResponse) {
		try {
			String originalContent = new String(content, charset);
			PortletWindow windowState = PortletWindow.getInstance(portletRequest);
			ActionToRenderTransformer transformer = new ActionToRenderTransformer(portletRequest, mimeResponse, windowState.getActualURL());
			String transformedContent = transformer.transform(originalContent);
			content = transformedContent.getBytes(charset);

		} catch (IOException ioe) {
			throw new RuntimeException("Error computing portal URLs", ioe);
		}
		portalURLComputed = true;
	}

	/**
	 * Infer filename from URL
	 * 
	 * @return
	 */
	public String getFileName() {
		String fileName = url;
		int lastSlashIndex = fileName.lastIndexOf('/');
		if (lastSlashIndex >= 0) {
			fileName = fileName.substring(lastSlashIndex);
			int questionMarkIndex = fileName.indexOf('?');
			if (questionMarkIndex >= 0) {
				fileName = fileName.substring(0, questionMarkIndex);
			}
		}
		return fileName;
	}

	private String getLogoutButton(RenderResponse response) {
		PortletURL logout = response.createActionURL();
		logout.setParameter("auth", "logout");
		String message = WIPUtil.getMessage("wip.auth.logout", response.getLocale());
		return MessageFormat.format(message, logout.toString());
	}

	public String getUrl() {
		return url;
	}

	public boolean isBinary() {
		return !(mimeType.startsWith("text") || mimeType.startsWith("application/xhtml+xml") || mimeType.startsWith("application/json") || mimeType.startsWith("application/xml") || mimeType
				.startsWith("application/javascript"));
	}

	public boolean isHtml() {
		return mimeType.startsWith("text/html") || mimeType.startsWith("application/xhtml+xml");
	}

	/**
	 * Print the response content to a MimeResponse (RenderResponse or
	 * ResourceResponse)
	 * 
	 * In the RESOURCE phase, this method can send binary content and set
	 * Content-Type and HTTP status code
	 * 
	 * @param portalRequest
	 * @param portalResponse
	 * @param printLogout
	 * @throws IOException
	 */
	public void printResponseContent(PortletRequest portalRequest, MimeResponse portalResponse, boolean printLogout) throws IOException {
		if (portalRequest instanceof RenderRequest && !portalURLComputed) {
			computePortalURL(portalRequest, portalResponse);
		}

		if (portalResponse instanceof ResourceResponse) {
			// Set content-type & return code
			portalResponse.setContentType(mimeType);
			portalResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(responseCode));
		}

		if (isBinary()) {
			if (portalResponse instanceof ResourceResponse) {
				OutputStream os = portalResponse.getPortletOutputStream();
				IOUtils.copy(new ByteArrayInputStream(content), os);
				os.close();
			}
		} else {
			PrintWriter writer = portalResponse.getWriter();

			if (portalResponse instanceof RenderResponse && printLogout) {
				writer.print(getLogoutButton((RenderResponse) portalResponse));
			}

			writer.print(new String(content, charset));
			writer.close();
		}
	}

	/**
	 * Print the response content to and HttpServletResponse
	 * 
	 * Set appropriate Content-Type, Charset and HTTP status code. Force
	 * Content-Disposition to "attachment". Can handle binary content.
	 * 
	 * @param httpServletResponse
	 * @throws IOException
	 */
	public void sendResponse(HttpServletResponse httpServletResponse) throws IOException {
		httpServletResponse.setContentType(mimeType);
		httpServletResponse.setCharacterEncoding(charset.name());
		httpServletResponse.setStatus(responseCode);
		if (isBinary()) {
            httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + getFileName() + "\"");
            OutputStream os = httpServletResponse.getOutputStream();
			IOUtils.copy(new ByteArrayInputStream(content), os);
			os.close();
		} else {
			String textContent = new String(content, charset);
			httpServletResponse.setContentLength(textContent.length());
			PrintWriter writer = httpServletResponse.getWriter();
			writer.print(textContent);
		}
	}
}
