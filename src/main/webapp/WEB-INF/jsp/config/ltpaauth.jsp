<%@include file="/WEB-INF/jsp/config/header.jsp" %>

<%
	String src = request.getContextPath() + "/img/remove.png";
%>

<div id="editForm">
    <div id="editHeader">
        <div class="left">
			<h5>Configuration: <%=wipConf.getName()%></h5>
		</div>
        <div class="right">
             <form id="changePage" action="<portlet:actionURL/>" method="POST">
                <select name="<%=Attributes.PAGE.name()%>" onchange="this.form.submit();return(false);">
                    <option value="GENERAL_SETTINGS"><fmt:message key="wip.config.generalsettings"/></option>
                    <option value="CACHING"><fmt:message key="wip.config.caching"/></option>
                    <option value="HTML_REWRITING"><fmt:message
                            key="wip.config.htmlrewriting"/></option>
                    <option value="CLIPPING"><fmt:message key="wip.config.easyclipping"/></option>
                    <option value="CSS_REWRITING"><fmt:message key="wip.config.cssrewriting"/></option>
                    <option value="JS_REWRITING"><fmt:message key="wip.config.jsrewriting"/></option>
                    <option value="LTPA_AUTH" selected="selected"><fmt:message key="wip.config.ltpaauth"/></option>
                </select>
            </form>
        </div>
    </div>
    <form method="POST" action="<portlet:actionURL/>" class="wip_form">
        <input type="hidden" name="form" value="7"/>

        <p class="line">
            <label for="ltpaSsoAuthentication"><fmt:message key="wip.config.ltpassoauthentication"/> :</label>
            <input type="checkbox" name="ltpaSsoAuthentication"
                   id="ltpaSsoAuthentication" <%if (wipConf.isLtpaSsoAuthentication()) out.print("checked");%> />
            <%=printHelp("wip.help.ltpassoauthentication", locale)%>
        </p>

        <p class="line">
            <label for="credentialProviderClassName"><fmt:message key="wip.config.credentialproviderclassname"/>
                :</label>
            <input type="text" name="credentialProviderClassName" id="credentialProviderClassName"
                   value="<%=wipConf.getCredentialProviderClassName()%>"/>
            <%=printHelp("wip.help.credentialproviderclassname", locale)%>
            <%=printError("credentialProviderClassName", errors)%>
        </p>

        <p class="line">
            <label for="ltpaSecretProviderClassName"><fmt:message key="wip.config.ltpasecretproviderclassname"/>
                :</label>
            <input type="text" name="ltpaSecretProviderClassName" id="ltpaSecretProviderClassName"
                   value="<%=wipConf.getLtpaSecretProviderClassName()%>"/>
            <%=printHelp("wip.help.ltpasecretproviderclassname", locale)%>
            <%=printError("ltpaSecretProviderClassName", errors)%>
        </p>

		<%
			if(!AbstractConfigurationDAO.DEFAULT_CONFIG_NAME.equals(wipConf.getName())) {
		%>
        <p class="submit">
            <input type="submit" value="<fmt:message key='wip.config.save' />"/>
        </p>
        <%} %>
    </form>
    <% session.removeAttribute("errors"); %>
</div>
