package fr.ippon.wip.http;

import javax.portlet.*;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 01/06/12
 * Time: 07:47
 * To change this template use File | Settings | File Templates.
 */
public interface HttpExecutor {
    public Response execute(Request request, PortletRequest portletRequest, PortletResponse portletResponse) throws IOException;

    public void login (String login, String password, PortletRequest portletRequest);

    public void logout (PortletRequest portletRequest);

    public void destroy ();
}