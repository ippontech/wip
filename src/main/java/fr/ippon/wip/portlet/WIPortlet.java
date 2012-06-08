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
import fr.ippon.wip.http.HttpExecutor;
import fr.ippon.wip.http.Request;
import fr.ippon.wip.http.Response;
import fr.ippon.wip.http.hc.HttpClientExecutor;
import fr.ippon.wip.state.PortletWindow;
import fr.ippon.wip.state.ResponseStore;
import fr.ippon.wip.util.WIPUtil;

import javax.portlet.*;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * WIPortlet enables web application integration within a portlet. It override
 * the processAction, render and serveResource methods of GenericPortlet.
 * 
 * Uses an instance of HttpExecutor to process remote HTTP request/response
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 * @author François Prot
 */
public class WIPortlet extends GenericPortlet {

	private static final Logger LOG = Logger.getLogger(WIPortlet.class.getName());

	// Session attribute and request parameter keys
	public static final String WIP_REQUEST_PARAMS_PREFIX_KEY = "WIP_";
	public static final String LINK_URL_KEY = "WIP_LINK_URL";
	public static final String METHOD_TYPE = "WIP_METHOD";
	public static final String RESOURCE_TYPE_KEY = "WIP_RESOURCE_TYPE";
	public static final String URL_CONCATENATION_KEY = "WIP_URL_CONCATENATION";

	// Class attributes
	private WIPConfigurationManager wipConfigurationManager;
	private HttpExecutor executor;

	/**
	 * Initialize configuration and create an instance of HttpExecutor
	 * 
	 * @param config
	 *            Configuration from portlet.xml
	 * @throws PortletException
	 */
	@Override
	public void init(PortletConfig config) throws PortletException {
		super.init(config);

		wipConfigurationManager = WIPConfigurationManager.getInstance();
		executor = new HttpClientExecutor();
	}

	/**
	 * Retrieve the portlet configuration, creating it if necessary.
	 * @param request
	 * @return the portlet configuration
	 */
	private WIPConfiguration getOrCreateConfiguration(PortletRequest request) {
		// check if the configuration is already saved in the session
		WIPConfiguration configuration = WIPUtil.extractConfiguration(request);
		if (configuration != null)
			return configuration;

		// retrieve the configuration name associated to the portlet preferences
		PortletPreferences preferences = request.getPreferences();
		String configurationName = preferences.getValue("configurationName", WIPConfigurationManager.DEFAULT_CONFIG_NAME);
		configuration = wipConfigurationManager.getConfiguration(configurationName);

		// update the session with the configuration
		PortletSession session = request.getPortletSession();
		session.setAttribute("configuration", configuration);
		return configuration;
	}

	/**
	 * Processes requests in the RENDER phase for the VIEW portlet mode
	 * 
	 * @param request
	 * @param response
	 * @throws PortletException
	 * @throws IOException
	 */
	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		request.getPreferences();
		WIPConfiguration wipConfig = getOrCreateConfiguration(request);

		PortletWindow windowState = PortletWindow.getInstance(request);
		Response wipResponse = null;
		UUID uuid = windowState.getResponseID();
		// A request has just been processed in the ACTION phase
		if (uuid != null) {
			// Get response from store & send it
			wipResponse = ResponseStore.getInstance().remove(uuid);
			windowState.setResponseID(null);
		}

		// If no pending response, create a new request
		if (wipResponse == null) {
			String requestUrl;
			// Check state for current URI
			if (windowState.getCurrentURL() == null) {
				// Create first request for this portlet window
				requestUrl = wipConfig.getInitUrlAsString();
				// Update state
				windowState.setCurrentURL(requestUrl);
			} else {
				// Re-create request with current URI
				requestUrl = windowState.getCurrentURL();
			}
			Request wipRequest = new Request(requestUrl, Request.HttpMethod.GET, Request.ResourceType.HTML, null);

			// Execute request
			wipResponse = executor.execute(wipRequest, request, response);
		}
		// Set Portlet title
		response.setTitle(wipConfig.getPortletTitle());

		// Check if authentication is requested by remote host
		if (windowState.getRequestedAuthSchemes() != null) {
			// Redirecting to the form
			String location = "/WEB-INF/jsp/auth.jsp";
			PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher(location);
			portletRequestDispatcher.include(request, response);
		} else {
			// Print content
			wipResponse.printResponseContent(request, response, windowState.isAuthenticated());
		}
	}

	/**
	 * Processes request in the ACTION phase
	 * 
	 * @param request
	 * @param response
	 * @throws PortletException
	 * @throws IOException
	 */
	@Override
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
		WIPConfiguration wipConfig = WIPUtil.extractConfiguration(request);

		// If in edit mode, delegates processing to WIPEdit
		if (request.getPortletMode().equals(PortletMode.EDIT)) {
			WIPEdit.processAction(request, response);
			return;
		}
		// If request comes from authentication form, process credentials and go
		// in RENDER phase
		else if (request.getParameter("auth") != null && request.getParameter(WIPortlet.LINK_URL_KEY) == null) {
			manageAuthentication(request, response);
			return;
		}

		Request wipRequest = new Request(request);
		Response wipResponse = executor.execute(wipRequest, request, response);

		// Check if remote URI must be proxied
		if (!wipConfig.isProxyURI(wipResponse.getUrl())) {
			// Redirect to remote URI without proxying
			try {
				response.sendRedirect(wipResponse.getUrl());
			} finally {
				wipResponse.dispose();
			}
		} else {
			// Store response for future usage
			UUID uuid = ResponseStore.getInstance().store(wipResponse);
			// Check if content must be rendered in the portlet or as an
			// attachment
			if (wipResponse.isHtml()) {
				// Update state & let the portlet render
				PortletWindow windowState = PortletWindow.getInstance(request);
				windowState.setResponseID(uuid);
				windowState.setCurrentURL(wipResponse.getUrl());
			} else {
				// Redirect to ResourceServlet
				response.sendRedirect(request.getContextPath() + "/ResourceHandler?&uuid=" + uuid.toString());
			}
		}
	}

	/**
	 * Processes requests in RESOURCE phase
	 * 
	 * @param request
	 * @param response
	 * @throws PortletException
	 * @throws IOException
	 */
	@Override
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		// Create request
		Request wipRequest = new Request(request);

		// Execute request
		Response wipResponse = executor.execute(wipRequest, request, response);

		// Print content
		wipResponse.printResponseContent(request, response, false);
	}

	/**
	 * Processes requests in RENDER phase when portlet mode is EDIT
	 * 
	 * Controller that dispatches requests ot the appropriate JSP
	 * 
	 * @param request
	 * @param response
	 * @throws PortletException
	 * @throws IOException
	 */
	@Override
	protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		PortletSession session = request.getPortletSession();
		
		if (session.getAttribute("editPage") != null && !session.getAttribute("editPage").equals("")) {
			String location = "/WEB-INF/jsp/" + session.getAttribute("editPage") + ".jsp";
			PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher(location);
			portletRequestDispatcher.include(request, response);
		} else if (session.getAttribute("configPage") != null) {
			PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/existingconfig.jsp?configPage" + session.getAttribute("configPage"));
			portletRequestDispatcher.include(request, response);
		} else if (session.getAttribute("saveConfig") != null) {
			PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/saveconfig.jsp");
			portletRequestDispatcher.include(request, response);
		} else {
			PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/generalsettings.jsp");
			portletRequestDispatcher.include(request, response);
		}
	}

	/**
	 * Releases resources on portlet un-deploy
	 */
	@Override
	public void destroy() {
		super.destroy();
		executor.destroy();
	}

	private void manageAuthentication(ActionRequest actionRequest, ActionResponse actionResponse) {
		// Login or logout ?
		if (actionRequest.getParameter("auth").equals("login")) {
			// Registering user login & password in session
			executor.login(actionRequest.getParameter("login"), actionRequest.getParameter("password"), actionRequest);
		} else if (actionRequest.getParameter("auth").equals("logout")) {
			// Logout the user
			executor.logout(actionRequest);
		}
	}
}
