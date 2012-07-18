<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>

<%-- JSTL from Sun --%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ page import="fr.ippon.wip.config.WIPConfiguration" %>
<%@ page import="fr.ippon.wip.config.dao.ConfigurationDAO" %>
<%@ page import="fr.ippon.wip.config.dao.ConfigurationDAOFactory" %>
<%@ page import="fr.ippon.wip.http.request.Request" %>
<%@ page import="fr.ippon.wip.portlet.Pages" %>
<%@ page import="fr.ippon.wip.util.WIPUtil" %>
<%@ page import="fr.ippon.wip.util.WIPLogging" %>
<%@ page import="fr.ippon.wip.portlet.Attributes" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="javax.portlet.PortletMode" %>
<%@ page import="javax.portlet.PortletResponse" %>
<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.logging.Logger" %>

<portlet:defineObjects/>
<%
	Locale locale = request.getLocale();
    request.setAttribute("localeCode", locale.getLanguage());
    RenderRequest pReq = (RenderRequest) request.getAttribute("javax.portlet.request");
    PortletResponse pRsp = (PortletResponse) request.getAttribute("javax.portlet.response");
    ConfigurationDAO wipConfigurationDAO = ConfigurationDAOFactory.getInstance().getXMLInstance();
    WIPConfiguration wipConf = WIPUtil.getConfiguration(pReq);
    Map<String, String> errors = (Map<String, String>) portletSession.getAttribute("errors");
    if (errors == null) errors = new HashMap<String, String>();
%>

<%!String printError(String key, Map<String, String> e) {
        String r = "";
        if (e != null && e.containsKey(key))
            r = "<span class=\"error\">" + e.get(key) + "</span>";
        return r;
    }

    String printHelp(String key, Locale locale) {
        String r = "";
        r += "<span class=\"wip_help\" onmouseover=\"showHelp(this)\" onmouseout=\"hideHelp(this)\" >";
        r += "<a>" + WIPUtil.getMessage("wip.help", locale) + "</a>";
        r += "<span style=\"display:none\">" + WIPUtil.getMessage(key, locale) + "</span>";
        r += "</span>";
        return r;
    }%>

<fmt:setLocale value="${localeCode}" scope="session"/>
<fmt:setBundle basename="content.Language"/>

<style>
    #wipconfig_error {
        background: url(<%= request.getContextPath() + "/img/alert.png" %>) no-repeat scroll 6px 50% #FFC;
        border: 1px solid #FC0;
        color: #B83A1B;
        display: block;
        font-weight: bold;
        margin: 2px auto 14px;
        padding: 6px 6px 6px 30px;
        text-align: left;
    }

    #editHeader {
        height: 55px;
    }

    #editHeader .left {
        float: left;
        text-align: left;
    }

    #editHeader h2 {
        margin-top: 0;
    }

    #editHeader .right {
        float: right;
        text-align: right;
    }

    .wip_form {
        padding: 10px 20px;
        width: 500px;
        margin: auto;
    }

    .wip_form p {
        margin: 1em 0;
    }

    .wip_form p.line {
        overflow: hidden;
        width: 100%;
    }

    .wip_form p.line label {
        text-align: right;
        width: 200px;
        float: left;
    }

    .wip_form p.line {
        margin-left: 12px;
        padding: 2px 4px;
    }

    .wip_form p.line input {
        margin-left: 12px;
        padding: 2px 4px;
        float: left;
    }

    .wip_form p.line input[type="text"] {
        width: 230px;
    }

    .wip_form label {
        cursor: pointer;
        font-weight: bold;
    }

    .wip_form p.submit {
        width: 100px;
        margin: auto;
    }

    .wip_form span.error {
        color: red;
    }

    .wip_form textarea {
        width: 80%;
        height: 200px
    }

    .wip_form table {
        width: 100%;
        margin-left: 12px;
        margin-bottom: 10px;
    }

    .wip_form table td {
        vertical-align: top;
    }

    .wip_form ul {
        list-style-type: none;
    }

    .wip_form li {
        height: 25px;
    }

    .wip_form span {
        margin-left: 22px;
    }

    .wip_form img {
        position: absolute;
        width: 12px;
        height: 12px;
        margin-top: 2px;
    }

    .wip_help {
        float: right;
        margin: 0px;
    }

    .wip_help a {
        cursor: pointer;
    }

    .wip_help span {
        position: absolute;
        min-width: 120px;
        min-height: 60px;
        background-color: #fff;
        border: 1px dashed #aaa;
        padding: 5px 10px 5px 10px;
        margin-left: 5px;
    }

    .wip td {
        padding-right: 5px;
        padding-left: 5px;
    }

    .config_menu {
        margin-bottom: 30px;
    }

    .config_menu a {
        margin-right: 15px;
    }

</style>

<script type="text/javascript">

    function showHelp(e) {
        e.childNodes[1].style.display = '';
    }

    function hideHelp(e) {
        e.childNodes[1].style.display = 'none';
    }

</script>

