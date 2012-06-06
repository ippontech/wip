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

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used to cache DTD resources needed by the XSLT parser.
 * These resources are saved in a dtd folder in the classpath.
 *
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class CachedDTD implements EntityResolver {

    private static final String XHTML_DTD = "xhtml1-transitional.dtd";
    private static final String XHTML_LAT1 = "xhtml-lat1.ent";
    private static final String XHTML_SYMBOL = "xhtml-symbol.ent";
    private static final String XHTML_SPECIAL = "xhtml-special.ent";

    public InputSource resolveEntity(String arg0, String arg1)
            throws SAXException, IOException {
        String resource = arg1.substring(arg1.lastIndexOf("/") + 1);
        InputStream uri;
        if (resource.equals(XHTML_DTD)) {
            uri = this.getClass().getResourceAsStream("/dtd/" + XHTML_DTD);
        } else if (resource.equals(XHTML_LAT1)) {
            uri = this.getClass().getResourceAsStream("/dtd/" + XHTML_LAT1);
        } else if (resource.equals(XHTML_SYMBOL)) {
            uri = this.getClass().getResourceAsStream("/dtd/" + XHTML_SYMBOL);
        } else if (resource.equals(XHTML_SPECIAL)) {
            uri = this.getClass().getResourceAsStream("/dtd/" + XHTML_SPECIAL);
        } else {
            return null;
        }
        return new InputSource(uri);

    }

}
