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

package fr.ippon.wip.ltpa.providers;

import javax.portlet.PortletRequest;

/**
 * The interface CredentialProvider must be implemented
 * by the external class that supply the credential.
 */
public interface CredentialProvider {

    /**
     * This method returns the user credential. The "request" parameter
     * is used to send data needed by the implementing method (ex: a user ID)
     *
     * @param request PortletRequest object to transmit data
     * @return a String tuple {domain, ltpaSecret}
     */
    public String getCredentials(PortletRequest request);

}
