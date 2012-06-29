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

package fr.ippon.wip.state;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.util.WIPUtil;

import java.util.List;
import java.util.UUID;

/**
 * This class is a container for all state datas associated to a given sessionID/windowID
 *
 * Instances of this class are obtained by the static method #getInstance
 *
 * @author François Prot
 */
public class PortletWindow {
    /**
     * This method must be called when the state of a particular session/window must be cleared
     * (on remote logout for example)
     *
     * @param request
     */
    public static void clearInstance(PortletRequest request) {
        PortletSession session = request.getPortletSession();
        session.removeAttribute(PORTLET_SESSION_KEY, PortletSession.PORTLET_SCOPE);
    }
    /**
     * Get the instance associated with this PortletSession (in the PORTLET_SCOPE) if exists.
     * Else create a new one and update session.
     *
     * @param request
     * @return
     */
    public static PortletWindow getInstance(PortletRequest request) {
        PortletSession session = request.getPortletSession();
        PortletWindow state;
        synchronized (session) {
            state = (PortletWindow) session.getAttribute(PORTLET_SESSION_KEY, PortletSession.PORTLET_SCOPE);

            if (state == null) {
                state = new PortletWindow();
                WIPConfiguration config = WIPUtil.getConfiguration(request);
                state.setConfiguration(config);
                state.setActualURL(config.getInitUrl());
                session.setAttribute(PORTLET_SESSION_KEY, state, PortletSession.PORTLET_SCOPE);
            }
        }
        return state;
    }
    
    private UUID responseID;
    private String actualURL;
    private List<String> requestedAuthSchemes;

    private boolean authenticated = false;

	private WIPConfiguration configuration;

	private static final String PORTLET_SESSION_KEY = "wip.window.state";

    private PortletWindow() {
    }

    public WIPConfiguration getConfiguration() {
		return configuration;
	}

    /**
     * The remote URL for this sessionID/windowID.
     *
     * If the remote host send "Redirect" response, the URL is the final target location
     *
     * @return
     */
    public String getActualURL() {
        return actualURL;
    }

    /**
     * Authentication schemes accepted by remote host.
     *
     * Is null is no authentication is requested.
     *
     * @return
     */
    public List<String> getRequestedAuthSchemes() {
        return requestedAuthSchemes;
    }

    /**
     * UUID of a Response obtained in the ACTION phase that has not already been sent to client.
     *
     * Used to get a Response instance from ResponseStore.
     *
     * @return
     */
    public UUID getResponseID() {
        return responseID;
    }

    /**
     * Is the user authenticated on the remote host
     *
     * @return
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public void setConfiguration(WIPConfiguration configuration) {
		this.configuration = configuration;
	}

    public void setActualURL(String actualURL) {
        this.actualURL = actualURL;
    }

    public void setRequestedAuthSchemes(List<String> requestedAuthSchemes) {
        this.requestedAuthSchemes = requestedAuthSchemes;
    }

    public void setResponseID(UUID responseID) {
        this.responseID = responseID;
    }
}
