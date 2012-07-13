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

package fr.ippon.wip.transformers;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.portlet.PortletRequest;
import javax.xml.transform.TransformerException;

/**
 * JSONTransformer implements the WIPTransformer interface that defines the
 * transform method used to rewrite the JSON code in the remote response.
 *
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class JSONTransformer extends AbstractTransformer {

	public JSONTransformer(PortletRequest portletRequest, String actualUrl) {
		super(portletRequest, actualUrl);
	}

    public String transform(String input) throws SAXException, IOException, TransformerException {
    	super.transform(input);

        // CUSTOM -----------------------------------------------
        int i = input.indexOf("api/node");
        if (i > -1)
            input = input.substring(0, i) + "/share/proxy/alfresco/" + input.substring(i);
        // ------------------------------------------------------

        return input;
    }
}
