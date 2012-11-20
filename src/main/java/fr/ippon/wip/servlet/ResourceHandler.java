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

package fr.ippon.wip.servlet;

import fr.ippon.wip.http.reponse.Response;
import fr.ippon.wip.state.ResponseStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * The servlet used to handle downloads.
 *
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class ResourceHandler extends HttpServlet {

    public ResourceHandler() {
        super();
    }

    /**
     * This method is called after the redirection to this servlet, and will
     * wait for the WIPDownloader to complete its download and then get its file
     * as a String and write it in the response
     *
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        // Retrieve response
        UUID uuid = UUID.fromString(request.getParameter("uuid"));
        Response wipResponse = ResponseStore.getInstance().get(uuid);

        // Send response
        wipResponse.sendResponse(response);
    }

}
