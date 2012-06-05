package fr.ippon.wip.http.hc;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.WIPConfigurationManager;
import fr.ippon.wip.http.Request;
import fr.ippon.wip.transformers.*;
import org.apache.http.*;
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
import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * Implementation of HttpResponseInterceptor in charge of processing all content transformations (HTML, CSS, JavaScript)
 * and clipping.
 * @author François Prot
 */
class TransformerResponseInterceptor implements HttpResponseInterceptor {

    private static final Logger LOG = Logger.getLogger(TransformerResponseInterceptor.class.getName());

    /**
     * If httpResponse must be transformed, creates an instance of WIPTransformer,
     *  executes WIPTransformer#transform on the response content and updates the
     *  response entity accordingly.
     *
     * @param httpResponse
     * @param context
     * @throws HttpException
     * @throws IOException
     */
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

        // Check if actual URI must be transformed
        HttpRequest actualRequest = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        HttpHost actualHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
        String actualURI = actualHost.toURI() + actualRequest.getRequestLine().getUri();
        if (!config.isProxyURI(actualURI)) {
            return;
        }

        // Creates an instance of Transformer depending on ResourceType and MimeType
        // May returns directly if no transformation is need
        WIPTransformer transformer = null;
        switch (request.getResourceType()) {
            // Direct link or form submit
            case HTML:
                if (!mimeType.equals("text/html") && !mimeType.equals("application/xhtml+xml")) {
                    // No transformation
                    return;
                } else {
                    // HTML transformation
                    transformer = new HTMLTransformer(portletRequest, portletResponse);
                }
                break;
            case JS:
                // JavaScript transformation
                transformer = new JSTransformer(portletRequest, portletResponse);
                // Empty content
                if (((JSTransformer) transformer).isDeletedScript(actualURI)) {
                    // Send à 404 empty response
                    emtpyResponse(httpResponse);
                    return;
                } else if (((JSTransformer) transformer).isIgnoredScript(actualURI)) {
                    // No transformation
                    return;
                }
                break;
            case CSS:
                // CSS transformation
                transformer = new CSSTransformer(portletRequest, portletResponse);
                break;
            case AJAX:
                if (mimeType == null) {
                    // No transformation
                    return;
                } else if (mimeType.equals("text/html") || mimeType.equals("application/xhtml+xml")) {
                    // HTML transformation
                    transformer = new HTMLTransformer(portletRequest, portletResponse);
                } else if (mimeType.equals("text/javascript") || mimeType.equals("application/javascript")) {
                    // JavaScript transformation
                    transformer = new JSTransformer(portletRequest, portletResponse);
                } else if (mimeType.equals("application/json")) {
                    // JSON transformation
                    transformer = new JSONTransformer();
                } else {
                    // No transformation
                    return;
                }
                break;
            case RAW:
                // No transformation
                return;
        }

        // Call WIPTransformer#transform method and update the response Entity object
        try {
            String transformedContent = transformer.transform(EntityUtils.toString(entity));
            HttpEntity transformedEntity;
            if (contentType.getCharset() != null) {
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

    private void emtpyResponse(HttpResponse httpResponse) {
        EntityUtils.consumeQuietly(httpResponse.getEntity());
        httpResponse.setEntity(new StringEntity("", Charset.defaultCharset()));
        httpResponse.setStatusCode(HttpStatus.SC_NOT_FOUND);
        httpResponse.setReasonPhrase("Deleted by WIP");
    }
}
