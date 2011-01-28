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

package fr.ippon.wip.rewriters;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletResponse;
import javax.portlet.ResourceURL;

import fr.ippon.wip.portlet.WIPortlet;

/**
 * An util class used to rewrite JS-related parts of code
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class JSRewriter extends WIPRewriter {

	/**
	 * A boolean value used to know wheter the user is authenticated or not to the distant application
	 */
	private boolean authenticated;
	
	/**
	 * A constructor calling the super constructor with the given current URL
	 * @param currentUrl
	 */
	public JSRewriter(String currentUrl, boolean authenticated) {
		super(currentUrl);
		this.authenticated = authenticated;
	}

	/**
	 * A method to rewrite URL corresponding to ajax calls
	 * @param url The original URL of the ajax call
	 * @param resourceURL An empty ResourceURL
	 * @return The given ResourceURL to which a parameter was added : the given URL transformed into an absolute URL if needed
	 */
	public String rewriteAjax(String url, ResourceURL resourceURL) {
		// Setting parameter
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		String[] tab = {toAbsolute(url)};
		parameters.put(WIPortlet.AJAX_URL_KEY, tab);
		resourceURL.setParameters(parameters);
		
		// URL_CONCATENATION: to concatenate the end of the URL in case the URL is a String concatenation
		String ret = resourceURL.toString()+"&"+WIPortlet.URL_CONCATENATION_KEY+"=";
		// Delete unused parameters
		int i = ret.indexOf("WIP_RESOURCE_URL");
		int j = ret.indexOf("WIP_RESOURCE_TYPE");
		if (i > -1 && j > -1) {
			if (i < j)
				ret = ret.substring(0, i) + ret.substring(j);
			else 
				ret = ret.substring(0, j) + ret.substring(i); 
		}
		
		return ret;
	}
	
	public String rewrite(String regex, String input, PortletResponse response) {
		Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            int group = extractGroup(matcher);
            if(group > 0) {
                String before = input.substring(matcher.start(), matcher.start(group));
                String url = matcher.group(group);
                String after = input.substring(matcher.end(group), matcher.end());
                if (authenticated)
                	matcher.appendReplacement(sb, before + rewriteResource(url, response, "other") + after);
                else
                	matcher.appendReplacement(sb, before + rewriteUrl(url) + after);
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
	}

    private int extractGroup(Matcher matcher) {
        int matchingGroup = -1;
        for(int i = 1; i <= matcher.groupCount(); i++) {
            if(matcher.start(i) > -1) {
                matchingGroup = i;
                break;
            }
        }
        return matchingGroup;
    }

}
