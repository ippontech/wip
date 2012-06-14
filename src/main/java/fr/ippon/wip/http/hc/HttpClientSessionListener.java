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

package fr.ippon.wip.http.hc;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Implementation of HttpSessionListener. Must be declared in web.xml to release
 * resources associated to a session when it is destroyed.
 *
 * @author François Prot
 */
public class HttpClientSessionListener implements HttpSessionListener {
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        // Nothing to do
    }

    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpClientResourceManager.getInstance().releaseSessionResources(httpSessionEvent.getSession().getId());
    }
}
