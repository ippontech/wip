<%@include file="/WEB-INF/jsp/config/header.jsp" %>

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
                    <option value="HTML_REWRITING" selected="selected"><fmt:message
                            key="wip.config.htmlrewriting"/></option>
                    <option value="CLIPPING"><fmt:message key="wip.config.easyclipping"/></option>
                    <option value="CSS_REWRITING"><fmt:message key="wip.config.cssrewriting"/></option>
                    <option value="JS_REWRITING"><fmt:message key="wip.config.jsrewriting"/></option>
                    <option value="LTPA_AUTH"><fmt:message key="wip.config.ltpaauth"/></option>
                </select>
            </form>
        </div>
    </div>
    <form method="POST" action="<portlet:actionURL/>" name="wipform" class="wip_form">
        <input type="hidden" name="form" value="3"/>
        <p style="font-weight: bold"><fmt:message key="wip.config.xslttransform"/> : (<a href="#" onclick="reset();">reset</a>)</p>
        <p class="line">
            <textarea name="xsltTransform" id="xsltTransform"><%=wipConf.getXsltTransform()%></textarea>
             <%=printHelp("wip.help.xslttransform", locale)%>
            <br/>
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

<script type="text/javascript">
	//code editor integration: code mirror provide syntax coloration
	var myCodeMirror = CodeMirror.fromTextArea(document.getElementById('xsltTransform'));
	
    function reset() {
        window.document.getElementById('xsltTransform').value = '';
        window.document.wipform.submit();
    }
</script>

