<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>

<%-- JSTL from Sun --%>
<%@ taglib uri="/WEB-INF/tld/c.tld"	prefix="c" %>
<%@ taglib uri="/WEB-INF/tld/fmt.tld" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="javax.portlet.PortletConfig" %>
<%@ page import="javax.portlet.PortletSession" %>
<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.PortletResponse" %>
<%@ page import="fr.ippon.wip.config.WIPConfiguration" %>
<%@ page import="fr.ippon.wip.config.WIPConfigurationManager" %>
<%@ page import="fr.ippon.wip.transformers.URLTypes" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map.Entry" %>

<portlet:defineObjects/>

<% 
	Locale locale = request.getLocale();
	RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
	ResourceBundle rb = ResourceBundle.getBundle("content.Language", locale);
	PortletResponse pRsp = (PortletResponse) request.getAttribute("javax.portlet.response");
	WIPConfiguration wipConf = WIPConfigurationManager.getInstance().getConfiguration(pRsp.getNamespace());
	Map<String, String> errors = (Map<String, String>) session.getAttribute("errors");
	if (errors == null) errors = new HashMap<String, String>();
%>

<%!
	String printError(String key, Map<String, String> e) {
		String r = "";
		if (e != null && e.containsKey(key)) 
			r = "<span class=\"error\">" + e.get(key) + "</span>";
		return r;
	}

	String printHelp(String mess) {
		String r = "";
		r += "<span class=\"wip_help\" onmouseover=\"showHelp(this)\" onmouseout=\"hideHelp(this)\" >";
		r += "<a>help</a>";
		r += "<span style=\"display:none\">"+mess+"</span>";
		r += "</span>";
		return r;
	}
%>

<fmt:setBundle basename="content.Language"/>

<div class="config_menu">
	<a href="<portlet:actionURL></portlet:actionURL>" title="Current config">
		<fmt:message key="wip.config.current" />
	</a>
	<a href="<portlet:actionURL><portlet:param name="configPage" value="all"/></portlet:actionURL>" title="Saved config">
		<fmt:message key="wip.config.existing" />
	</a>
	<a href="<portlet:actionURL><portlet:param name="saveConfig" value=""/></portlet:actionURL>">
		<fmt:message key="wip.config.savecurrent" /> 
	</a>
	<a href="<portlet:actionURL><portlet:param name="back" value=""/></portlet:actionURL>" title="Back">
	   <fmt:message key="wip.config.back"/>
	</a>
</div>

<style>

	#editHeader {
		height:55px;
	}
	#editHeader .left {
		float:left;
		text-align:left;
	}
	#editHeader h2 {
		margin-top:0;
	}
	#editHeader .right {
		float:right;
		text-align:right;
	}
	.wip_form {
		padding:10px 20px;
		width:500px;
		margin:auto;
	}
	.wip_form p {
		margin:1em 0;
	}
	.wip_form p.line {
		overflow:hidden;
		width:100%;
	}
	.wip_form p.line label {
		text-align:right;
		width:200px;
		float:left;
	}
	.wip_form p.line {
		margin-left:12px;
		padding:2px 4px;
	}
	.wip_form p.line input {
		margin-left:12px;
		padding:2px 4px;
		float: left;
	}
	.wip_form p.line input[type="text"] {
		width:230px;
	}
	.wip_form label {
		cursor:pointer;
		font-weight:bold;
	}
	.wip_form p.submit {
		width:100px;
		margin:auto;
	}
	.wip_form span.error {
		color:red;
	}
	.wip_form textarea {
		width:80%;
		height:200px 
	}
	.wip_form table {
		width:100%;
		margin-left:12px;
		margin-bottom: 10px;
	}
	.wip_form table td {
		vertical-align: top;
	}
	.wip_form ul {
		list-style-type:none;
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
		padding-right:5px;
		padding-left:5px;
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
		e.childNodes[1].style.display='';
	}

	function hideHelp(e) {
		e.childNodes[1].style.display='none';
	}

</script>

