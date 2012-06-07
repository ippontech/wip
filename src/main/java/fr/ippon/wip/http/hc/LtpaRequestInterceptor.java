package fr.ippon.wip.http.hc;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;
import fr.ippon.wip.ltpa.LtpaCookieUtil;
import fr.ippon.wip.state.PortletWindow;
import fr.ippon.wip.util.WIPUtil;

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
 * Implementation of HttpRequestInterceptor used to create a LTPA token cookie and add it to the current CookieStore
 */
class LtpaRequestInterceptor implements HttpRequestInterceptor {
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        PortletRequest portletRequest = HttpClientResourceManager.getInstance().getCurrentPortletRequest();
        PortletWindow windowState = PortletWindow.getInstance(portletRequest);
        WIPConfiguration wipConfig = WIPUtil.extractConfiguration(portletRequest);

        // If it is the first request
        if (windowState.getCurrentURL() == null) {
            // If LTPA SSO enabled for this portlet
            if (wipConfig.getLtpaSsoAuthentication()) {
                // Create LTPA cookie & add it ot store
                String[] valueAndDomain = LtpaCookieUtil.getCookieValueAndDomain(portletRequest, wipConfig);
                if (valueAndDomain != null) {
                    BasicClientCookie ltpaCookie = new BasicClientCookie(LtpaCookieUtil.COOKIE_NAME, valueAndDomain[0]);
                    if (valueAndDomain[1] != null & !valueAndDomain[1].equals("")) {
                        ltpaCookie.setDomain(valueAndDomain[1]);
                    }
                    CookieStore cookieStore = (CookieStore) context.getAttribute(ClientContext.COOKIE_STORE);
                    cookieStore.addCookie(ltpaCookie);
                }
            }
        }
    }
}
