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

import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

/**
 * Wip Utility Class
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */

public class WIPUtil {

    private static final Logger LOG = Logger.getLogger(WIPUtil.class.getName());

    private static final String BUNDLE_NAME = "content.Language";

    private static Map<Locale, ResourceBundle> bundles = new HashMap<Locale, ResourceBundle>();

    public static String getMessage (String key, Locale locale) {
        ResourceBundle bundle = bundles.get(locale);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
            bundles.put(locale, bundle);
        }
        return bundle.getString(key);
    }
}
