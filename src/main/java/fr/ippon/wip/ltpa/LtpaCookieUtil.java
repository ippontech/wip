package fr.ippon.wip.ltpa;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.ltpa.token.LtpaLibrary;

import javax.portlet.PortletRequest;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The LtpaCookieUtil class is used to generate a LtpaCookie
 * that enable a LTPA SSO authentication.
 */
public class LtpaCookieUtil {

    private static final Logger LOG = Logger.getLogger(LtpaCookieUtil.class.getName());

	/**
	 * Generate LTPA cookie from credentials and LTPA secret.
	 */
	public static String getLtpaCookie(PortletRequest request, WIPConfiguration wipConfig) {
		String ltpaCookie = null;
		
		// Get credentials and LTPA secret
		String credentials = getCredentials(request, wipConfig.getCredentialProviderClassName());
		String[] ltpaTuple = getLtpaSecret(request, wipConfig.getLtpaSecretProviderClassName());
		
		if (credentials != null && ltpaTuple != null && ltpaTuple.length == 2) {
			String domain = ltpaTuple[0];
			String ltpaSecret = ltpaTuple[1];		
			String ltpaToken = "";
			try {
				GregorianCalendar calendar = new GregorianCalendar();
				calendar.add(Calendar.MINUTE, -10);
				// Create token
				ltpaToken = LtpaLibrary.createLtpaToken(credentials, calendar,
						120, ltpaSecret);
			} catch (Exception e) {
                LOG.log(Level.WARNING, "Could not create LTPA token", e);
			}
			// Use Set-Cookie instead of javax.servlet.http.Cookie
			// API cause some Tomcat version prepend the character '"'
			// before and after cookie value if the value is not URI encoded
			if (domain != null && domain.compareTo("") != 0)
				ltpaCookie = String.format("LtpaToken=%s; domain=.%s; path=/",
						ltpaToken, domain);
			else
				ltpaCookie = String.format("LtpaToken=%s; path=/", ltpaToken);
		}
		return ltpaCookie;
	}

	/**
	 * Get LTPA secret by invoking the method getLtpaSecret from
	 * the class that implements LtpaSecretProvider.
	 */
	private static String[] getLtpaSecret(PortletRequest request, String className) {
		String[] ltpaSecret = null;
		try {
			Class clazz = Class.forName(className);
			Object o = clazz.newInstance();
			ltpaSecret = (String[]) invokeMethod("getLtpaSecret", o, new Object[]{request});
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Could not get LTPA secret with " + className, e);
		}
		return ltpaSecret;
	}
	
	/**
	 * Get credentials by invoking the method getCredentials from
	 * the class that implements CredentialProvider.
	 */
	private static String getCredentials(PortletRequest request, String className) {
		String credentials = null;
		try {
			Class clazz = Class.forName(className);
			Object o = clazz.newInstance();
			credentials = (String) invokeMethod("getCredentials", o, new Object[]{request});
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Could not get user credentials with" + className, e);
		}
		return credentials;
	}

	/** 
	 * Invoke method 'methodName' from object 'o' with arguments 'args'
	 */
	private static Object invokeMethod(String methodName, Object o, Object[] args)
			throws Exception {
		Class[] paramTypes = new Class[] {Class.forName("javax.portlet.PortletRequest")};
		Method m = o.getClass().getMethod(methodName, paramTypes);
		return m.invoke(o, args);
	}
	
}
