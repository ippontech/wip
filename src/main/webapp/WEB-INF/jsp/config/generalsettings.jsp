<%@include file="/WEB-INF/jsp/config/header.jsp" %>

<script type="text/javascript">

    function checkUrlRewriting() {
        if (document.forms.wipform.enableUrlRewriting.checked) {
            document.getElementById('domainstoproxydiv').style.display = 'block';
        } else {
            document.getElementById('domainstoproxydiv').style.display = 'none';
        }
        return true;
    }

</script>

<div id="editForm">
    <div id="editHeader">
        <div class="left">
			<h5>Configuration: <%=wipConf.getName()%></h5>
		</div>
        <div class="right">
             <form id="changePage" action="<portlet:actionURL/>" method="POST">
               	<select name="<%=Attributes.PAGE.name()%>" onchange="this.form.submit();return(false);">
                    <option value="GENERAL_SETTINGS" selected="selected"><fmt:message key="wip.config.generalsettings"/></option>
                    <option value="CACHING"><fmt:message key="wip.config.caching"/></option>
                    <option value="HTML_REWRITING"><fmt:message key="wip.config.htmlrewriting"/></option>
                    <option value="CLIPPING"><fmt:message key="wip.config.easyclipping"/></option>
                    <option value="CSS_REWRITING"><fmt:message key="wip.config.cssrewriting"/></option>
                    <option value="JS_REWRITING"><fmt:message key="wip.config.jsrewriting"/></option>
                    <option value="LTPA_AUTH"><fmt:message key="wip.config.ltpaauth"/></option>
                </select>
            </form>
        </div>
    </div>
    <form method="POST" action="<portlet:actionURL/>" class="wip_form" name="wipform">
        <input type="hidden" name="form" value="1"/>

        <p class="line">
            <label for="initUrl"><fmt:message key="wip.config.initurl"/> :</label>
            <input type="text" name="initUrl" id="initUrl" value="<%=wipConf.getInitUrl()%>"/>
            <%=printHelp("wip.help.initurl", locale)%>
            <%=printError("initUrl", errors)%>
        </p>

        <p class="line" id="domainstoproxydiv" <%if (!wipConf.isEnableUrlRewriting())
            out.print("style=\"display:none;\"");%>>
            <label for="domainsToProxy"><fmt:message key="wip.config.domainstoproxy"/> :</label>
            <input type="text" name="domainsToProxy" id="domainsToProxy"
                   value="<%=StringUtils.join(wipConf.getDomainsToProxy(), ";")%>"/>
            <%=printHelp("wip.help.domainstoproxy", locale)%>
            <%=printError("domainsToProxy", errors)%>
        </p>

        <p class="line">
            <label for="enableUrlRewriting"><fmt:message key="wip.config.enableurlrewriting"/> :</label>
            <input type="checkbox" name="enableUrlRewriting"
                   id="enableUrlRewriting" <%if (wipConf.isEnableUrlRewriting()) out.print("checked");%> />
            <%=printHelp("wip.help.enableurlrewriting", locale)%>
        </p>

        <p class="line">
            <label for="portletTitle"><fmt:message key="wip.config.portlettitle"/> :</label>
            <input type="text" name="portletTitle" id="portletTitle" value="<%=wipConf.getPortletTitle()%>"/>
            <%=printHelp("wip.help.portlettitle", locale)%>
        </p>

        <p class="submit">
       		<%
       			if(!AbstractConfigurationDAO.DEFAULT_CONFIG_NAME.equals(wipConf.getName())) {
       		%>
	            <input type="submit" value="<fmt:message key='wip.config.save' />"/>
    	    <%} %>
        </p>
    </form>
    <% session.removeAttribute("errors"); %>
</div>
