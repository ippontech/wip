package fr.ippon.wip.http;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import java.io.IOException;

/**
 * Implements this interface to send HTTP requests to a remote host and process HTTP reponses
 * @author François Prot
 */
public interface HttpExecutor {
    public Response execute(Request request, PortletRequest portletRequest, PortletResponse portletResponse) throws IOException;

    public void login(String login, String password, PortletRequest portletRequest);

    public void logout(PortletRequest portletRequest);

    public void destroy();
}