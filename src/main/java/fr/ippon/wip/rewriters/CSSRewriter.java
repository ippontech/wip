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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.portlet.PortletResponse;

/**
 * A rewriter used to rewrite CSS code
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 */
public class CSSRewriter extends WIPRewriter {
	
	/**
	 * A boolean value used to know wheter the user is authenticated or not to the distant application
	 */
	private boolean authenticated;

	/**
	 * A constructor who calls the super class's one with the given url, and set authenticated to the given value
	 * 
	 * @param currentUrl The URL of the page of the distant application currently displayed in the portlet
	 * @param authenticated A boolean value used to know wheter the user is authenticated or not to the distant application
	 */
	public CSSRewriter(String currentUrl, boolean authenticated) {
		super(currentUrl);
		this.authenticated = authenticated;
	}

	/**
	 * Rewrite every url found thanks to the given regex in the input 
	 * String to ResourceURL if the user is authenticated, and to 
	 * absolute URLs if he's not
	 * 
	 * @param regex The regex used to find URLs in the CSS code
	 * @param input The CSS code that we want to rewrite
	 * @param response An empty PortletResponse used to create the ResourceURLs
	 * @return The CSS code, whose all URLs have been rewrited
	 */
	public String rewrite(String regex, String input, PortletResponse response) {
		Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
        	try{
	            int group = extractGroup(matcher);
	            if(group > 0) {
	                String before = input.substring(matcher.start(), matcher.start(group));
	                String url = matcher.group(group);
	                String after = input.substring(matcher.end(group), matcher.end());
	                if (before.startsWith("@import") || before.startsWith("@CHARSET")) {
	                	matcher.appendReplacement(sb, before + rewriteResource(url, response, "CSS") + after);
	                } else {
	                	if (authenticated)
		                	matcher.appendReplacement(sb, before + rewriteResource(url, response, "other") + after);
		                else
		                	matcher.appendReplacement(sb, before + rewriteUrl(url) + after);
	                }
	            }
        	}catch (IllegalArgumentException e) {
				e.printStackTrace();
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
