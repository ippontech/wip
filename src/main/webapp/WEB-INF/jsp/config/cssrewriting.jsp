<%@include file="/WEB-INF/jsp/config/header.jsp" %>

<script type="text/javascript">

    function check() {
        if (!document.forms.wipform.enableCssRetrieving.checked) {
            document.forms.wipform.enableCssRewriting.checked = false;
            document.getElementById('enablecssrewriting').style.display = 'none';
            document.getElementById('cssregex').style.display = 'none';
            document.getElementById('addprefix').style.display = 'none';
            document.getElementById('portletdivid').style.display = 'none';
            document.getElementById('absolutepositioning').style.display = 'none';
        } else {
            if (document.forms.wipform.enableCssRewriting.checked) {
                document.getElementById('absolutepositioning').style.display = 'block';
                document.getElementById('enablecssrewriting').style.display = 'block';
                document.getElementById('cssregex').style.display = 'block';
                document.getElementById('addprefix').style.display = 'block';
                if (document.forms.wipform.addPrefix.checked)
                    document.getElementById('portletdivid').style.display = 'block';
                else
                    document.getElementById('portletdivid').style.display = 'none';
            } else {
                document.getElementById('absolutepositioning').style.display = 'none';
                document.getElementById('enablecssrewriting').style.display = 'block';
                document.getElementById('cssregex').style.display = 'none';
                document.getElementById('addprefix').style.display = 'none';
                document.getElementById('portletdivid').style.display = 'none';
            }
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
                    <option value="GENERAL_SETTINGS"><fmt:message key="wip.config.generalsettings"/></option>
                    <option value="CACHING"><fmt:message key="wip.config.caching"/></option>
                    <option value="HTML_REWRITING"><fmt:message
                            key="wip.config.htmlrewriting"/></option>
                    <option value="CLIPPING"><fmt:message key="wip.config.easyclipping"/></option>
                    <option value="CSS_REWRITING" selected="selected"><fmt:message key="wip.config.cssrewriting"/></option>
                    <option value="JS_REWRITING"><fmt:message key="wip.config.jsrewriting"/></option>
                    <option value="LTPA_AUTH"><fmt:message key="wip.config.ltpaauth"/></option>
                </select>
            </form>
        </div>
    </div>
    <form method="POST" action="<portlet:actionURL/>" class="wip_form" name="wipform">
        <input type="hidden" name="form" value="4"/>

        <p class="line" id="enablecssretrieving">
            <label for="enableCssRetrieving"><fmt:message key="wip.config.enablecssretrieving"/> :</label>
            <input type="checkbox" name="enableCssRetrieving" id="enableCssRetrieving"
                   onclick="check();" <%if (wipConf.isEnableCssRetrieving()) out.print("checked");%> />
            <%=printHelp("wip.help.enablecssretrieving", locale)%>
        </p>

        <p class="line" id="enablecssrewriting" <%if (!wipConf.isEnableCssRetrieving())
            out.print("style=\"display:none;\"");%>>
            <label for="enableCssRewriting"><fmt:message key="wip.config.enablecssrewriting"/> :</label>
            <input type="checkbox" name="enableCssRewriting" id="enableCssRewriting"
                   onclick="check();" <%if (wipConf.isEnableCssRewriting()) out.print("checked");%> />
            <%=printHelp("wip.help.enablecssrewriting", locale)%>
        </p>

        <p class="line" id="cssregex" <%if (!wipConf.isEnableCssRetrieving() || !wipConf.isEnableCssRewriting())
            out.print("style=\"display:none;\"");%>>
            <label for="cssRegex"><fmt:message key="wip.config.cssregex"/> :</label>
            <input type="text" name="cssRegex" id="cssRegex"
                   value="<%=StringEscapeUtils.escapeHtml(wipConf.getCssRegex())%>"/>
            <%=printHelp("wip.help.cssregex", locale)%>
        </p>

        <p class="line"
           id="absolutepositioning" <%if (!wipConf.isEnableCssRetrieving() || !wipConf.isEnableCssRewriting())
            out.print("style=\"display:none;\"");%>>
            <label for="absolutePositioning"><fmt:message key="wip.config.absolutepositioning"/> :</label>
            <input type="checkbox" name="absolutePositioning" id="absolutepositioning"
                   onclick="check();" <%if (wipConf.isAbsolutePositioning()) out.print("checked");%> />
            <%=printHelp("wip.help.absolutepositioning", locale)%>
        </p>

        <p class="line" id="addprefix" <%if (!wipConf.isEnableCssRetrieving() || !wipConf.isEnableCssRewriting())
            out.print("style=\"display:none;\"");%>>
            <label for="addPrefix"><fmt:message key="wip.config.addprefix"/> :</label>
            <input type="checkbox" name="addPrefix" id="addprefix" onclick="check();" <%if (wipConf.isAddPrefix())
                out.print("checked");%> />
            <%=printHelp("wip.help.addprefix", locale)%>
        </p>

        <p class="line"
           id="portletdivid" <%if (!wipConf.isEnableCssRetrieving() || !wipConf.isEnableCssRewriting() || !wipConf.isAddPrefix())
            out.print("style=\"display:none;\"");%>>
            <label for="portletDivId"><fmt:message key="wip.config.portletdivid"/> :</label>
            <input type="text" name="portletDivId" id="portletDivId" value="<%=wipConf.getPortletDivId()%>"/>
            <%=printHelp("wip.help.portletdivid", locale)%>
        </p>

        <p class="line">
            <label for="customCss"><fmt:message key="wip.config.customcss"/> :</label>
            <textarea name="customCss" id="customCss"><%=wipConf.getCustomCss()%>
            </textarea>
            <%=printHelp("wip.help.customcss", locale)%>
            <%=printError("customCss", errors)%>
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