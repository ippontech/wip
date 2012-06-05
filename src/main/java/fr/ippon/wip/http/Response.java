package fr.ippon.wip.http;

import fr.ippon.wip.transformers.ActionToRenderTransformer;
import fr.ippon.wip.util.WIPUtil;
import org.apache.commons.io.IOUtils;

import javax.portlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;

/**
 * Container class for all date describing an HTTP response from a remote host
 *
 * @author François Prot
 */
public class Response {
    private InputStream stream;
    private Charset charset;
    private String mimeType;
    private String url;
    private int responseCode;
    private boolean portalURLComputed;

    public Response (InputStream content, Charset charset, String mimeType, String url, int responseCode, boolean portalURLComputed) {
        this.stream = content;
        if (charset != null) {
            this.charset = charset;
        } else {
            this.charset = Charset.defaultCharset();
        }
        this.mimeType = mimeType;
        this.url = url;
        this.responseCode = responseCode;
        this.portalURLComputed = portalURLComputed;
    }

    public boolean isHtml () {
        return mimeType.equals("text/html") || mimeType.equals("application/xhtml+xml");
    }

    public boolean isBinary() {
        return !(mimeType.startsWith("text") || mimeType.startsWith("application/xhtml+xml") || mimeType.startsWith("application/json") || mimeType.startsWith("application/xml") || mimeType.startsWith("application/javascript"));
    }

    /**
     * Infer filename from URL
     *
     * @return
     */
    public String getFileName() {
        String fileName = url;
        int lastSlashIndex = fileName.lastIndexOf('/');
        if (lastSlashIndex >= 0) {
            fileName = fileName.substring(lastSlashIndex);
            int questionMarkIndex = fileName.indexOf('?');
            if (questionMarkIndex >= 0) {
                fileName = fileName.substring(0, questionMarkIndex);
            }
        }
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public void dispose() {
        try {
            stream.close();
        } catch (IOException e) {
            // TODO: add log ?
        }
    }

    /**
     * Print the response content to a MimeResponse (RenderResponse or ResourceResponse)
     *
     * In the RESOURCE phase, this method can send binary content and set Content-Type and HTTP status code
     *
     * @param portalRequest
     * @param portalResponse
     * @param printLogout
     * @throws IOException
     */
    public void printResponseContent(PortletRequest portalRequest, MimeResponse portalResponse, boolean printLogout) throws IOException {
        if (portalRequest instanceof RenderRequest && !portalURLComputed) {
            computePortalURL(portalRequest, portalResponse);
        }

        if (portalResponse instanceof ResourceResponse) {
            // Set content-type & return code
            portalResponse.setContentType(mimeType);
            portalResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(responseCode));
        }

        if (isBinary()) {
            if (portalResponse instanceof ResourceResponse) {
                OutputStream os = portalResponse.getPortletOutputStream();
                IOUtils.copy(stream, os);
                os.close();
            }
        } else {
            PrintWriter writer = portalResponse.getWriter();

            if (portalResponse instanceof RenderResponse && printLogout) {
                writer.print(getLogoutButton((RenderResponse) portalResponse));
            }

            writer.print(IOUtils.toCharArray(stream, charset));
            writer.close();
        }
        dispose();
    }

    /**
     * Print the response content to and HttpServletResponse
     *
     * Set appropriate Content-Type, Charset and HTTP status code.
     * Force Content-Disposition to "attachment".
     * Can handle binary content.
     *
     * @param httpServletResponse
     * @throws IOException
     */
    public void sendResponse(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType(mimeType);
        httpServletResponse.setCharacterEncoding(charset.name());
        httpServletResponse.setStatus(responseCode);
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + getFileName() + "\"");
        if (isBinary()) {
            OutputStream os = httpServletResponse.getOutputStream();
            IOUtils.copy(stream, os);
            os.close();
        } else {
            String content = IOUtils.toString(stream, charset);
            httpServletResponse.setContentLength(content.length());
            PrintWriter writer = httpServletResponse.getWriter();
            writer.print(content);
        }
        dispose();
    }

    /**
     * This method is an ugly but necessary hack in order to permit content transformation
     * in the ACTION phase when it is not possible to create PortletURL.
     *
     * This method parse the response content to find all links to rewrite into PortletURL
     *
     * @param portletRequest
     * @param mimeResponse
     */
    private void computePortalURL(PortletRequest portletRequest, MimeResponse mimeResponse) {
        try {
            String originalContent = IOUtils.toString(stream, charset);
            ActionToRenderTransformer transformer = new ActionToRenderTransformer(portletRequest, mimeResponse);
            String transformedContent = transformer.transform(originalContent);
            stream = new ByteArrayInputStream(transformedContent.getBytes(charset));
        } catch (IOException ioe) {
            throw new RuntimeException("Error computing portal URLs", ioe);
        }
        portalURLComputed = true;
    }

    private String getLogoutButton(RenderResponse response) {
        PortletURL logout = response.createActionURL();
        logout.setParameter("auth", "logout");
        String message = WIPUtil.getMessage("wip.auth.logout", response.getLocale());
        return MessageFormat.format(message, logout.toString());
    }
}
