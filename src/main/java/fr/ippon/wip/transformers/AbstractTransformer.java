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

import fr.ippon.wip.http.UrlFactory;
import fr.ippon.wip.util.WIPLogging;
import fr.ippon.wip.util.WIPUtil;

import org.xml.sax.SAXException;

import javax.portlet.PortletRequest;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.regex.Matcher;

/**
 * Base convenience abstract class for Transformers
 *
 * Creates a local instance of UrlFactory
 */
public abstract class AbstractTransformer implements WIPTransformer {
	
	protected final String actualUrl;
	
    protected final UrlFactory urlFactory;
    
    protected final PortletRequest portletRequest;
    
    public AbstractTransformer(PortletRequest portletRequest, String actualUrl) {
    	this.portletRequest = portletRequest;
    	this.actualUrl = actualUrl;
		urlFactory = new UrlFactory(portletRequest, actualUrl);
    }

    public String transform(String input) throws SAXException, IOException, TransformerException {
    	if(WIPUtil.isDebugMode(portletRequest))
    		WIPLogging.INSTANCE.logTransform(input);
    	
    	return input;
    }

    protected int extractGroup(Matcher matcher) {
        int matchingGroup = -1;
        for (int i = 1; i <= matcher.groupCount(); i++) {
            if (matcher.start(i) > -1) {
                matchingGroup = i;
                break;
            }
        }
        return matchingGroup;
    }

}
