<%@include file="/WEB-INF/jsp/header.jsp" %>

<script type="text/javascript">

	function display(name) {
		if (name == 'none') {
			window.document.getElementById('xpathform').style.display = 'none';
			window.document.getElementById('xsltform').style.display = 'none';
		}
		if (name == 'xpath') {
			window.document.getElementById('xsltform').style.display = 'none';
			window.document.getElementById('xpathform').style.display = 'block';
		}
		if (name == 'xslt') {
			window.document.getElementById('xpathform').style.display = 'none';
			window.document.getElementById('xsltform').style.display = 'block';
		}
		return true;
	}

	function reset() {
		window.document.getElementById('xsltClipping').value = '';
		window.document.wipform.submit();
	}

	function toggleSourceUrl() {
		var div = document.getElementById('sourceUrl');
		if (div.style.display == 'none')
			div.style.display = '';
		else
			div.style.display = 'none';
	}

	function toggleOtherUrl() {
		var div = document.getElementById('otherUrl');
		if (div.style.display == 'none')
			div.style.display = '';
		else
			div.style.display = 'none';
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
				<option value="clipping" selected="selected"><fmt:message key="wip.config.easyclipping"/></option>
				<option value="cssrewriting"><fmt:message key="wip.config.cssrewriting"/></option>
				<option value="jsrewriting"><fmt:message key="wip.config.jsrewriting"/></option>
				<option value="ltpaauth"><fmt:message key="wip.config.ltpaauth"/></option>
			</select>
		</form>
		</div>
	</div>
	<form method="POST" action="<portlet:actionURL/>" name="wipform" class="wip_form">
		<input type="hidden" name="form" value="2"/>
		<p class="line">
			<table id="wiptable" cellspacing="0" cellpadding="0">
				<tr>
					<td rowspan="3" align="right" width="50%"><strong><fmt:message key="wip.config.clippingtype" /> : &nbsp;</strong></td>
					<td align="left" width="50%">				
						<input type="radio" name="clippingType" value="none" id="none" onclick="javascript:display('none');" 
						<% if (!errors.containsKey("xPath")&&wipConf.getClippingType().equals("none")) { %> checked <% } %> /><label for="none"> None </label>
					</td>
					<td><%= printHelp(rb.getString("wip.help.clippingtype")) %></td>
				</tr>
				<tr>
					<td align="left" width="50%">
						<input type="radio" name="clippingType" value="xpath" id="xpath" onclick="javascript:display('xpath');" 
						<% if (errors.containsKey("xPath")||wipConf.getClippingType().equals("xpath")) { %> checked <% } %> /><label for="xpath"> XPath </label>
					</td>
					<td></td>
				</tr>
				<tr>
					<td align="left" width="50%">
						<input type="radio" name="clippingType" value="xslt" id="xslt" onclick="javascript:display('xslt');" 
						<% if (!errors.containsKey("xPath")&&wipConf.getClippingType().equals("xslt")) { %> checked <% } %> /><label for="xslt"> XSLT </label>
					</td>
					<td></td>
				</tr>
			</table>
		</p>
		<div id="xpathform" <% if (!(errors.containsKey("xPath")||wipConf.getClippingType().equals("xpath"))) { %> style="display:none;" <% } %>>
			<p class="line">
				<label><fmt:message key="wip.config.xpath" /></label>
				<input type="text" name="xPath" value="<%= wipConf.getXPath() %>"/>	
				<%= printHelp(rb.getString("wip.help.xpath")) %>
				<%= printError("xPath", errors) %>
			</p>
		</div>
		<div id="xsltform" <% if (!wipConf.getClippingType().equals("xslt")||(errors.containsKey("xPath"))) { %> style="display:none;" <% } %>>
			<p class="line">
				<label for="xsltClipping"><fmt:message key="wip.config.xsltclipping" /> : (<a href="#" onclick="javascript:reset();">reset</a>)</label>
				<textarea name="xsltClipping" id="xsltClipping"><%= wipConf.getXsltClipping() %></textarea>
				<%= printHelp(rb.getString("wip.help.xsltclipping")) %>
				<br/>
			</p>
		</div>
		
		<p class="submit">
			<input type="submit" value="<fmt:message key='wip.config.save' />" />
		</p>
	</form>
	<div>
		<a href="Javascript:toggleSourceUrl();"><fmt:message key="wip.config.source" /></a>
		<div id="sourceUrl" style="display:none;">
			<a href="<portlet:actionURL><portlet:param name="source" value="init" /></portlet:actionURL>" title="source" target="_blank"><fmt:message key="wip.config.initurl" /></a>
			<a href="Javascript:toggleOtherUrl();"><fmt:message key="wip.config.otherurl" /></a>
			<div id="otherUrl" style="display:none;">
				<form action="<portlet:actionURL><portlet:param name="source" value="other" /></portlet:actionURL>" method="post" target="_blank">
					<input type="text" name="url" />&nbsp;
					<input type="submit" value="<fmt:message key="wip.config.see"/>" />
				</form>
			</div>
		</div>
	</div>
	<% session.removeAttribute("errors"); %>
</div>