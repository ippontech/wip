package fr.ippon.wip.cookies;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Implementation of the HttpSessionListener interface.
 * Referenced in web.xml
 * In charge of cleaning cookies stored in memory for the current session
 * @author François Prot
 */

public class CookiesCleanupListener implements HttpSessionListener {
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        // Nothing to do
    }

    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        CookiesManagerImpl.getInstance().clearCookies(httpSessionEvent.getSession().getId());
    }
}
