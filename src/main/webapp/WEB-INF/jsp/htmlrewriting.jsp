<%@include file="/WEB-INF/jsp/header.jsp" %>

<script type="text/javascript">
	function reset() {
		window.document.getElementById('xsltTransform').value = '';
		window.document.wipform.submit();
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
				<option value="htmlrewriting" selected="selected"><fmt:message key="wip.config.htmlrewriting"/></option>
				<option value="clipping"><fmt:message key="wip.config.easyclipping"/></option>
				<option value="cssrewriting"><fmt:message key="wip.config.cssrewriting"/></option>
				<option value="jsrewriting"><fmt:message key="wip.config.jsrewriting"/></option>
			</select>
		</form>
		</div>
	</div>
	<form method="POST" action="<portlet:actionURL/>" name="wipform" class="wip_form">
		<input type="hidden" name="form" value="3"/>
		<p class="line">
			<label for="xsltTransform"><fmt:message key="wip.config.xslttransform" /> : (<a href="#" onclick="javascript:reset();">reset</a>)</label>
			<textarea name="xsltTransform" id="xsltTransform"><%= wipConf.getXsltTransform() %></textarea>
			<%= printHelp(rb.getString("wip.help.xslttransform")) %>
			<br/>
		</p>
		<p class="submit">
			<input type="submit" value="<fmt:message key='wip.config.save' />" />
		</p>
	</form>
	<% session.removeAttribute("errors"); %>
</div>