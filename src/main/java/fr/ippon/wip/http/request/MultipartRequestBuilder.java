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

package fr.ippon.wip.http.request;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;

import javax.portlet.ActionRequest;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;

import com.google.common.collect.Multimap;

/**
 * Container class for all data describing a POST multipart-content request from
 * a remote host.
 * 
 * @author Yohan Legat
 * 
 */
public class MultipartRequestBuilder extends AbstractRequestBuilder implements Serializable, RequestBuilder {

	private static PortletFileUpload fileUploadPortlet;

	private List<DiskFileItem> files;

	{
		DiskFileItemFactory factory = new DiskFileItemFactory(0, null);
		fileUploadPortlet = new PortletFileUpload(factory);
	}

	protected MultipartRequestBuilder(String url, ResourceType resourceType, ActionRequest portletRequest, Multimap<String, String> parameterMap) throws FileUploadException {
		super(url, HttpMethod.POST, resourceType, parameterMap);
		files = fileUploadPortlet.parseRequest(portletRequest);
	}

	public HttpRequestBase buildHttpRequest() throws URISyntaxException {
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		try {
			for(DiskFileItem fileItem : files) {
				if(fileItem.isFormField())
					multipartEntity.addPart(fileItem.getFieldName(), new StringBody(new String(fileItem.get())));
				else {
//					FileBody fileBody = new FileBody(fileItem.getStoreLocation(), fileItem.getName(), fileItem.getContentType(), fileItem.getCharSet());
					InputStreamBody fileBody = new InputStreamKnownSizeBody(fileItem.getInputStream(), fileItem.get().length, fileItem.getContentType(), fileItem.getName());
					multipartEntity.addPart(fileItem.getFieldName(), fileBody);
				}
			}

			// some request may have additional parameters in a query string
			if(parameterMap != null)
				for (Entry<String, String> entry : parameterMap.entries())
					multipartEntity.addPart(entry.getKey(), new StringBody(entry.getValue()));

		
		} catch (Exception e) {
			e.printStackTrace();
		}

		HttpPost postRequest = new HttpPost(requestedURL);
		postRequest.setEntity(multipartEntity);
		return postRequest;
	}

	/*
	 * With InputStreamBody, getContentLength() method return -1 so we have to override it
	 */
	private class InputStreamKnownSizeBody extends InputStreamBody {
		private int lenght;

		public InputStreamKnownSizeBody(final InputStream in, final int lenght, final String mimeType, final String filename) {
			super(in, mimeType, filename);
			this.lenght = lenght;
		}

		@Override
		public long getContentLength() {
			return this.lenght;
		}
	}
}
