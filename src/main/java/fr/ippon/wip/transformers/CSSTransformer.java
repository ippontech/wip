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
import fr.ippon.wip.util.WIPUtil;

import org.xml.sax.SAXException;

import com.google.common.base.Stopwatch;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.xml.transform.TransformerException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CSSTransformer implements the WIPTransformer interface that defines the
 * transform method used to rewrite the CSS code of the distant application.
 * 
 * @author Anthony Luce
 * @author Quentin Thierry
 * @author Yohan Legat
 */
public class CSSTransformer extends AbstractTransformer {

	private static final Logger LOG = Logger.getLogger(CSSTransformer.class.getName());

	/**
	 * The WIPConfiguration instance
	 */
	private final WIPConfiguration wipConfig;

	/**
	 * An empty PortletResponse used to create ResourceURLs
	 */
	private final PortletResponse response;

	public static ThreadLocal<Long> timeProcess = new ThreadLocal<Long>();

	/**
	 * Create a new CSSTransformer by getting the portlet configuration,
	 * initializing the CSS rewriter and setting the PortletResponse.
	 * 
	 * @param request
	 *            The PortletRequest used to get configuration
	 * @param response
	 *            The PortletResponse used to build ResourceURLs
	 * @throws MalformedURLException
	 */
	public CSSTransformer(PortletRequest request, PortletResponse response, String actualUrl) {
		super(request, actualUrl);
		this.response = response;
		wipConfig = WIPUtil.getConfiguration(request);
	}

	/**
	 * Get custom CSS code in the portlet configuration
	 * 
	 * @return the custom CSS code as a string
	 */
	public String getCustomCss() {
		return wipConfig.getCustomCss();
	}

	/**
	 * Rewrite every url found thanks to the given regex in the input String to
	 * ResourceURL if the user is authenticated, and to absolute URLs if he's
	 * not
	 * 
	 * @param regex
	 *            The regex used to find URLs in the CSS code
	 * @param input
	 *            The CSS code that we want to rewrite
	 * @param response
	 *            An empty PortletResponse used to create the ResourceURLs
	 * @return The CSS code, whose all URLs have been rewrited
	 */
	protected String rewrite(String regex, String input, PortletResponse response) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			try {
				int group = extractGroup(matcher);
				if (group > 0) {
					String before = input.substring(matcher.start(), matcher.start(group));
					String url = matcher.group(group);
					String after = input.substring(matcher.end(group), matcher.end());
					if (before.startsWith("@import") || before.startsWith("@CHARSET")) {
						matcher.appendReplacement(sb, before + urlFactory.createProxyUrl(url, "GET", "CSS", response) + after);
					} else {
						matcher.appendReplacement(sb, before + urlFactory.createProxyUrl(url, "GET", "RAW", response) + after);
					}
				}
			} catch (IllegalArgumentException e) {
				LOG.warning("Error parsing URL in CSS: " + e.getMessage());
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Transform the given CSS code: add the prefix defined in portlet config
	 * before each selectors, rewrite URL thanks to the CSS regex defined in
	 * config, remove absolute positionning and add custom CSS.
	 * 
	 * @param input
	 *            the string corresponding to the CSS code
	 * @return a string corresponding to the transformed CSS code
	 * @throws TransformerException
	 */
	public String transform(String input) throws SAXException, IOException, TransformerException {
		super.transform(input);

		Stopwatch stopwatch = new Stopwatch().start();
		if (wipConfig.isEnableCssRewriting()) {
			// Getting prefix
			final String wip = "\n." + wipConfig.getPortletDivId() + " ";

			// removing comments
			input = input.replaceAll("\\/\\*[^*]*\\*+([^/*][^*]*\\*+)*\\/", "");

			// Removing all double white spaced characters
			input = input.replaceAll("\\r|\\n", "");
			input = input.replaceAll(System.getProperty("line.separator"), "");
			input = " " + input;

			// Parsing the file to add the wip prefix before selectors
			if (wipConfig.isAddPrefix()) {
				StringBuilder aux = new StringBuilder();
				String imported = "";
				String selector;

				int index = 0;
				int blocStart = input.indexOf("{", 0);
				int blocEnd = input.indexOf("}", 0);

				if (blocStart > -1) {
					do {
						selector = input.substring(index + 1, blocStart).trim();
						if (selector.startsWith("@import") || selector.startsWith("@CHARSET")) {
							// Get import and save it
							int i = input.indexOf(";", index);
							imported += input.substring(index + 1, i + 1);
							index = i;

						} else {
							if (selector.startsWith("@media")) {
								// this code should transform css code inside
								// @media, but it make the browser freezing...
								// Pattern pattern = Pattern.compile("\\s*\\{");
								// Matcher matcher =
								// pattern.matcher(input).region(index + 1,
								// input.length());
								// matcher.find();
								// int startMediaCss = matcher.end();
								//
								// pattern = Pattern.compile("\\}\\s*\\}");
								// matcher =
								// pattern.matcher(input).region(startMediaCss,
								// input.length());
								// matcher.find();
								// int endMediaCss = matcher.start() + 1;
								// blocEnd = matcher.end();
								//
								// String transformedMediaCss =
								// transform(input.substring(startMediaCss,
								// endMediaCss));
								// String result = "\n" + selector + "{" +
								// transformedMediaCss + "}";
								// aux.append(result);

								// Copy the entire bloc without modification
								blocEnd = input.indexOf("}}", index) + 1;
								if (blocEnd < 1)
									blocEnd = input.indexOf("} }", index) + 1;

								aux.append("\n" + selector);
								aux.append(input.substring(blocStart, blocEnd + 1));

							} else {
								if (selector.contains(","))
									selector = selector.replaceAll(",", "," + wip);
								aux.append("\n" + wip + selector);
								aux.append(input.substring(blocStart, blocEnd + 1));
							}

							// Next step
							index = blocEnd;
							blocStart = input.indexOf("{", index);
							blocEnd = input.indexOf("}", blocStart);
						}
					} while (blocStart > -1);

					input = imported + aux.toString();
				}
			}

			// Rewriting URLs
			String regex = wipConfig.getCssRegex();
			input = rewrite(regex, input, response);

			// Removing position: absolute;
			if (!wipConfig.isAbsolutePositioning())
				input = input.replaceAll("absolute", "relative");

		}

		timeProcess.set(stopwatch.elapsedMillis());
		return input;
	}
}
