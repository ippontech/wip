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

    public static final String COOKIE_NAME = "LtpaToken";

    private static final Logger LOG = Logger.getLogger(LtpaCookieUtil.class.getName());

    public static String[] getCookieValueAndDomain(PortletRequest request, WIPConfiguration wipConfig) {
        // Get credentials and LTPA secret
        String credentials = getCredentials(request, wipConfig.getCredentialProviderClassName());
        String[] ltpaTuple = getLtpaSecret(request, wipConfig.getLtpaSecretProviderClassName());

        if (credentials != null && ltpaTuple != null && ltpaTuple.length == 2) {
            String domain = ltpaTuple[0];
            String ltpaSecret = ltpaTuple[1];
            String ltpaToken;
            try {
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.add(Calendar.MINUTE, -10);
                // Create token
                ltpaToken = LtpaLibrary.createLtpaToken(credentials, calendar,
                        120, ltpaSecret);
            } catch (Exception e) {
                throw new UnsupportedOperationException("Could not create LTPA token", e);
            }

            return new String[]{ltpaToken, domain};
        }
        throw new UnsupportedOperationException("Could not create LTPA token");
    }

    /**
     * Generate LTPA cookie from credentials and LTPA secret.
     */
    public static String getLtpaCookie(PortletRequest request, WIPConfiguration wipConfig) {
        String ltpaCookie;

        String[] valueAndDomain = getCookieValueAndDomain(request, wipConfig);
        if (valueAndDomain[1] != null && valueAndDomain[1].compareTo("") != 0) {
            ltpaCookie = String.format(COOKIE_NAME + "=%s; domain=.%s; path=/", valueAndDomain[0], valueAndDomain[1]);
        } else {
            ltpaCookie = String.format(COOKIE_NAME + "=%s; path=/", valueAndDomain[0]);
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
        Class[] paramTypes = new Class[]{Class.forName("javax.portlet.PortletRequest")};
        Method m = o.getClass().getMethod(methodName, paramTypes);
        return m.invoke(o, args);
    }

}
