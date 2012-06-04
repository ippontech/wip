package fr.ippon.wip.http;

import fr.ippon.wip.util.WIPUtil;
import org.apache.commons.io.IOUtils;

import javax.portlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.text.MessageFormat;

/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 01/06/12
 * Time: 13:54
 * To change this template use File | Settings | File Templates.
 */
public class Response {
    private InputStream stream;
    private Charset charset = Charset.defaultCharset();
    private String mimeType;
    private String uri;
    private int returnCode;
    private boolean portalURLComputed = false;

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        if (charset != null) {
            this.charset = charset;
        }
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public boolean isPortalURLComputed() {
        return portalURLComputed;
    }

    public void setPortalURLComputed(boolean portalURLComputed) {
        this.portalURLComputed = portalURLComputed;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFileName() {
        String fileName = uri;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void dispose () {
        try {
            stream.close();
        } catch (IOException e) {
            // TODO: add log ?
        }
    }

    public void printResponseContent (PortletRequest portalRequest, MimeResponse portalResponse, boolean printLogout) throws IOException {
        if (portalRequest instanceof RenderRequest && !portalURLComputed) {
            computePortalURL(portalRequest, portalResponse);
        }

        if (portalResponse instanceof ResourceResponse) {
            // Set content-type & return code
            portalResponse.setContentType(mimeType);
            portalResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(returnCode));
        }

        if (isBinary ()) {
            OutputStream os = portalResponse.getPortletOutputStream();
            IOUtils.copy(stream, os);
            os.close();
        } else {
            PrintWriter writer = portalResponse.getWriter();

            if (portalResponse instanceof RenderResponse && printLogout) {
                writer.print(getLogoutButton((RenderResponse)portalResponse));
            }

            writer.print(IOUtils.toCharArray(stream, charset));
            writer.close();
        }
        dispose ();
    }

    public void sendResponse (HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType(mimeType);
        httpServletResponse.setCharacterEncoding(charset.name());
        httpServletResponse.setHeader("Content-Disposition", "attachment; filename=\"" + getFileName() + "\"");
        if (isBinary ()) {
            OutputStream os = httpServletResponse.getOutputStream();
            IOUtils.copy(stream, os);
            os.close();
        } else {
            String content = IOUtils.toString(stream, charset);
            httpServletResponse.setContentLength(content.length());
            PrintWriter writer = httpServletResponse.getWriter();
            writer.print(content);
        }
        dispose ();
    }

    private void computePortalURL (PortletRequest portletRequest, MimeResponse portalResponse) {
        try {
            UrlFactory urlFactory = new UrlFactory(portletRequest);
            String originalContent = IOUtils.toString(stream, charset);
            int startIndex = 0;
            int endIndex = 0;
            int previousLast = 0;
            StringBuilder content = new StringBuilder();
            while ((startIndex = originalContent.indexOf(UrlFactory.TEMP_URL_ENCODED_SEPARATOR, previousLast)) > 0) {
                endIndex = originalContent.indexOf(UrlFactory.TEMP_URL_ENCODED_SEPARATOR, startIndex + UrlFactory.TEMP_URL_ENCODED_SEPARATOR.length());
                if (endIndex < 0) {
                    throw new RuntimeException("No end token for temp URL");
                }
                String tmpUrl = originalContent.substring(startIndex + UrlFactory.TEMP_URL_ENCODED_SEPARATOR.length(), endIndex);
                content.append(originalContent, previousLast, startIndex);
                content.append(urlFactory.createPortalUrl(tmpUrl, portalResponse));
                previousLast = endIndex + UrlFactory.TEMP_URL_ENCODED_SEPARATOR.length();
            }
            content.append(originalContent, previousLast, originalContent.length());
            if (charset != null) {
                stream = new ByteArrayInputStream(content.toString().getBytes(charset));
            } else {
                stream = new ByteArrayInputStream(content.toString().getBytes());
            }
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

    private boolean isBinary () {
        if (mimeType.startsWith("text") || mimeType.startsWith("application/xhtml+xml") || mimeType.startsWith("application/json") || mimeType.startsWith("application/xml") || mimeType.startsWith("application/javascript")) {
            return false;
        }
        return true;
    }
}
