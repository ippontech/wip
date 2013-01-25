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
import fr.ippon.wip.config.dao.AbstractConfigurationDAO;
import fr.ippon.wip.http.HttpExecutor;
import fr.ippon.wip.http.hc.HttpClientExecutor;
import fr.ippon.wip.http.hc.HttpClientResourceManager;
import fr.ippon.wip.http.reponse.Response;
import fr.ippon.wip.http.request.RequestBuilder;
import fr.ippon.wip.http.request.RequestBuilderFactory;
import fr.ippon.wip.state.PortletWindow;
import fr.ippon.wip.state.ResponseStore;
import fr.ippon.wip.util.WIPLogging;
import fr.ippon.wip.util.WIPUtil;

import javax.portlet.*;

import org.apache.commons.lang.StringUtils;

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
 * @author Yohan Legat
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

        // initialization of logging singleton
		WIPLogging logging = WIPLogging.INSTANCE;

		double heuristicCacheRatio = Double.parseDouble(config.getInitParameter("HEURISTIC_CACHE_RATIO"));
		HttpClientResourceManager.getInstance().setHeuristicCacheRation(heuristicCacheRatio);

		int staleIfErrorTime = Integer.parseInt(config.getInitParameter("STALE_IF_ERROR"));
		HttpClientResourceManager.getInstance().setStaleIfErrorTime(staleIfErrorTime);
		
		int responseStoreMaxEntries = Integer.parseInt(config.getInitParameter("RESPONSE_STORE_MAX_ENTRIES"));
		ResponseStore.getInstance().setMaxEntries(responseStoreMaxEntries);
		executor = new HttpClientExecutor();
	}

	/**
	 * Check if a configuration has been selected. If not then select one.
	 * @param request
	 */
	private void checkIsConfigurationSet(PortletRequest request) {
		String configurationName = (String) request.getPortletSession().getAttribute(Attributes.CONFIGURATION_NAME.name());
		if(!StringUtils.isEmpty(configurationName))
			return;
		
		PortletPreferences preferences = request.getPreferences();
		configurationName = preferences.getValue(Attributes.CONFIGURATION_NAME.name(), AbstractConfigurationDAO.DEFAULT_CONFIG_NAME);
		request.getPortletSession().setAttribute(Attributes.CONFIGURATION_NAME.name(), configurationName);
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
		checkIsConfigurationSet(request);
		WIPConfiguration configuration = WIPUtil.getConfiguration(request);
		
		PortletWindow windowState = PortletWindow.getInstance(request);
		
		// reset if the used configuration has changed
		if(windowState.getConfiguration().getTimestamp() != configuration.getTimestamp()) {
			PortletWindow.clearInstance(request);
			windowState = PortletWindow.getInstance(request);
		}
		
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
			String requestUrl = windowState.getActualURL();
			RequestBuilder wipRequest = RequestBuilderFactory.INSTANCE.getRequest(request, requestUrl, RequestBuilder.ResourceType.HTML, RequestBuilder.HttpMethod.GET, null, false);

			// TODO: copy global parameters from PortletRequest ?
			
            if(WIPUtil.isDebugMode(request))
            	WIPLogging.INSTANCE.resetForUrl(wipRequest.getRequestedURL());

			// Execute request
			wipResponse = executor.execute(wipRequest, request, response);
		}
		
		// Set Portlet title
		response.setTitle(WIPUtil.getConfiguration(request).getPortletTitle());

		// Check if authentication is requested by remote host
		if (windowState.getRequestedAuthSchemes() != null) {
			// Redirecting to the form
			String location = Pages.AUTH.getPath();
			PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher(location);
			portletRequestDispatcher.include(request, response);
		} else {
			// Print content
			wipResponse.printResponseContent(request, response, windowState.isAuthenticated());
		}
		
		WIPLogging.INSTANCE.closeTransformLogFile();
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
		checkIsConfigurationSet(request);
		PortletSession session = request.getPortletSession();
		WIPConfiguration wipConfig = WIPUtil.getConfiguration(request);

		if (request.getPortletMode().equals(PortletMode.EDIT)) {
			String debugMode = request.getParameter(Attributes.DEBUG_MODE.name());
			if(!StringUtils.isEmpty(debugMode)) {
				session.setAttribute(Attributes.DEBUG_MODE.name(), Boolean.parseBoolean(debugMode));
				return;
			}
			
			String configurationName = request.getParameter(Attributes.ACTION_SELECT_CONFIGURATION.name());
			if (StringUtils.isEmpty(configurationName))
				return;

			try {
				session.setAttribute(Attributes.CONFIGURATION_NAME.name(), configurationName);
				request.getPreferences().setValue(Attributes.CONFIGURATION_NAME.name(), configurationName);
				request.getPreferences().store();

			} catch (ReadOnlyException e) {
				e.printStackTrace();
			} catch (ValidatorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			PortletWindow.clearInstance(request);
			return;
		}
		
		// If request comes from authentication form, process credentials and go
		// in RENDER phase
		if (request.getParameter("auth") != null && request.getParameter(WIPortlet.LINK_URL_KEY) == null) {
			manageAuthentication(request, response);
			return;
		}

		RequestBuilder wipRequest = RequestBuilderFactory.INSTANCE.getRequest(request);
		
        if(WIPUtil.isDebugMode(request))
        	WIPLogging.INSTANCE.resetForUrl(wipRequest.getRequestedURL());

		Response wipResponse = executor.execute(wipRequest, request, response);

		// Check if remote URI must be proxied
		if (!wipConfig.isProxyURI(wipResponse.getUrl())) {
			// Redirect to remote URI without proxying
			response.sendRedirect(wipResponse.getUrl());
		} else {
            // Store response for future usage
            UUID uuid = ResponseStore.getInstance().store(wipResponse);
            // Check if content must be rendered in the portlet or as an
            // attachment
            if (wipResponse.isHtml()) {
                // Update state & let the portlet render
                PortletWindow windowState = PortletWindow.getInstance(request);
                windowState.setResponseID(uuid);
                windowState.setActualURL(wipResponse.getUrl());
            } else {
                // Redirect to ResourceServlet
                response.sendRedirect(request.getContextPath() + "/ResourceHandler?&uuid=" + uuid.toString());
            }
        }
		
		WIPLogging.INSTANCE.closeTransformLogFile();
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
		checkIsConfigurationSet(request);
		
		// Create request
		RequestBuilder wipRequest = RequestBuilderFactory.INSTANCE.getRequest(request);

		// Execute request
		Response wipResponse = executor.execute(wipRequest, request, response);

		// Print content
		wipResponse.printResponseContent(request, response, false);
		
		WIPLogging.INSTANCE.closeTransformLogFile();
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
		checkIsConfigurationSet(request);
		
		Pages requestedPage = (Pages) request.getPortletSession().getAttribute(Attributes.PAGE.name());
		if(requestedPage == null)
			requestedPage = Pages.SELECT_CONFIG;
		
		PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher(requestedPage.getPath());
		portletRequestDispatcher.include(request, response);
	}

	/**
	 * Releases resources on portlet un-deploy
	 */
	@Override
	public void destroy() {
		super.destroy();

		executor.destroy();
		WIPLogging.INSTANCE.closeAccessLogFile();
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
