package fr.ippon.wip.http.hc;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;
import fr.ippon.wip.ltpa.LtpaCookieUtil;
import fr.ippon.wip.state.PortletWindow;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HttpContext;

import javax.portlet.PortletRequest;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 03/06/12
 * Time: 07:21
 * To change this template use File | Settings | File Templates.
 */
public class LtapRequestInterceptor implements HttpRequestInterceptor {
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        PortletRequest portletRequest = HttpClientResourceManager.getInstance().getCurrentPortletRequest();
        PortletWindow windowState = PortletWindow.getInstance(portletRequest);
        WIPConfiguration wipConfig = WIPConfigurationManager.getInstance().getConfiguration(portletRequest.getWindowID());

        // If it is the first request
        if (windowState.getCurrentURI() == null) {
            // If LTPA SSO enabled for this portlet
            if (wipConfig.getLtpaSsoAuthentication()) {
                // Create LTPA cookie & add it ot store
                String[] valueAndDomain = LtpaCookieUtil.getCookieValueAndDomain(portletRequest, wipConfig);
                if (valueAndDomain != null) {
                    BasicClientCookie ltpaCookie = new BasicClientCookie(LtpaCookieUtil.COOKIE_NAME, valueAndDomain[0]);
                    if (valueAndDomain[1] != null & !valueAndDomain[1].equals("")) {
                        ltpaCookie.setDomain(valueAndDomain[1]);
                    }
                    CookieStore cookieStore = (CookieStore)context.getAttribute(ClientContext.COOKIE_STORE);
                    cookieStore.addCookie(ltpaCookie);
                }
            }
        }
    }
}
