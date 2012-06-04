package fr.ippon.wip.http.hc;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;
import fr.ippon.wip.http.Request;
import fr.ippon.wip.transformers.*;
import org.apache.http.*;
import org.apache.http.auth.AuthState;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 29/05/12
 * Time: 23:16
 * To change this template use File | Settings | File Templates.
 */
public class TransformerResponseInterceptor implements HttpResponseInterceptor {

    private static final Logger LOG = Logger.getLogger(TransformerResponseInterceptor.class.getName());

    public void process(HttpResponse httpResponse, HttpContext context) throws HttpException, IOException {
        PortletRequest portletRequest = HttpClientResourceManager.getInstance().getCurrentPortletRequest();
        PortletResponse portletResponse = HttpClientResourceManager.getInstance().getCurrentPortletResponse();
        WIPConfiguration config = WIPConfigurationManager.getInstance().getConfiguration(portletRequest.getWindowID());
        Request request = HttpClientResourceManager.getInstance().getCurrentRequest();

        if (httpResponse == null) {
            // No response -> no transformation
            LOG.warning("No response to transform for URI: " + request.getRequestedURL());
            return;
        }

        HttpEntity entity = httpResponse.getEntity();
        if (entity == null) {
            // No entity -> no transformation
            return;
        }

        ContentType contentType = ContentType.getOrDefault(entity);
        String mimeType = contentType.getMimeType();
        boolean authenticated = false;
        AuthState authState = (AuthState)context.getAttribute(ClientContext.TARGET_AUTH_STATE);
        if (authState != null & authState.getCredentials() != null) {
            authenticated = true;
        }

        // Check if actual URI must be transformed
        HttpRequest actualRequest = (HttpRequest)context.getAttribute(ExecutionContext.HTTP_REQUEST);
        HttpHost actualHost = (HttpHost)context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
        String actualURI = actualHost.toURI() + actualRequest.getRequestLine().getUri();
        if (!config.isProxyURI(actualURI)) {
            return;
        }

        WIPTransformer transformer = null;
        switch (request.getResourceType()) {
            case HTML:
                if (!mimeType.equals("text/html") && !mimeType.equals("application/xhtml+xml")) {
                    // No transformation
                    return;
                } else {
                    // HTML transformation
                    transformer = new HTMLTransformer(portletRequest, portletResponse, actualURI, authenticated);
                }
                break;
            case JS:
                // JavaScript transformation
                transformer = new JSTransformer(portletRequest, portletResponse, actualURI, authenticated);
                break;
            case CSS:
                // CSS transformation
                transformer = new CSSTransformer(portletRequest, portletResponse, actualURI, authenticated);
                break;
            case AJAX:
                if (contentType == null) {
                    // No transformation
                    return;
                } else if (mimeType.equals("text/html") || mimeType.equals("application/xhtml+xml")) {
                    // HTML transformation
                    transformer = new HTMLTransformer(portletRequest, portletResponse, actualURI, authenticated);
                } else if (mimeType.equals("text/javascript") || mimeType.equals("application/javascript")) {
                    // JavaScript transformation
                    transformer = new JSTransformer(portletRequest, portletResponse, actualURI, authenticated);
                } else if (mimeType.equals("application/json")) {
                    // JSON transformation
                    transformer = new JSONTransformer();
                }
                break;
            case RAW:
                // No transformation
                return;
        }

        try {
            String transformedContent = transformer.transform(EntityUtils.toString(entity));
            HttpEntity transformedEntity = null;
            if (contentType != null && contentType.getCharset() != null) {
                transformedEntity = new StringEntity(transformedContent, ContentType.getOrDefault(entity));
            } else {
                transformedEntity = new StringEntity(transformedContent);
            }
            httpResponse.setEntity(transformedEntity);

        } catch (SAXException e) {
            // TODO: log exception
        } catch (TransformerException e) {
            // TODO: log exception
        }
    }
}
