/*
 *	Copyright 2010,2011 Ippon Technologies 
 *  
 *	This file is part of Web Integration Portlet (WIP).
 *	Web Integration Portlet (WIP) is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Lesser General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Web Integration Portlet (WIP) is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Lesser General Public License for more details.
 *
 *	You should have received a copy of the GNU Lesser General Public License
 *	along with Web Integration Portlet (WIP).  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.ippon.wip.portlet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import fr.ippon.wip.config.WIPConfiguration;
import fr.ippon.wip.config.ZipConfiguration;
import fr.ippon.wip.config.dao.AbstractConfigurationDAO;
import fr.ippon.wip.config.dao.ConfigurationDAOFactory;
import fr.ippon.wip.util.WIPUtil;

public class WIPConfigurationPortlet extends GenericPortlet {

	private static final Logger LOG = Logger.getLogger(WIPConfigurationPortlet.class.getName());
	
	private AbstractConfigurationDAO configurationDAO;
    private PortletFileUpload fileUploadPortlet;
    private String deployPath;

	/**
	 * Set if necessary the default configuration and page display in the
	 * session.
	 * 
	 * @param request
	 */
	private void checkOrSetSession(PortletRequest request) {
		PortletSession session = request.getPortletSession();

		// check and set if necessary the selected configuration in session
		String configName = (String) session.getAttribute(Attributes.CONFIGURATION_NAME.name());
		if (StringUtils.isEmpty(configName))
			session.setAttribute(Attributes.CONFIGURATION_NAME.name(), AbstractConfigurationDAO.DEFAULT_CONFIG_NAME);

		// check and set if necessary the configuration page in session
		Pages page = (Pages) session.getAttribute(Attributes.PAGE.name());
		if (page == null)
			session.setAttribute(Attributes.PAGE.name(), Pages.GENERAL_SETTINGS);
	}
	
	/**
	 * This class will try to build an URL from a string and store an error if
	 * the URL is malformed or empty
	 * 
	 * @param varName
	 *            The name of the variable, used to map errors correctly
	 * @param urlAsString
	 *            The String to convert into a list
	 * @param errors
	 *            The map containing the error messages
	 * @param rb
	 *            The resource bundle used to set the error message
	 * @return The URL built from the given String
	 */
	private void checkURL(String varName, String urlAsString, Map<String, String> errors, ResourceBundle rb) {
		if (StringUtils.isEmpty(urlAsString)) {
			errors.put(varName, rb.getString("wip.errors." + varName + ".empty"));
			return;
		}

		try {
			new URL(urlAsString);
		} catch (MalformedURLException e1) {
			errors.put(varName, rb.getString("wip.errors." + varName + ".malformed"));
		}
	}

	/**
	 * This class will try to build a list of URLs from a string and store an
	 * error if an URL is malformed
	 * 
	 * @param varName
	 *            The name of the variable, used to map errors correctly
	 * @param urlListAsString
	 *            The String to convert into a list
	 * @param errors
	 *            The map containing the error messages
	 * @param rb
	 *            The resource bundle used to set the error message
	 * @return A list of the URLs contained in the given String
	 */
	private void checkURLs(String varName, String urlListAsString, Map<String, String> errors, ResourceBundle rb, WIPConfiguration wipConfig) {
		for (String urlAsString : urlListAsString.split(";"))
			checkURL(varName, urlAsString, errors, rb);
	}

	/**
	 * Display the configuration portlet.
	 */
	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		checkOrSetSession(request);

		Pages page = (Pages) request.getPortletSession().getAttribute(Attributes.PAGE.name());
		PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher(page.getPath());
		portletRequestDispatcher.include(request, response);
	}

	private void handleCaching(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPUtil.getConfiguration(request);
		Map<String, String> errors = new HashMap<String, String>();

		// Getting the parameters from the request
		String tmpEnableCache = request.getParameter("enableCache");
		boolean enableCache = true;
		if (tmpEnableCache == null)
			enableCache = false;

		wipConfig.setEnableCache(enableCache);

		if (enableCache) {
			String tmpPageCachePrivate = request.getParameter("pageCachePrivate");
			boolean pageCachePrivate = true;
			if (tmpPageCachePrivate == null)
				pageCachePrivate = false;

			String tmpForcePageCaching = request.getParameter("forcePageCaching");
			boolean forcePageCaching = true;
			if (tmpForcePageCaching == null)
				forcePageCaching = false;

			String tmpForceResourceCaching = request.getParameter("forceResourceCaching");
			boolean forceResourceCaching = true;
			if (tmpForceResourceCaching == null)
				forceResourceCaching = false;

			wipConfig.setPageCachePrivate(pageCachePrivate);
			wipConfig.setForcePageCaching(forcePageCaching);
			wipConfig.setForceResourceCaching(forceResourceCaching);
		}
		
		configurationDAO.update(wipConfig);

		// Sending errors to the portlet session
		request.getPortletSession().setAttribute("errors", errors, PortletSession.APPLICATION_SCOPE);

		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "caching");
	}

	/**
	 * Handle clipping configuration: get settings in request parameters from
	 * the configuration form. Save them in the portlet configuartion.
	 * 
	 * @param request
	 *            The ActionRequest sent to WIPortlet in edit mode
	 * @param response
	 *            The ActionResponse sent to WIPortlet in edit mode
	 */
	private void handleClipping(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPUtil.getConfiguration(request);
		ResourceBundle rb = ResourceBundle.getBundle("content.Language", request.getLocale());
		Map<String, String> errors = new HashMap<String, String>();

		// Getting the parameters from the request
		String clippingType = request.getParameter("clippingType");

		if (clippingType.equals("xpath")) {
			String xPath = request.getParameter("xPath");
			if (xPath.equals("")) {
				errors.put("xPath", rb.getString("wip.errors.xpath.empty"));
			} else {
				wipConfig.setXPath(xPath);
				wipConfig.setClippingType(clippingType);
				configurationDAO.update(wipConfig);
			}
		} else if (clippingType.equals("xslt")) {
			String xsltClipping = request.getParameter("xsltClipping");
			if(StringUtils.isEmpty(xsltClipping))
				xsltClipping = configurationDAO.getDefaultConfiguration().getXsltClipping();
			
			wipConfig.setXsltClipping(xsltClipping);
			wipConfig.setClippingType(clippingType);
			configurationDAO.update(wipConfig);

		} else {
			wipConfig.setClippingType(clippingType);
			configurationDAO.update(wipConfig);
		}

		// Sending errors to the portlet session
		request.getPortletSession().setAttribute("errors", errors, PortletSession.APPLICATION_SCOPE);

		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "clipping");
	}

	/**
	 * Handle CSS rewriting configuration: get settings in request parameters
	 * from the configuration form. Save them in the portlet configuartion.
	 * 
	 * @param request
	 *            The ActionRequest sent to WIPortlet in edit mode
	 * @param response
	 *            The ActionResponse sent to WIPortlet in edit mode
	 */
	private void handleCSSRewriting(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPUtil.getConfiguration(request);
		Map<String, String> errors = new HashMap<String, String>();

		// Getting the parameters from the request
		String customCss = request.getParameter("customCss");
		String cssRegex = request.getParameter("cssRegex");
		String portletDivId = request.getParameter("portletDivId");

		String tmpEnableCssRetrieving = request.getParameter("enableCssRetrieving");
		boolean enableCssRetrieving = true;
		if (tmpEnableCssRetrieving == null)
			enableCssRetrieving = false;

		String tmpAbsolutePositioning = request.getParameter("absolutePositioning");
		boolean absolutePositioning = true;
		if (tmpAbsolutePositioning == null)
			absolutePositioning = false;

		String tmpAddPrefix = request.getParameter("addPrefix");
		boolean addPrefix = true;
		if (tmpAddPrefix == null)
			addPrefix = false;

		String tmpEnableCssRewriting = request.getParameter("enableCssRewriting");
		boolean enableCssRewriting = true;
		if (tmpEnableCssRewriting == null)
			enableCssRewriting = false;

		// Saving the new configuration
		wipConfig.setCssRegex(cssRegex);
		wipConfig.setAbsolutePositioning(absolutePositioning);
		wipConfig.setAddPrefix(addPrefix);
		wipConfig.setPortletDivId(portletDivId);
		wipConfig.setEnableCssRetrieving(enableCssRetrieving);
		wipConfig.setEnableCssRewriting(enableCssRewriting);
		wipConfig.setCustomCss(customCss);
		configurationDAO.update(wipConfig);

		// Sending errors to the portlet session
		request.getPortletSession().setAttribute("errors", errors, PortletSession.APPLICATION_SCOPE);

		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "cssrewriting");
	}

	/**
	 * Handle general settings: get settings in request parameters from the
	 * configuration form. Save them in the portlet configuration.
	 * 
	 * @param request
	 *            The ActionRequest sent to WIPortlet in edit mode
	 * @param response
	 *            The ActionResponse sent to WIPortlet in edit mode
	 */
	private void handleGeneralSettings(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPUtil.getConfiguration(request);
		ResourceBundle rb = ResourceBundle.getBundle("content.Language", request.getLocale());
		Map<String, String> errors = new HashMap<String, String>();

		// Getting the parameters from the request
		String initUrl = request.getParameter("initUrl");
		checkURL("initUrl", initUrl, errors, rb);

		String domainsToProxy = request.getParameter("domainsToProxy");
		checkURLs("domainsToProxy", domainsToProxy, errors, rb, wipConfig);

		String tmpEnableUrlRewriting = request.getParameter("enableUrlRewriting");
		boolean enableUrlRewriting = true;
		if (tmpEnableUrlRewriting == null)
			enableUrlRewriting = false;

		String portletTitle = request.getParameter("portletTitle");

		// Saving the new configuration
		if (initUrl != null)
			wipConfig.setInitUrl(initUrl);
		if (domainsToProxy != null) {
			List<String> list = Arrays.asList(domainsToProxy.split(";"));
			wipConfig.setDomainsToProxy(list);
		}
		if (portletTitle != null)
			wipConfig.setPortletTitle(portletTitle);

		wipConfig.setEnableUrlRewriting(enableUrlRewriting);
		configurationDAO.update(wipConfig);

		// Sending errors to the portlet session
		request.getPortletSession().setAttribute("errors", errors, PortletSession.APPLICATION_SCOPE);

		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "generalsettings");
	}

	/**
	 * Handle HTML rewriting configuration: get settings in request parameters
	 * from the configuration form. Save them in the portlet configuartion.
	 * 
	 * @param request
	 *            The ActionRequest sent to WIPortlet in edit mode
	 * @param response
	 *            The ActionResponse sent to WIPortlet in edit mode
	 */
	private void handleHtmlRewriting(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPUtil.getConfiguration(request);

		// Getting the parameters from the request
		String xsltTransform = request.getParameter("xsltTransform");

		// Saving the new configuration
		wipConfig.setXsltTransform(xsltTransform);
		configurationDAO.update(wipConfig);

		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "htmlrewriting");
	}

	/**
	 * Handle JS rewriting configuration: get settings in request parameters
	 * from the configuration form. Save them in the portlet configuartion.
	 * 
	 * @param request
	 *            The ActionRequest shttp://www.developpez.com/ent to WIPortlet
	 *            in edit mode
	 * @param response
	 *            The ActionResponse sent to WIPortlet in edit mode
	 */
	private void handleJSRewriting(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPUtil.getConfiguration(request);
		Map<String, String> errors = new HashMap<String, String>();

		// Getting the parameters from the request
		String jsRegex = request.getParameter("jsRegex");

		String tmpJavascriptUrls = request.getParameter("javascriptUrls");
		String[] l1 = tmpJavascriptUrls.split(";");
		List<String> javascriptUrls = Arrays.asList(l1);

		String tmpScriptsToIgnore = request.getParameter("scriptIgnoredUrls");
		String[] l3 = tmpScriptsToIgnore.split(";");
		List<String> scriptsToIgnore = Arrays.asList(l3);

		String tmpScriptsToDelete = request.getParameter("scriptDeletedUrls");
		String[] l4 = tmpScriptsToDelete.split(";");
		List<String> scriptsToDelete = Arrays.asList(l4);

		// Saving the new configuration
		wipConfig.setJsRegex(jsRegex);
		wipConfig.setJavascriptUrls(javascriptUrls);
		wipConfig.setScriptsToIgnore(scriptsToIgnore);
		wipConfig.setScriptsToDelete(scriptsToDelete);
		configurationDAO.update(wipConfig);

		// Sending errors to the portlet session
		request.getPortletSession().setAttribute("errors", errors, PortletSession.APPLICATION_SCOPE);

		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "jsrewriting");
	}

	private void handleLTPAAuthentication(ActionRequest request, ActionResponse response) {
		// Getting WIPConfig, resource bundle and a map to store errors
		WIPConfiguration wipConfig = WIPUtil.getConfiguration(request);
		Map<String, String> errors = new HashMap<String, String>();

		// Getting the parameters from the request
		String tmpLtpaSsoAuthentication = request.getParameter("ltpaSsoAuthentication");
		boolean ltpaSsoAuthentication = true;
		if (tmpLtpaSsoAuthentication == null)
			ltpaSsoAuthentication = false;
		String ltpaSecretProviderClassName = request.getParameter("ltpaSecretProviderClassName");
		String credentialProviderClassName = request.getParameter("credentialProviderClassName");

		// Saving the new configuration
		wipConfig.setLtpaSsoAuthentication(ltpaSsoAuthentication);
		wipConfig.setLtpaSecretProviderClassName(ltpaSecretProviderClassName);
		wipConfig.setCredentialProviderClassName(credentialProviderClassName);
		configurationDAO.update(wipConfig);

		// Sending errors to the portlet session
		request.getPortletSession().setAttribute("errors", errors, PortletSession.APPLICATION_SCOPE);

		// Sending the page to display to the portlet session
		request.getPortletSession().setAttribute("editPage", "ltpaauth");
	}

	@Override
	public void init(PortletConfig config) throws PortletException {
		super.init(config);

        String confDirPath = config.getInitParameter("CONF_DIRECTORY_LOCATION");
        String deployDirPath = config.getInitParameter("DEPLOY_DIRECTORY_LOCATION");

        ConfigurationDAOFactory.INSTANCE.setPortletContext(config.getPortletContext());
        ConfigurationDAOFactory.INSTANCE.setConfDirectoryLocation(confDirPath);
        ConfigurationDAOFactory.INSTANCE.setDeployDirectoryLocation(deployDirPath);
        configurationDAO = ConfigurationDAOFactory.INSTANCE.getXMLDAOInstance();

	    File deployFile = new File(ConfigurationDAOFactory.INSTANCE.getDeployDirectoryLocation());
        deployPath = deployFile.getAbsolutePath();
        DiskFileItemFactory factory = new DiskFileItemFactory(0, deployFile);
		fileUploadPortlet = new PortletFileUpload(factory);
	}

    @Override
    public void destroy() {
        super.destroy();

        deployPath = null;
        configurationDAO = null;
        fileUploadPortlet = null;
    }

    /**
	 * Process the user action: a configuration can be deleted, selected or
	 * saved.
	 */
	@Override
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
		PortletSession session = request.getPortletSession();
		
		if(fileUploadPortlet.isMultipartContent(request)) {
			try {
				List<DiskFileItem> fileItems = fileUploadPortlet.parseRequest(request);
				for(DiskFileItem fileItem : fileItems) {
                    // TODO : ???
                    String newName = FilenameUtils.concat(deployPath, fileItem.getName());
					File file = fileItem.getStoreLocation();
					file.renameTo(new File(newName));
				}
				
				request.setAttribute(Attributes.PAGE.name(), Pages.EXISTING_CONFIG.name());
				
			} catch (FileUploadException e) {
				e.printStackTrace();
			}
			
			return;
		}
		
		String page = request.getParameter(Attributes.PAGE.name());
		if (!StringUtils.isEmpty(page)) {
			session.setAttribute(Attributes.PAGE.name(), Pages.valueOf(page));
			return;
		}

		String configName = request.getParameter(Attributes.ACTION_SELECT_CONFIGURATION.name());
		if (!StringUtils.isEmpty(configName) && configurationDAO.exists(configName)) {
			session.setAttribute(Attributes.CONFIGURATION_NAME.name(), configName);
			session.setAttribute(Attributes.PAGE.name(), Pages.GENERAL_SETTINGS);
			return;
		}

		configName = request.getParameter(Attributes.ACTION_DELETE_CONFIGURATION.name());
		if (!StringUtils.isEmpty(configName)) {
			if(WIPUtil.getConfiguration(request).getName().equals(configName))
				session.setAttribute(Attributes.CONFIGURATION_NAME.name(), AbstractConfigurationDAO.DEFAULT_CONFIG_NAME);
			
			configurationDAO.delete(configurationDAO.read(configName));
			return;
		}

		configName = request.getParameter(Attributes.ACTION_SAVE_CONFIGURATION_AS.name());
		if (!StringUtils.isEmpty(configName)) {
			WIPConfiguration configuration = WIPUtil.getConfiguration(request);
			WIPConfiguration newConfiguration = (WIPConfiguration) configuration.clone();
			newConfiguration.setName(configName);
			newConfiguration = configurationDAO.create(newConfiguration);
			session.setAttribute(Attributes.CONFIGURATION_NAME.name(), newConfiguration.getName());
			session.setAttribute(Attributes.PAGE.name(), Pages.GENERAL_SETTINGS);
			return;
		}

		if (request.getParameter("form") != null) {
			switch (Integer.valueOf(request.getParameter("form"))) {
			case 1:
				handleGeneralSettings(request, response);
				break;
			case 2:
				handleClipping(request, response);
				break;
			case 3:
				handleHtmlRewriting(request, response);
				break;
			case 4:
				handleCSSRewriting(request, response);
				break;
			case 5:
				handleJSRewriting(request, response);
				break;
			case 6:
				handleCaching(request, response);
				break;
			case 7:
				handleLTPAAuthentication(request, response);
				break;
			}
		}
	}

	@Override
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {
		String configName = request.getParameter(Attributes.ACTION_EXPORT_CONFIGURATION.name());
		if (StringUtils.isEmpty(configName))
			return;
		
		response.setContentType("multipart/x-zip");
		response.setProperty("Content-Disposition", "attachment; filename=" + configName + ".zip");
		
		WIPConfiguration configuration = configurationDAO.read(configName);
		ZipConfiguration zip = new ZipConfiguration();
		ZipOutputStream out = new ZipOutputStream(response.getPortletOutputStream());
		zip.zip(configuration, out);
		out.flush();
		out.close();
	}
}
