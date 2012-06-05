package fr.ippon.wip.http.hc;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Implementation of HttpSessionListener. Must be declared in web.xml to release
 * resources associated to a session when it is destroyed.
 *
 * @author François Prot
 */
public class HttpClientSessionListener implements HttpSessionListener {
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        // Nothing to do
    }

    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpClientResourceManager.getInstance().releaseSessionResources(httpSessionEvent.getSession().getId());
    }
}
