<%@include file="/WEB-INF/jsp/header.jsp" %>

<% String src = request.getContextPath() + "/img/remove.png"; %>

<div id="editForm">
    <div id="editHeader">
        <div class="left"><h2><fmt:message key="wip.config.title"/></h2></div>
        <div class="right">
            <form id="changePage" action="<portlet:actionURL/>" method="POST">
                <select name="editPage" onchange="javascript:this.form.submit();return(false);">
                    <option value="generalsettings"><fmt:message key="wip.config.generalsettings"/></option>
                    <option value="caching"><fmt:message key="wip.config.caching" /></option>
                    <option value="htmlrewriting"><fmt:message key="wip.config.htmlrewriting"/></option>
                    <option value="clipping"><fmt:message key="wip.config.easyclipping"/></option>
                    <option value="cssrewriting"><fmt:message key="wip.config.cssrewriting"/></option>
                    <option value="jsrewriting"><fmt:message key="wip.config.jsrewriting"/></option>
                    <option value="ltpaauth" selected="selected"><fmt:message key="wip.config.ltpaauth"/></option>
                </select>
            </form>
        </div>
    </div>
    <form method="POST" action="<portlet:actionURL/>" class="wip_form">
        <input type="hidden" name="form" value="7"/>
        <p class="line">
            <label for="ltpaSsoAuthentication"><fmt:message key="wip.config.ltpassoauthentication" /> :</label>
            <input type="checkbox" name="ltpaSsoAuthentication" id="ltpaSsoAuthentication" <% if (wipConf.getLtpaSsoAuthentication()) out.print("checked"); %> />
            <%= printHelp(rb.getString("wip.help.ltpassoauthentication")) %>
        </p>
        <p class="line">
            <label for="credentialProviderClassName"><fmt:message key="wip.config.credentialproviderclassname" /> :</label>
            <input type="text" name="credentialProviderClassName" id="credentialProviderClassName" value="<%= wipConf.getCredentialProviderClassName() %>" />
            <%= printHelp(rb.getString("wip.help.credentialproviderclassname")) %>
            <%= printError("credentialProviderClassName", errors) %>
        </p>              
        <p class="line">
            <label for="ltpaSecretProviderClassName"><fmt:message key="wip.config.ltpasecretproviderclassname" /> :</label>
            <input type="text" name="ltpaSecretProviderClassName" id="ltpaSecretProviderClassName" value="<%= wipConf.getLtpaSecretProviderClassName() %>" />
            <%= printHelp(rb.getString("wip.help.ltpasecretproviderclassname")) %>
            <%= printError("ltpaSecretProviderClassName", errors) %>
        </p>          
        <p class="submit">
            <input type="submit" value="<fmt:message key='wip.config.save' />" />
        </p>
    </form>
    <% session.removeAttribute("errors"); %>
</div>
