package fr.ippon.wip.state;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import java.util.List;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 01/06/12
 * Time: 10:05
 * To change this template use File | Settings | File Templates.
 */
public class PortletWindow {
    private UUID requestResponseID;
    private boolean renderPending = false;
    private String currentURI;
    private List<String> requestedAuthSchemes;
    private boolean authenticated = false;

    public static final String PORTLET_SESSION_KEY = "wip.window.state";

    public PortletWindow() {
    }

    public UUID getRequestResponseID() {
        return requestResponseID;
    }

    public void setRequestResponseID(UUID requestResponseID) {
        this.requestResponseID = requestResponseID;
    }

    public boolean isRenderPending() {
        return renderPending;
    }

    public void setRenderPending(boolean renderPending) {
        this.renderPending = renderPending;
    }

    public String getCurrentURI() {
        return currentURI;
    }

    public void setCurrentURI(String currentURI) {
        this.currentURI = currentURI;
    }

    public List<String> getRequestedAuthSchemes() {
        return requestedAuthSchemes;
    }

    public void setRequestedAuthSchemes(List<String> requestedAuthSchemes) {
        this.requestedAuthSchemes = requestedAuthSchemes;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public static PortletWindow getInstance (PortletRequest request) {
        PortletSession session = request.getPortletSession();
        PortletWindow state = (PortletWindow)session.getAttribute(PORTLET_SESSION_KEY, PortletSession.PORTLET_SCOPE);

        if (state == null) {
            state = new PortletWindow();
            session.setAttribute(PORTLET_SESSION_KEY, state, PortletSession.PORTLET_SCOPE);
        }

        return state;
    }

    public static void clearInstance (PortletRequest request) {
        PortletSession session = request.getPortletSession();
        session.removeAttribute(PORTLET_SESSION_KEY, PortletSession.PORTLET_SCOPE);
    }
}
