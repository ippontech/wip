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
					<option value="jsrewriting" selected="selected"><fmt:message key="wip.config.jsrewriting"/></option>
				</select>
			</form>
		</div>
	</div>
	<form method="POST" action="<portlet:actionURL/>" class="wip_form" onsubmit="saveJavascriptUrls(); saveScriptUrls();">
		<input type="hidden" name="form" value="5"/>
		<p style="margin-left:15px; margin-bottom: 25px;">
			<label for="jsRegex"><fmt:message key="wip.config.jsregex" /> :</label>
			<input type="text" name="jsRegex" id="jsRegex" value="<%= wipConf.getJsRegex() %>" size="60" />
			<%= printHelp(rb.getString("wip.help.jsregex")) %>
		</p>
		<table>
			<tr>
				<td width="50%">
					<label><fmt:message key="wip.config.ajaxurls" /></label>
					<%= printHelp(rb.getString("wip.help.urllistajax")) %>
					<ul id="javascriptUrlList">
						<%
							Map<String, URLTypes> l = wipConf.getJavascriptUrls();
							for (String s : l.keySet()) {
								String entry = s+"::::"+l.get(s).name();
								out.println(
									"<li id=\"javascriptUrl"+ entry +"\">"
									+ 	"<a href=\"JavaScript:removeUrl('javascriptUrl"+entry+"')\">"
 									+ 		"<img src=\""+src+"\" alt=\"remove\" />"
									+	"</a>"
									+ 	"<span>" + s + " - " + l.get(s).name() + "</span>"
									+ "</li>"
								);
							}
						%>
					</ul>
					<input type="text" name="javascriptUrlToAdd" id="javascriptUrlToAdd" />
					<select name="javascriptUrlTypeToAdd" id="javascriptUrlTypeToAdd">
						<% for (URLTypes type : URLTypes.values()) { %>
							<option value="<%= type.name() %>" ><%= type.name() %></option>
						<% } %>
					</select>
					<a href="JavaScript:addUrl('javascriptUrl')">
						<fmt:message key="wip.config.add" />
					</a>
				</td>
				<td width="50%">
					<label><fmt:message key="wip.config.scripturls" /></label>
					<%= printHelp(rb.getString("wip.help.ignorelist")) %>
					<ul id="scriptUrlList">
						<%
							List<String> l3 = wipConf.getScriptsToIgnore();
							for (String s : l3) {
								out.println(
									"<li id=\"scriptUrl"+s+"\">"
									+ 	"<a href=\"JavaScript:removeUrl('scriptUrl"+s+"')\">"
 									+ 		"<img src=\""+src+"\" alt=\"remove\" />"
									+ 	"</a>"
									+	"<span>" + s + "</span>"
									+ "</li>"
								);
							}
						%>
					</ul>
					<input type="text" name="scriptUrlToAdd" id="scriptUrlToAdd" />
					<a href="JavaScript:addUrl('scriptUrl')">
						<fmt:message key="wip.config.add" />
					</a>
				</td>
			</tr>
		</table>
		<p class="submit">
			<input type="hidden" name="javascriptUrls" id="javascriptUrlToSave" />
			<input type="hidden" name="scriptUrls" id="scriptUrlToSave" />
			<input type="submit" value="<fmt:message key='wip.config.save' />" />
		</p>
	</form>
	<% session.removeAttribute("errors"); %>
</div>


<script type="text/javascript">

	function saveScriptUrls() {
		var list = document.getElementById("scriptUrlList");
		var urls = list.getElementsByTagName("li");
		var s = "";
		for (var i=0; i<urls.length; i++) {
			s += urls[i].getElementsByTagName("span")[0].innerHTML;
			if (i != urls.length - 1) s += ";";
		}
		document.getElementById("scriptUrlToSave").value = s;
	}

	function saveJavascriptUrls() {
		var list = document.getElementById("javascriptUrlList");
		var urls = list.getElementsByTagName("li");
		var s = "";
		for (var i=0; i<urls.length; i++) {
			s += urls[i].getElementsByTagName("span")[0].innerHTML.split(" - ")[0];
			s += "::::";
			s += urls[i].getElementsByTagName("span")[0].innerHTML.split(" - ")[1];
			if (i != urls.length - 1) s += ";";
		}
		document.getElementById("javascriptUrlToSave").value = s;
	}
	
	/**
	 * Add a url in the ul/li list according to the given parameter.
	 * @param type the url type (ajaxUrl | regularUrl)
	 */
	function addUrl(type) {
		var l = document.getElementById(type+"List");
		var e = document.getElementById(type+"ToAdd");
		if (e.value != "") {
			if (type == 'javascriptUrl') {
				var urlType = document.getElementById("javascriptUrlTypeToAdd").value;
				var toAdd = e.value + "::::" + urlType;
				l.innerHTML += "<li id='"+type+toAdd+"'><a href=\"JavaScript:removeUrl('"+type+toAdd+"')\"><img src=\"<%=src%>\" alt=\"remove\" /></a><span>"+e.value+" - "+urlType+"</span></li>";
				e.value = "";
			} else {
				l.innerHTML += "<li id='"+type+e.value+"'><a href=\"JavaScript:removeUrl('"+type+e.value+"')\"><img src=\"<%=src%>\" alt=\"remove\" /></a><span>"+e.value+"</span></li>";
				e.value = "";	
			}
		}
	}
	
	/**
	 * Remove the url with the given id.
	 * @param id the id of the element to remove
	 */
	function removeUrl(id) {
		var element = document.getElementById(id);
		var parent = element.parentNode;
		parent.removeChild(element);
		
	}
	
</script>