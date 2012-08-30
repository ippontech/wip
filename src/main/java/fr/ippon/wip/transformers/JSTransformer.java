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

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.http.request.PostRequestBuilder;
import fr.ippon.wip.http.request.RequestBuilder.ResourceType;
import fr.ippon.wip.util.WIPUtil;

import org.xml.sax.SAXException;

import com.google.common.base.Stopwatch;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.xml.transform.TransformerException;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * JSTransformer implements the WIPTransformer interface that defines the
 * transform method used to rewrite the JavaScript code of the distant
 * application.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 * @author Yohan Legat
 */
public class JSTransformer extends AbstractTransformer {

	private static final Logger LOG = Logger.getLogger(JSTransformer.class.getName());

	/**
	 * A PortletResponse used by the rewriter to create ResourceUrls
	 */
	private final PortletResponse response;

	/**
	 * The instance of WIPConfiguration
	 */
	private final WIPConfiguration wipConfig;

	public static ThreadLocal<Long> timeProcess = new ThreadLocal<Long>();
	
	/**
	 * Create a new JSTransformer by initializing the rewriter, getting the
	 * portlet configuration and initializing the other fields with given
	 * values.
	 * 
	 * @param request
	 *            the Portlet request
	 * @param response
	 *            the Portlet response used to create ResourceURLs
	 */
	public JSTransformer(PortletRequest request, PortletResponse response, String actualUrl) {
		super(request, actualUrl);

		this.response = response;
		this.wipConfig = WIPUtil.getConfiguration(request);
	}

	/**
	 * Check if the script from the given URL has to be rewritten
	 * 
	 * @param url
	 *            the script URL
	 * @return a boolean indicating if the script has to be rewritten
	 */
	public boolean isIgnoredScript(String url) {
		for (String regex : wipConfig.getScriptsToIgnore()) {
			try {
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(url);
				if (m.find())
					return true;
			} catch (PatternSyntaxException e) {
				LOG.log(Level.WARNING, "Could not parse ignoredScript regex: ", e);
			}
		}

		return false;
	}

	private String rewrite(String regex, String input) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			int group = extractGroup(matcher);
			if (group > 0) {
				String before = input.substring(matcher.start(), matcher.start(group));
				String url = matcher.group(group);
				String after = input.substring(matcher.end(group), matcher.end());
				matcher.appendReplacement(sb, before + urlFactory.createProxyUrl(url, "GET", "AJAX", response) + after);
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Transform the given Javascript code: rewrite Ajax URLs and regular URLs
	 * defined in the portlet configuration.
	 * 
	 * @param input
	 *            the string corresponding to the original JavaScript code
	 * @return a string corresponding to the transformed JavaScript code
	 * @throws TransformerException
	 */
	public String transform(String input) throws SAXException, IOException, TransformerException {
		super.transform(input);
		Stopwatch stopwatch = new Stopwatch().start();

		String url;

		// CUSTOM
		// ------------------------------------------------------------------

		// ---------------------------------------------------------------------------

		Map<String, PostRequestBuilder.ResourceType> jsUrls = wipConfig.getJavascriptResourcesMap();
		for (String jsUrl : jsUrls.keySet()) {
			url = jsUrl;
			// Add \\ for regex characters like "?"
			if (url.contains("?"))
				url = url.replace("?", "\\?");
			// Rewrite accoding to the URL type
			input = input.replaceAll(url, urlFactory.createProxyUrl(jsUrl, "GET", jsUrls.get(jsUrl).name(), response));
		}

		// Rewriting URLs
		String regex = wipConfig.getJsRegex();
		input = rewrite(regex, input);

		input = transformAjaxJQuery(input);

		timeProcess.set(stopwatch.elapsedMillis());
		return input;
	}

	/**
	 * Compute JQuery AJAX call. Only compatible with $.get and $.post for now,
	 * and the url must be a litteral.
	 * 
	 * @param input
	 * @return
	 */
	private String transformAjaxJQuery(String input) {
		for (String method : new String[] { "post", "get" }) {
			Pattern pattern = Pattern.compile("\\$\\." + method + "\\s*\\(\\s*");
			Matcher matcher = pattern.matcher(input);
			while (matcher.find()) {
				int start = matcher.end();
				int end;
				char quoteType = input.charAt(start);
				boolean isQuoted;
				String link;

				isQuoted = (quoteType == '\'' || quoteType == '"');
				// if not quoted then the url is not a litteral, we don't manage
				// variable yet
				if (!isQuoted)
					return input;

				start++;
				end = input.indexOf(quoteType, start);

				link = input.substring(start, end).trim();
				link = urlFactory.createProxyUrl(link, method.toUpperCase(), ResourceType.AJAX.name(), response);
				input = input.substring(0, start) + link + input.substring(end);
			}
		}

		return input;
	}
}
