/*
 *	Copyright 2010,2011 Ippon Technologies 
 *  
 *	This file is part of Wip Portlet.
 *	Wip Portlet is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Wip Portlet is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Wip Portlet.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ippon.wip.transformers;

import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * JSONTransformer implements the WIPTransformer interface that defines the
 * transform method used to rewrite the JSON code in the remote response.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class JSONTransformer implements WIPTransformer {

	public JSONTransformer() {}
	
	public String transform(String input) throws SAXException, IOException {
		
		// CUSTOM -----------------------------------------------
		int i = input.indexOf("api/node");
		if (i > -1)
			input = input.substring(0, i)+"/share/proxy/alfresco/"+input.substring(i);
		// ------------------------------------------------------
		
		return input;
	}

}
