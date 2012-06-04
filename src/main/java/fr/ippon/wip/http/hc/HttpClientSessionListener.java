package fr.ippon.wip.http.hc;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 01/06/12
 * Time: 11:00
 * To change this template use File | Settings | File Templates.
 */
public class HttpClientSessionListener implements HttpSessionListener {
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        // Nothing to do
    }

    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpClientResourceManager.getInstance().releaseSessionResources(httpSessionEvent.getSession().getId());
    }
}
