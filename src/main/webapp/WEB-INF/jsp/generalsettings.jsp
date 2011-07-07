<%@include file="/WEB-INF/jsp/header.jsp" %>

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
		<div class="left"><h2><fmt:message key="wip.config.title"/></h2></div>
		<div class="right">
		<form id="changePage" action="<portlet:actionURL/>" method="POST">
			<select name="editPage" onchange="javascript:this.form.submit();return(false);">
				<option value="generalsettings" selected="selected"><fmt:message key="wip.config.generalsettings"/></option>
				<option value="caching"><fmt:message key="wip.config.caching" /></option>
				<option value="htmlrewriting"><fmt:message key="wip.config.htmlrewriting"/></option>
				<option value="clipping"><fmt:message key="wip.config.easyclipping"/></option>
				<option value="cssrewriting"><fmt:message key="wip.config.cssrewriting"/></option>
				<option value="jsrewriting"><fmt:message key="wip.config.jsrewriting"/></option>
				<option value="ltpaauth"><fmt:message key="wip.config.ltpaauth"/></option>
			</select>
		</form>
		</div>
	</div>
	<form method="POST" action="<portlet:actionURL/>" class="wip_form" name="wipform">
		<input type="hidden" name="form" value="1"/>
        <p class="line">
            <label for="initUrl"><fmt:message key="wip.config.initurl" /> :</label>
            <input type="text" name="initUrl" id="initUrl" value="<%= wipConf.getInitUrl() %>" />
            <%= printHelp(rb.getString("wip.help.initurl")) %>
            <%= printError("initUrl", errors) %>
        </p>		
		<p class="line" id="domainstoproxydiv" <% if (!wipConf.getEnableUrlRewriting()) out.print("style=\"display:none;\""); %>>
			<label for="domainsToProxy"><fmt:message key="wip.config.domainstoproxy" /> :</label>
			<input type="text" name="domainsToProxy" id="domainsToProxy" value="<%= wipConf.getDomainsAsString(wipConf.getDomainsToProxy()) %>" />
			<%= printHelp(rb.getString("wip.help.domainstoproxy")) %>
			<%= printError("domainsToProxy", errors) %>
		</p>
	    <p class="line">
            <label for="enableUrlRewriting"><fmt:message key="wip.config.enableurlrewriting" /> :</label>
            <input type="checkbox" name="enableUrlRewriting" id="enableUrlRewriting" <% if (wipConf.getEnableUrlRewriting()) out.print("checked"); %> />
            <%= printHelp(rb.getString("wip.help.enableurlrewriting")) %>
        </p>
        <p class="line">
            <label for="portletTitle"><fmt:message key="wip.config.portlettitle" /> :</label>
            <input type="text" name="portletTitle" id="portletTitle" value="<%= wipConf.getPortletTitle() %>" />
            <%= printHelp(rb.getString("wip.help.portlettitle")) %>
        </p>    
		<p class="submit">
			<input type="submit" value="<fmt:message key='wip.config.save' />" />
		</p>
	</form>
	<% session.removeAttribute("errors"); %>
</div>
