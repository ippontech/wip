package fr.ippon.wip.http.hc;

import fr.ippon.wip.state.PortletWindow;
import fr.ippon.wip.http.Request;
import fr.ippon.wip.http.HttpExecutor;
import fr.ippon.wip.http.Response;
import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.portlet.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: fprot
 * Date: 01/06/12
 * Time: 07:49
 * To change this template use File | Settings | File Templates.
 */
public class HttpClientExecutor implements HttpExecutor {

    public Response execute(Request request, PortletRequest portletRequest, PortletResponse portletResponse) throws IOException {
        Response response = null;
        HttpClientResourceManager resourceManager = HttpClientResourceManager.getInstance();
        try {
            HttpClient client = resourceManager.getHttpClient(portletRequest);
            HttpContext context = resourceManager.initExecutionContext(portletRequest, portletResponse, request);
            HttpUriRequest httpRequest = null;
            if (request.getHttpMethod() == Request.HttpMethod.POST) {
                httpRequest = createPostRequest(request);
            } else {
                httpRequest = createGetRequest(request);
            }

            HttpResponse httpResponse = null;
            try {
                httpResponse = client.execute(httpRequest, context);

                PortletWindow portletWindow = PortletWindow.getInstance(portletRequest);

                int statusCode = httpResponse.getStatusLine().getStatusCode();
                List<String> schemes = null;
                if (statusCode == HttpStatus.SC_UNAUTHORIZED){
                    // Check what authentication scheme are required
                    schemes = new ArrayList<String>();
                    for (Header authHeader : httpResponse.getHeaders(HttpHeaders.WWW_AUTHENTICATE)) {
                        String headerValue = authHeader.getValue();
                        schemes.add(headerValue.split(" ")[0]);
                    }
                    portletWindow.setRequestedAuthSchemes(schemes);
                } else {
                    portletWindow.setRequestedAuthSchemes(null);
                }

                AuthState authState = (AuthState)context.getAttribute(ClientContext.TARGET_AUTH_STATE);
                portletWindow.setAuthenticated(authState != null & authState.getCredentials() != null);

                // TODO: not thread-safe !
                // Get real (ie. redirected) URI
                HttpUriRequest actualRequest = (HttpUriRequest) context.getAttribute(
                        ExecutionContext.HTTP_REQUEST);
                HttpHost actualHost = (HttpHost)  context.getAttribute(
                        ExecutionContext.HTTP_TARGET_HOST);
                String actualUri = (actualRequest.getURI().isAbsolute()) ? actualRequest.getURI().toString() : (actualHost.toURI() + actualRequest.getURI());

                response = createResponse(httpResponse, actualUri);
                response.setPortalURLComputed(!(portletResponse instanceof ActionResponse));
            } catch (RuntimeException rte) {
                if (httpResponse != null && httpResponse.getEntity() != null) {
                    EntityUtils.consume(httpResponse.getEntity());
                }
                throw rte;
            }
        } finally {
            resourceManager.releaseThreadResources();
        }
        return response;
    }

    public void login(String login, String password, PortletRequest portletRequest) {
        HttpClientResourceManager resourceManager = HttpClientResourceManager.getInstance();
        CredentialsProvider credentialsProvider = resourceManager.getCredentialsProvider(portletRequest);
        PortletWindow portletWindow = PortletWindow.getInstance(portletRequest);
        List<String> schemes = portletWindow.getRequestedAuthSchemes();

        if (schemes.contains("Basic")) {
            // Creating basic credentials
            AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, "Basic");
            Credentials credentials = new UsernamePasswordCredentials(login, password);
            credentialsProvider.setCredentials(scope, credentials);
        }
        if (schemes.contains("NTLM")) {
            // Creating ntlm credentials
            AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, "NTLM");
            Credentials credentials = new NTCredentials( login, password, "", "" );
            credentialsProvider.setCredentials(scope, credentials);
        }
    }

    public void logout(PortletRequest portletRequest) {
        HttpClientResourceManager resourceManager = HttpClientResourceManager.getInstance();
        CredentialsProvider credentialsProvider = resourceManager.getCredentialsProvider(portletRequest);

        // Clear credentials
        credentialsProvider.clear();

        // Clear state
        PortletWindow.clearInstance(portletRequest);
    }

    public void destroy() {
        HttpClientResourceManager.releaseGlobalResources();
    }

    private HttpUriRequest createPostRequest (Request request) {
        HttpPost postRequest = new HttpPost(request.getRequestedURL());
        Map<String, String[]> paramMap = request.getParameterMap();

        if (paramMap != null) {
            List<NameValuePair> httpParams = new LinkedList<NameValuePair>();
            for (Map.Entry<String,String[]> entry : paramMap.entrySet()) {
                for (String value : entry.getValue()) {
                    httpParams.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
            HttpEntity formEntity = new UrlEncodedFormEntity(httpParams, ContentType.APPLICATION_FORM_URLENCODED.getCharset());
            postRequest.setEntity(formEntity);
        }

        return postRequest;
    }

    private HttpUriRequest createGetRequest (Request request) {
        HttpGet getRequest = new HttpGet(request.getRequestedURL());

        return getRequest;
    }

    private Response createResponse (HttpResponse httpResponse, String uri) throws IOException {
        Response response = new Response();

        ContentType contentType = ContentType.getOrDefault(httpResponse.getEntity());
        response.setCharset(contentType.getCharset());
        response.setMimeType(contentType.toString());

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        response.setReturnCode(statusCode);

        response.setStream(httpResponse.getEntity().getContent());

        response.setUri(uri);

        return response;
    }
}
