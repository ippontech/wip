package fr.ippon.wip.state;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import java.util.List;
import java.util.UUID;

/**
 * This class is a container for all state datas associated to a given sessionID/windowID
 *
 * Instances of this class are obtained by the static method #getInstance
 *
 * @author François Prot
 */
public class PortletWindow {
    private UUID responseID;
    private String currentURL;
    private List<String> requestedAuthSchemes;
    private boolean authenticated = false;

    private static final String PORTLET_SESSION_KEY = "wip.window.state";

    private PortletWindow() {
    }

    /**
     * UUID of a Response obtained in the ACTION phase that has not already been sent to client.
     *
     * Used to get a Response instance from ResponseStore.
     *
     * @return
     */
    public UUID getResponseID() {
        return responseID;
    }

    public void setResponseID(UUID responseID) {
        this.responseID = responseID;
    }

    /**
     * The remote URL for this sessionID/windowID.
     *
     * If the remote host send "Redirect" response, the URL is the final target location
     *
     * @return
     */
    public String getCurrentURL() {
        return currentURL;
    }

    public void setCurrentURL(String currentURL) {
        this.currentURL = currentURL;
    }

    /**
     * Authentication schemes accepted by remote host.
     *
     * Is null is no authentication is requested.
     *
     * @return
     */
    public List<String> getRequestedAuthSchemes() {
        return requestedAuthSchemes;
    }

    public void setRequestedAuthSchemes(List<String> requestedAuthSchemes) {
        this.requestedAuthSchemes = requestedAuthSchemes;
    }

    /**
     * Is the user authenticated on the remote host
     *
     * @return
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    /**
     * Get the instance associated with this PortletSession (in the PORTLET_SCOPE) if exists.
     * Else create a new one and update session.
     *
     * @param request
     * @return
     */
    public static PortletWindow getInstance(PortletRequest request) {
        PortletSession session = request.getPortletSession();
        PortletWindow state;
        synchronized (session) {
            state = (PortletWindow) session.getAttribute(PORTLET_SESSION_KEY, PortletSession.PORTLET_SCOPE);

            if (state == null) {
                state = new PortletWindow();
                WIPConfiguration config = WIPConfigurationManager.getInstance().getConfiguration(request.getWindowID());
                state.setCurrentURL(config.getInitUrl().toExternalForm());
                session.setAttribute(PORTLET_SESSION_KEY, state, PortletSession.PORTLET_SCOPE);
            }
        }
        return state;
    }

    /**
     * This method must be called when the state of a particular session/window must be cleared
     * (on remote logout for example)
     *
     * @param request
     */
    public static void clearInstance(PortletRequest request) {
        PortletSession session = request.getPortletSession();
        session.removeAttribute(PORTLET_SESSION_KEY, PortletSession.PORTLET_SCOPE);
    }
}
