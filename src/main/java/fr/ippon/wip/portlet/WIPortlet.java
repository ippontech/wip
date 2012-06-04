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
import fr.ippon.wip.http.hc.HttpClientExecutor;
import fr.ippon.wip.state.*;

import javax.portlet.*;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * WIPortlet enables simple web application integration within a portlet. It
 * override the processAction, render and serveResource methods of GenericPortlet.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class WIPortlet extends GenericPortlet {

    private static final Logger LOG = Logger.getLogger(WIPortlet.class.getName());

    // Session attribute and request parameter keys
    public static final String WIP_REQUEST_PARAMS_PREFIX_KEY = "WIP_";
	public static final String LINK_URL_KEY = "WIP_LINK_URL";
	public static final String METHOD_TYPE = "WIP_METHOD";
	public static final String RESOURCE_URL_KEY = "WIP_LINK_URL";
	public static final String RESOURCE_TYPE_KEY = "WIP_RESOURCE_TYPE";
	public static final String AJAX_URL_KEY = "WIP_LINK_URL";
	public static final String URL_CONCATENATION_KEY = "WIP_URL_CONCATENATION";

	// Class attributes
	private WIPConfigurationManager wipConfigurationManager;
    private HttpExecutor executor;

	@Override
	public void init(PortletConfig config) throws PortletException {
		super.init(config);

		wipConfigurationManager = WIPConfigurationManager.getInstance();
		String pathConfigFiles = config.getPortletContext().getRealPath(config.getInitParameter("config-path"));
		wipConfigurationManager.load(pathConfigFiles);

        executor = new HttpClientExecutor();
	}
	
	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        WIPConfiguration wipConfig = wipConfigurationManager.getConfiguration(request.getWindowID());
        PortletWindow windowState = PortletWindow.getInstance(request);
        Response wipResponse = null;
        if (windowState.isRenderPending()) {
            // Get response from store & send it
            UUID uuid = windowState.getRequestResponseID();
            wipResponse = ResponseStore.getInstance().remove(uuid);
            windowState.setRenderPending(false);
        }

        if (wipResponse == null) {
            // If no pending response, create a new request
            Request wipRequest = new Request();
            wipRequest.setHttpMethod(Request.HttpMethod.GET);
            wipRequest.setResourceType(Request.ResourceType.HTML);
            if (windowState.getCurrentURI() == null) {
                // Create first request for this portlet window
                String initUrl = wipConfig.getInitUrlAsString();
                wipRequest.setRequestedURL(initUrl);
                // Update state
                windowState.setCurrentURI(initUrl);
            } else {
                // Re-create request with current URI
                wipRequest.setRequestedURL(windowState.getCurrentURI());
            }
            // Execute request
            wipResponse = executor.execute(wipRequest, request, response);
        }
        // Set Portlet title
        response.setTitle(wipConfig.getPortletTitle());

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

	@Override
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
        // If edit mode, process the edit action
        if (request.getPortletMode().equals(PortletMode.EDIT)) {
            WIPEdit.processAction(request, response);
            return;
        }
        // Authentication
        else if (request.getParameter("auth") != null && request.getParameter(WIPortlet.LINK_URL_KEY) == null) {
            manageAuthentication(request, response);
            return;
        }

        Request wipRequest = new Request(request);
        Response wipResponse = executor.execute(wipRequest, request, response);

        WIPConfiguration wipConfig = wipConfigurationManager.getConfiguration(request.getWindowID());

        if (!wipConfig.isProxyURI(wipResponse.getUri())) {
            // Redirect to remote URI
            try {
                response.sendRedirect(wipResponse.getUri());
            } finally {
                wipResponse.dispose();
            }
        } else {
            // Store response for future usage
            UUID uuid = ResponseStore.getInstance().store(wipResponse);
            if ((!wipResponse.getMimeType().startsWith("text/html") && !wipResponse.getMimeType().startsWith("application/xhtml+xml"))) {
                // Redirect to ResourceServlet
                response.sendRedirect(request.getContextPath() + "/ResourceHandler?&uuid=" + uuid.toString());
            } else {
                // Update state & let the portlet render
                PortletWindow windowState = PortletWindow.getInstance(request);
                windowState.setRequestResponseID(uuid);
                windowState.setRenderPending(true);
                windowState.setCurrentURI(wipResponse.getUri());
            }
        }
	}

	// Two behaviors for serveResource:
	// 1. Rewrite imported style sheets and scripts
	// 2. Manage AJAX Request
	@Override
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
        // Create request
        Request wipRequest = new Request(request);

        //Execute request
        Response wipResponse = executor.execute(wipRequest, request, response);

        // Print content
        wipResponse.printResponseContent(request, response, false);
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
		} else {
	 		PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/generalsettings.jsp");
			portletRequestDispatcher.include(request, response);
	 	}	
	}

	@Override
	public void destroy() {
		super.destroy();
        executor.destroy();
	}

    private void manageAuthentication (ActionRequest actionRequest, ActionResponse actionResponse) {
        PortletSession session = actionRequest.getPortletSession();
        if (actionRequest.getParameter("auth").equals("login")) {
            // Registering user login & password in session
            executor.login (actionRequest.getParameter("login"), actionRequest.getParameter("password"), actionRequest);
        } else if (actionRequest.getParameter("auth").equals("logout")) {
            // Logout the user
            executor.logout (actionRequest);
        }
    }

}
