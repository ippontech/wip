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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletSession;

import fr.ippon.wip.cookies.CookiesManagerImpl;
import fr.ippon.wip.http.HttpManager;

/**
 * Class to manage WIP auhtentication mechanisms. 
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */

public class WIPAuth {

	/**
	 * A pseudo processAction method, replacing the WIPortlet's one to perform authentication.
	 * @param request The ActionRequest sent to WIPortlet in edit mode
	 * @param response The ActionResponse sent to WIPortlet in edit mode
	 */
	public static void processAction(ActionRequest request, ActionResponse response, HttpManager httpManager) {
		
		PortletSession session = request.getPortletSession();
		if (request.getParameter("auth").equals("login")) {
			// Registering user login & password in session
			session.setAttribute("userLogin", request.getParameter("login"));
			session.setAttribute("userPassword", request.getParameter("password"));
			session.setAttribute("authType", request.getParameter("authType"));

		} else if (request.getParameter("auth").equals("logout")) {
			// Logout the user
			session.removeAttribute("userLogin");
			session.removeAttribute("userPassword");
			session.removeAttribute("authType");
			httpManager.cleanPrivateCache(session.getId());
		}
	}
	
}
