<%@include file="/WEB-INF/jsp/header.jsp" %>

<div id="editForm">
	<div id="editHeader">
		<div class="left"><h2><fmt:message key="wip.config.title"/></h2></div>
		<div class="right">
			<form id="changePage" action="<portlet:actionURL/>" method="POST">
				<select name="editPage" onchange="javascript:this.form.submit();return(false);">
					<option value="generalsettings"><fmt:message key="wip.config.generalsettings"/></option>
					<option value="caching" selected="selected"><fmt:message key="wip.config.caching" /></option>
					<option value="htmlrewriting"><fmt:message key="wip.config.htmlrewriting"/></option>
					<option value="clipping"><fmt:message key="wip.config.easyclipping"/></option>
					<option value="cssrewriting"><fmt:message key="wip.config.cssrewriting"/></option>
					<option value="jsrewriting"><fmt:message key="wip.config.jsrewriting"/></option>
				</select>
			</form>
		</div>
	</div>	
	<form method="POST" action="<portlet:actionURL/>" name="wipform" class="wip_form">
		<input type="hidden" name="form" value="6"/>
		<p class="line">
			<label for="enableCache"><fmt:message key="wip.config.enablecache" /> :</label>
			<input type="checkbox" name="enableCache" id="enableCache" onclick="javascript:checkCache();" <% if (wipConf.getEnableCache()) out.print("checked"); %> />
			<%= printHelp(rb.getString("wip.help.enablecache")) %>
		</p>
		<div id="enableCacheDiv" <% if (!wipConf.getEnableCache()) out.print("style=\"display:none;\""); %>>
			<h5><fmt:message key="wip.config.pagecache" /></h5>
			<p class="line" >
				<label for="pageCachePrivate"><fmt:message key="wip.config.pagecacheprivate" /> :</label>
				<input type="checkbox" name="pageCachePrivate" id="pageCachePrivate" <% if (wipConf.getPageCachePrivate()) out.print("checked"); %> />
				<%= printHelp(rb.getString("wip.help.pagecacheprivate")) %>
			</p>
			<p class="line" >
				<label for="forcePageCaching"><fmt:message key="wip.config.forcepagecaching" /> :</label>
				<input type="checkbox" name="forcePageCaching" id="forcePageCaching" onclick="javascript:checkForcePageCaching();" <% if (wipConf.getForcePageCaching()) out.print("checked"); %> />
				<%= printHelp(rb.getString("wip.help.forcepagecaching")) %>
			</p>
			<p class="line" id="pageCacheTimeoutBloc" <% if (!wipConf.getForcePageCaching()) out.print("style=\"display:none;\""); %>>
				<label for="pageCacheTimeout"><fmt:message key="wip.config.pagecachetimeout" /> :</label>
				<input type="text" name="pageCacheTimeout" id="pageCacheTimeout" value="<%= wipConf.getPageCacheTimeout() %>" size="10" />
				<%= printHelp(rb.getString("wip.help.pagecachetimeout")) %>
			</p>
			
			<h5><fmt:message key="wip.config.resourcecache" /></h5>
			<p class="line" >
				<label for="resourceCachePublic"><fmt:message key="wip.config.resourcecachepublic" /> :</label>
				<input type="checkbox" name="resourceCachePublic" id="resourceCachePublic" <% if (wipConf.getResourceCachePublic()) out.print("checked"); %> />
				<%= printHelp(rb.getString("wip.help.resourcecachepublic")) %>
			</p>
			<p class="line" >
				<label for="forceResourceCaching"><fmt:message key="wip.config.forceresourcecaching" /> :</label>
				<input type="checkbox" name="forceResourceCaching" id="forceResourceCaching" onclick="javascript:checkForceResourceCaching();" <% if (wipConf.getForceResourceCaching()) out.print("checked"); %> />
				<%= printHelp(rb.getString("wip.help.forceresourcecaching")) %>
			</p>
			<p class="line" id="resourceCacheTimeoutBloc" <% if (!wipConf.getForceResourceCaching()) out.print("style=\"display:none;\""); %>>
				<label for="resourceCacheTimeout"><fmt:message key="wip.config.resourcecachetimeout" /> :</label>
				<input type="text" name="resourceCacheTimeout" id="resourceCacheTimeout" value="<%= wipConf.getResourceCacheTimeout() %>" size="10" />
				<%= printHelp(rb.getString("wip.help.resourcecachetimeout")) %>
			</p>
		</div>
		<p class="submit">
			<input type="submit" value="<fmt:message key='wip.config.save' />" />
		</p>
	</form>
	<% session.removeAttribute("errors"); %>
</div>

<script type="text/javascript">

	function checkCache() {
		if (document.forms.wipform.enableCache.checked) {
			document.getElementById('enableCacheDiv').style.display = 'block';
		} else {
			document.getElementById('enableCacheDiv').style.display = 'none';
		}
		return true;
	}

	function checkForcePageCaching() {
		if (document.forms.wipform.forcePageCaching.checked) {
			document.getElementById('pageCacheTimeoutBloc').style.display = 'block';
		} else {
			document.getElementById('pageCacheTimeoutBloc').style.display = 'none';
		}
		return true;
	}

	function checkForceResourceCaching() {
		if (document.forms.wipform.forceResourceCaching.checked) {
			document.getElementById('resourceCacheTimeoutBloc').style.display = 'block';
		} else {
			document.getElementById('resourceCacheTimeoutBloc').style.display = 'none';
		}
		return true;
	}
	
</script>
