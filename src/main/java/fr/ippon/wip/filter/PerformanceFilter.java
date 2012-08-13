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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.filter.ActionFilter;
import javax.portlet.filter.FilterChain;
import javax.portlet.filter.FilterConfig;
import javax.portlet.filter.RenderFilter;
import javax.portlet.filter.ResourceFilter;

import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;

import fr.ippon.wip.http.hc.HttpClientDecorator;
import fr.ippon.wip.portlet.WIPortlet;
import fr.ippon.wip.transformers.CSSTransformer;
import fr.ippon.wip.transformers.HTMLTransformer;
import fr.ippon.wip.transformers.JSTransformer;

/**
 * A filter for logging time processing of different transformation stape
 * 
 * @author Yohan Legat
 *
 */
public class PerformanceFilter implements RenderFilter, ActionFilter, ResourceFilter {

	private final String HOME = System.getProperty("user.home");
	
	private Writer out;
	
	public PerformanceFilter() {
		// TODO Auto-generated constructor stub
	}
	
	public void init(FilterConfig filterConfig) throws PortletException {
		String logDirectory = HOME + "/wip";
		try {
			File file = new File(logDirectory + "/timeProcess");
			out = new FileWriter(file);
			if(!file.exists())
				out.write("TYPE\tREQUEST\tTOTAL\tHTML\tCSS\tJS\tHTTP_COMPONENTS");
			
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
		Stopwatch timeProcess = new Stopwatch().start();
		chain.doFilter(request, response);
		StringBuilder data = new StringBuilder();
		writePerformance(data.append("RENDER\t").append(request.getParameter(WIPortlet.LINK_URL_KEY) + "\t").append(timeProcess.elapsedMillis() + "\t"));
	}

	public void doFilter(ActionRequest request, ActionResponse response, FilterChain chain) throws IOException, PortletException {
		Stopwatch timeProcess = new Stopwatch().start();
		chain.doFilter(request, response);
		StringBuilder data = new StringBuilder();
		writePerformance(data.append("ACTION\t").append(request.getParameter(WIPortlet.LINK_URL_KEY) + "\t").append(timeProcess.elapsedMillis() + "\t"));
	}

	public void doFilter(ResourceRequest request, ResourceResponse response, FilterChain chain) throws IOException, PortletException {
		Stopwatch timeProcess = new Stopwatch().start();
		chain.doFilter(request, response);
		StringBuilder data = new StringBuilder();
		writePerformance(data.append("RESOURCE\t").append(request.getParameter(WIPortlet.LINK_URL_KEY) + "\t").append(timeProcess.elapsedMillis() + "\t"));
	}
	
	private void  writePerformance(StringBuilder data) {
		Optional<Long> optionalTime;
		
		optionalTime = Optional.fromNullable(HTMLTransformer.timeProcess.get());
		data.append(optionalTime.or(0L) + "\t");
		if(optionalTime.isPresent())
			HTMLTransformer.timeProcess.remove();
		
		optionalTime = Optional.fromNullable(CSSTransformer.timeProcess.get());
		data.append(optionalTime.or(0L) + "\t");
		if(optionalTime.isPresent())
			CSSTransformer.timeProcess.remove();
		
		optionalTime = Optional.fromNullable(JSTransformer.timeProcess.get());
		data.append(optionalTime.or(0L) + "\t");
		if(optionalTime.isPresent())
			JSTransformer.timeProcess.remove();
		
		optionalTime = Optional.fromNullable(HttpClientDecorator.timeProcess.get());
		data.append(optionalTime.or(0L) + "\t");
		if(optionalTime.isPresent())
			HttpClientDecorator.timeProcess.remove();
		
		data.append("\n");
		
		try {
			out.write(data.toString());
			out.flush();		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
