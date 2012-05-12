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

package fr.ippon.wip.portlet;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;
import fr.ippon.wip.http.*;
import fr.ippon.wip.ltpa.LtpaCookieUtil;
import fr.ippon.wip.transformers.HTMLTransformer;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.xml.sax.SAXException;

import javax.portlet.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * WIPortlet enables simple web application integration within a portlet. It
 * override the processAction, render and serveResource methods of GenericPortlet.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class WIPortlet extends GenericPortlet {

	// Session attribute and request parameter keys
	public static final String WIP_REQUEST_KEY = "WIP_REQUEST";
	public static final String WIP_RESPONSE_KEY = "WIP_RESPONSE";
	public static final String LINK_URL_KEY = "WIP_LINK_URL";
	public static final String METHOD_TYPE = "WIP_METHOD";
	public static final String RESOURCE_URL_KEY = "WIP_RESOURCE_URL";
	public static final String RESOURCE_TYPE_KEY = "WIP_RESOURCE_TYPE";
	public static final String AJAX_URL_KEY = "WIP_AJAX_URL";
	public static final String URL_CONCATENATION_KEY = "WIP_URL_CONCATENATION";

	// Class attributes
	private HttpManager httpManager;
	private WIPConfigurationManager wipConfigurationManager;

	@Override
	public void init(PortletConfig config) throws PortletException {
		super.init(config);
		wipConfigurationManager = WIPConfigurationManager.getInstance();
		httpManager = HttpManagerImpl.getInstance();
		
		String pathConfigFiles = config.getPortletContext().getRealPath(config.getInitParameter("config-path"));
		wipConfigurationManager.load(pathConfigFiles);
	}
	
	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		// Getting session and user session id
		PortletSession session = request.getPortletSession();
		String id = session.getId();
		String instance = response.getNamespace();
		
		// Getting WIP request 
		WIPRequest wipRequest = (WIPRequest) session.getAttribute(WIP_REQUEST_KEY);
		WIPResponse wipResponse = (WIPResponse) session.getAttribute(WIP_RESPONSE_KEY);
		
		// Getting WIP config
		WIPConfiguration wipConfig = wipConfigurationManager.getConfiguration(response.getNamespace());
		response.setTitle(wipConfig.getPortletTitle());
		
		// The response is set only if processAction have been executed before
		if (wipResponse == null) {
			// LTPA SSO authentication
			if (wipConfig.getLtpaSsoAuthentication()) {
				String cookie = LtpaCookieUtil.getLtpaCookie(request, wipConfig);
				if (cookie != null)
					httpManager.saveSingleCookie(id, cookie);
			}
			// First request
			if (wipRequest == null) {
				wipRequest = new WIPRequest(wipConfig.getInitUrlAsString(), request, false);
				session.setAttribute(WIP_REQUEST_KEY, wipRequest);
			}
			wipResponse = httpManager.doRequest(id, wipRequest, instance);
		} else {
			// Removing from session
			session.removeAttribute(WIP_RESPONSE_KEY);
		}
		
		// Transforming the response if it has not been already done
		if (!wipResponse.isTransformedResponse())
			wipResponse.transformHTML(request, response, wipRequest.getUrl());
		
		// Caching the response
		httpManager.setCacheEntry(id, wipRequest, wipResponse, instance);
		
		if (!wipResponse.getAuthType().equals("none")) {
			// Redirecting to the form
			String location = "/WEB-INF/jsp/auth.jsp";
			PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher(location);
			request.setAttribute("authType", wipResponse.getAuthType());
			portletRequestDispatcher.include(request, response);
		} else { 
			// Writing response
			response.setContentType(wipResponse.getContentType());
			PrintWriter pw = response.getWriter();
			pw.print(getLogoutButton(session, response));
			pw.print(wipResponse.getRemoteResponse());
			pw.close();
		}
	}

	@Override
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
		// Getting portlet session and user session id
		PortletSession session = request.getPortletSession();
		String id = session.getId();
		String instance = response.getNamespace();
		
		// If edit mode, process the edit action 
		if (request.getPortletMode().equals(PortletMode.EDIT)) {
			WIPEdit.processAction(request, response);
			httpManager.cleanCache();
		}
		
		// Authentication
		else if (request.getParameter("auth") != null) {
			WIPAuth.processAction(request, response, httpManager);
		}
		
		// Else normal behavior
		else {
			// Getting URL, creating the WIPRequest and getting the corresponding WIPResponse
			String url = request.getParameter(LINK_URL_KEY);
			WIPRequest wipRequest = new WIPRequest(url, request, false);
			WIPResponse wipResponse = httpManager.doRequest(id, wipRequest, instance);
			
			int statusCode = wipResponse.getStatusCode();
			String contentType = wipResponse.getContentType();
			
			// Redirect to the resource handler or let the doView do the work
			if (statusCode == StatusCode.OK && contentType.compareTo("text/html") != 0) {
				// Creating a new WIPDownloader, registering, starting
				WIPDownloader downloader = new WIPDownloader(wipResponse.getHttpMethod());
				downloader.register();
				downloader.start();
				// Redirecting to ResourceHandler servlet
				response.sendRedirect(request.getContextPath() + "/ResourceHandler?contentType=" + contentType + "&dId=" + downloader.getDownloaderId());
			} else {
				// Saving in session to be treated in doView
				session.setAttribute(WIP_REQUEST_KEY, wipRequest);
				session.setAttribute(WIP_RESPONSE_KEY, wipResponse);
			}
		}
	}

	// Two behaviors for serveResource:
	// 1. Rewrite imported style sheets and scripts
	// 2. Manage AJAX Request
	@Override
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		WIPResponse wipResponse = null;
		
		// Getting portlet session and user session id
		PortletSession session = request.getPortletSession();
		String id = session.getId();
		String instance = response.getNamespace();
		
		// Handling CSS and JS resources
		if (request.getParameter(AJAX_URL_KEY) == null) {
			// Getting URL, creating the WIPRequest and getting the corresponding WIPResponse
			String url = request.getParameter(RESOURCE_URL_KEY);
			String url2 = request.getParameter(URL_CONCATENATION_KEY);
			if (url2 !=  null && !url2.equals("")) 
				url += url2.replaceAll(" ", "%20");
			
			WIPRequest wipRequest = new WIPRequest(url, request, true);
			wipResponse = httpManager.doRequest(id, wipRequest, instance);
			
			// Transforming response according to the resource type
			if (!wipResponse.isTransformedResponse()) {
				String type = request.getParameter(RESOURCE_TYPE_KEY);
				if (type.compareTo("CSS") == 0) {
					wipResponse.transformCSS(request, response, wipRequest.getUrl());
				} else if (type.compareTo("JS") == 0) {
					wipResponse.transformJS(request, response, wipRequest.getUrl());
				}
			}
			// Caching the response
			httpManager.setCacheEntry(id, wipRequest, wipResponse, instance);
		} 
		
		// Handling Ajax
		else {
			// Getting URL, creating the WIPRequest and getting the corresponding WIPResponse
			String url = request.getParameter(AJAX_URL_KEY);
			
			// Managing url contatenation in JS files 
			// For Liferay, do not work with GateIn and uPortal
			String url2 = request.getParameter(URL_CONCATENATION_KEY);
			if (url2 !=  null && !url2.equals(""))
				url += url2.replaceAll(" ", "%20");

			WIPRequest wipRequest = new WIPRequest(url, request, true);
			wipResponse = httpManager.doRequest(id, wipRequest, instance);

			if (!wipResponse.isTransformedResponse()) {
				if (wipResponse.getContentType().equals("text/html")) {
					wipResponse.transformHTML(request, response, wipRequest.getUrl());
				} else if (wipResponse.getContentType().equals("text/javascript")) {
					wipResponse.transformJS(request, response, wipRequest.getUrl());
				} else if (wipResponse.getContentType().equals("application/json")) {
					wipResponse.transformJSON();
				} else if (wipResponse.getContentType().equals("application/xml")
						|| wipResponse.getContentType().equals("text/xml")) {
					// TODO: handle xml rewriting
				}
			}
		}

		// Writing response
		response.setContentType(wipResponse.getContentType());
		
		if (request.getParameter(RESOURCE_TYPE_KEY)!=null && request.getParameter(RESOURCE_TYPE_KEY).equals("other")) {
			OutputStream os = response.getPortletOutputStream();
			os.write(wipResponse.getBinaryContent());
			os.close();
		} else {
			PrintWriter pw = response.getWriter();
			pw.print(wipResponse.getRemoteResponse());
			pw.close();
		}
	}

	@Override
	protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		PortletSession session = request.getPortletSession();
		if (session.getAttribute("editPage") != null && !session.getAttribute("editPage").equals("")) {
			String location = "/WEB-INF/jsp/" + session.getAttribute("editPage") + ".jsp";
			//session.removeAttribute("editPage");
			PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher(location);
			portletRequestDispatcher.include(request, response);
		} else if (session.getAttribute("configPage") != null) {
			PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/existingconfig.jsp?configPage"+session.getAttribute("configPage"));
			//session.removeAttribute("configPage");
			portletRequestDispatcher.include(request, response);
		} else if (session.getAttribute("saveConfig") != null) {
			PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/saveconfig.jsp");
			//session.removeAttribute("saveConfig");
			portletRequestDispatcher.include(request, response);
		} else if (session.getAttribute("source") != null) {
			String url = (String) session.getAttribute("source");
			//session.removeAttribute("source");
			WIPRequest wipRequest = new WIPRequest(url, request, false);
			WIPResponse wipResponse = httpManager.doRequest(session.getId(), wipRequest, response.getNamespace());
			String ret = "";
			try {
				ret = HTMLTransformer.htmlToXhtml(wipResponse.getRemoteResponse());
			} catch (SAXException e) {
				e.printStackTrace();
			}
			PrintWriter pw = response.getWriter();
			pw.print(ret.replaceAll("<", "&lt;").replaceAll(">", "&gt;<br />"));
			pw.close();
	 	} else {
	 		PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/generalsettings.jsp");
			portletRequestDispatcher.include(request, response);
	 	}	
	}

	@Override
	public void destroy() {
		httpManager.destroy();
        MultiThreadedHttpConnectionManager.shutdownAll();
		super.destroy();
	}
	
	private String getLogoutButton(PortletSession session, RenderResponse response) {
		String ret = "";
//		String id = session.getId();
		if (session.getAttribute("authType") != null) {
			PortletURL logout = response.createActionURL();
			logout.setParameter("auth", "logout");
			ret = "Logged in through the portal. Clic <a href=\""+logout.toString()+"\"> here </a> to log out.";
//		} else if (CookiesManagerImpl.getInstance().hasCookies(id)) {
//			PortletURL clearCookiesURL = response.createActionURL();
//			clearCookiesURL.setParameter("auth", "clearCookies");
//			ret = "Clic <a href=\""+clearCookiesURL.toString()+"\"> here </a> to clear cookies from current session.";
		}
		return ret;
	}
}
