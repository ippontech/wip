<%@include file="/WEB-INF/jsp/header.jsp" %>

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
		<div class="left"><h2><fmt:message key="wip.config.title"/></h2></div>
		<div class="right">
		<form id="changePage" action="<portlet:actionURL/>" method="POST">
			<select name="editPage" onchange="javascript:this.form.submit();return(false);">
				<option value="generalsettings"><fmt:message key="wip.config.generalsettings"/></option>
				<option value="caching"><fmt:message key="wip.config.caching" /></option>
				<option value="htmlrewriting"><fmt:message key="wip.config.htmlrewriting"/></option>
				<option value="clipping"><fmt:message key="wip.config.easyclipping"/></option>
				<option value="cssrewriting" selected="selected"><fmt:message key="wip.config.cssrewriting"/></option>
				<option value="jsrewriting"><fmt:message key="wip.config.jsrewriting"/></option>
			</select>
		</form>
		</div>
	</div>
	<form method="POST" action="<portlet:actionURL/>" class="wip_form" name="wipform">
		<input type="hidden" name="form" value="4"/>
		<p class="line" id="enablecssretrieving">
			<label for="enableCssRetrieving"><fmt:message key="wip.config.enablecssretrieving" /> :</label>
			<input type="checkbox" name="enableCssRetrieving" id="enableCssRetrieving" onclick="javascript:check();" <% if (wipConf.getEnableCssRetrieving()) out.print("checked"); %> />
			<%= printHelp(rb.getString("wip.help.enablecssretrieving")) %>
		</p>
		<p class="line" id="enablecssrewriting" <% if (!wipConf.getEnableCssRetrieving()) out.print("style=\"display:none;\""); %>>
			<label for="enableCssRewriting"><fmt:message key="wip.config.enablecssrewriting" /> :</label>
			<input type="checkbox" name="enableCssRewriting" id="enableCssRewriting" onclick="javascript:check();" <% if (wipConf.getEnableCssRewriting()) out.print("checked"); %> />
			<%= printHelp(rb.getString("wip.help.enablecssrewriting")) %>
		</p>
		<p class="line" id="cssregex" <% if (!wipConf.getEnableCssRetrieving()||!wipConf.getEnableCssRewriting()) out.print("style=\"display:none;\""); %>>
			<label for="cssRegex"><fmt:message key="wip.config.cssregex" /> :</label>
			<input type="text" name="cssRegex" id="cssRegex" value="<%= StringEscapeUtils.escapeHtml(wipConf.getCssRegex()) %>" />
			<%= printHelp(rb.getString("wip.help.cssregex")) %>
		</p>
		<p class="line" id="absolutepositioning" <% if (!wipConf.getEnableCssRetrieving()||!wipConf.getEnableCssRewriting()) out.print("style=\"display:none;\""); %>>
			<label for="absolutePositioning"><fmt:message key="wip.config.absolutepositioning" /> :</label>
			<input type="checkbox" name="absolutePositioning" id="absolutepositioning" onclick="javascript:check();" <% if (wipConf.getAbsolutePositioning()) out.print("checked"); %> />
			<%= printHelp(rb.getString("wip.help.absolutepositioning")) %>
		</p>
		<p class="line" id="addprefix" <% if (!wipConf.getEnableCssRetrieving()||!wipConf.getEnableCssRewriting()) out.print("style=\"display:none;\""); %>>
			<label for="addPrefix"><fmt:message key="wip.config.addprefix" /> :</label>
			<input type="checkbox" name="addPrefix" id="addprefix" onclick="javascript:check();" <% if (wipConf.getAddPrefix()) out.print("checked"); %> />
			<%= printHelp(rb.getString("wip.help.addprefix")) %>
		</p>
		<p class="line" id="portletdivid" <% if (!wipConf.getEnableCssRetrieving()||!wipConf.getEnableCssRewriting()||!wipConf.getAddPrefix()) out.print("style=\"display:none;\""); %>>
			<label for="portletDivId"><fmt:message key="wip.config.portletdivid" /> :</label>
			<input type="text" name="portletDivId" id="portletDivId" value="<%= wipConf.getPortletDivId() %>" />
			<%= printHelp(rb.getString("wip.help.portletdivid")) %>
		</p>
		<p class="line">
			<label for="customCss"><fmt:message key="wip.config.customcss" /> :</label>
			<textarea name="customCss" id="customCss"><%= wipConf.getCustomCss() %></textarea>
			<%= printHelp(rb.getString("wip.help.customcss")) %>
			<%= printError("customCss", errors) %>
		</p>
		<p class="submit">
			<input type="submit" value="<fmt:message key='wip.config.save' />" />
		</p>
	</form>
	<% session.removeAttribute("errors"); %>
</div>