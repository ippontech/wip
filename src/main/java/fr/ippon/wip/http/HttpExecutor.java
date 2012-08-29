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

package fr.ippon.wip.http;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import fr.ippon.wip.http.reponse.Response;
import fr.ippon.wip.http.request.RequestBuilder;

import java.io.IOException;

/**
 * Implements this interface to send HTTP requests to a remote host and process HTTP reponses
 * @author François Prot
 */
public interface HttpExecutor {
    public Response execute(RequestBuilder request, PortletRequest portletRequest, PortletResponse portletResponse) throws IOException;

    public void login(String login, String password, PortletRequest portletRequest);

    public void logout(PortletRequest portletRequest);

    public void destroy();
}