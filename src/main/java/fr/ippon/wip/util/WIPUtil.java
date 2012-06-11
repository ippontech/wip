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

package fr.ippon.wip.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.portlet.Attributes;

/**
 * Wip Utility Class
 *
 * @author Anthony Luce
 * @author Quentin Thierry
 */

public class WIPUtil {
    private static final String BUNDLE_NAME = "content.Language";

    private static final Map<Locale, ResourceBundle> bundles = new HashMap<Locale, ResourceBundle>();

    public static String getMessage(String key, Locale locale) {
        ResourceBundle bundle = bundles.get(locale);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            bundles.put(locale, bundle);
        }
        return bundle.getString(key);
    }
    
    /**
     * Extract the selected configuration associated to a session
     * @param request the session container
     * @return the selected configuration
     */
    public static WIPConfiguration extractConfiguration(PortletRequest request) {
    	PortletSession session = request.getPortletSession();
    	return (WIPConfiguration) session.getAttribute(Attributes.CONFIGURATION.name());
    }
}
