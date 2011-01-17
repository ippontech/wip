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

package fr.ippon.wip.rewriters;

/**
 * A rewriter used to rewrite Ajax-related parts of the code
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 *
 */
public class AjaxRewriter extends WIPRewriter {

	/**
	 * This constructor only calls the super class's constructor
	 * 
	 * @param currentUrl The URL of the page of the distant application currently displayed in the portlet
	 */
	public AjaxRewriter(String currentUrl) {
		super(currentUrl);
	}
	
}
