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

package fr.ippon.wip.filter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;

import fr.ippon.wip.http.hc.HttpClientDecorator;
import fr.ippon.wip.transformers.CSSTransformer;
import fr.ippon.wip.transformers.HTMLTransformer;
import fr.ippon.wip.transformers.JSTransformer;

/**
 * A filter for logging time processing of different transformation stape
 * 
 * @author Yohan Legat
 *
 */
public class PerformanceFilter implements RenderFilter {

	private final String HOME = System.getProperty("user.home");
	
	private BufferedWriter out;
	
	public PerformanceFilter() {
		// TODO Auto-generated constructor stub
	}
	
	public void init(FilterConfig filterConfig) throws PortletException {
		String logDirectory = HOME + "/wip";
		try {
			File file = new File(logDirectory + "/timeProcess");
			out = new BufferedWriter(new FileWriter(file));
			if(!file.exists())
				out.write("HTML,CSS,JS,HTTP_COMPONENTS");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void destroy() {
		try {
			out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void doFilter(RenderRequest request, RenderResponse response, FilterChain chain) throws IOException, PortletException {
		chain.doFilter(request, response);

		StringBuilder data = new StringBuilder();
		data.append(Long.toString(HTMLTransformer.timeProcess.get().or(0L)) + ",");
		data.append(Long.toString(CSSTransformer.timeProcess.get().or(0L)) + ",");
		data.append(Long.toString(JSTransformer.timeProcess.get().or(0L)) + ",");
		data.append(Long.toString(HttpClientDecorator.timeProcess.get().or(0L)) + ",");
		data.append("\n");
		out.write(data.toString());
	}
}
