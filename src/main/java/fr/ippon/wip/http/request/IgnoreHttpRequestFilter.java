package fr.ippon.wip.http.request;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HttpContext;

import fr.ippon.wip.config.WIPConfiguration;

/**
 * A request filter based on the content of the ignore script parameter in the current configuration.
 * 
 * @author Yohan Legat
 *
 */
public class IgnoreHttpRequestFilter extends AbstractHttpRequestFilter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean filter(HttpHost target, HttpRequest request, HttpContext context) {
		WIPConfiguration configuration = (WIPConfiguration) context.getAttribute("WIP_CONFIGURATION");
    	HttpRequestBase base = (HttpRequestBase) request;
    	return !isDeletedScript(configuration, base.getURI().toString());
	}
	
	/**
	 * Check if the script from the given URL has to be deleted
	 * 
	 * @param url
	 *            the script URL
	 * @return a boolean indicating if the script has to be deleted
	 */
	private boolean isDeletedScript(WIPConfiguration configuration, String url) {
		for (String regex : configuration.getScriptsToDelete()) {
			try {
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(url);
				if (m.find())
					return true;
				
			} catch (PatternSyntaxException e) {
				throw e;
			}
		}

		return false;
	}
}
