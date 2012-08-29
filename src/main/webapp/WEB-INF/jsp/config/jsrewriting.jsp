<%@include file="/WEB-INF/jsp/config/header.jsp" %>

<%
	String src = request.getContextPath() + "/img/remove.png";
%>

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
                    <option value="CSS_REWRITING"><fmt:message key="wip.config.cssrewriting"/></option>
                    <option value="JS_REWRITING" selected="selected"><fmt:message key="wip.config.jsrewriting"/></option>
                    <option value="LTPA_AUTH"><fmt:message key="wip.config.ltpaauth"/></option>
                </select>
            </form>
        </div>
    </div>
    <form method="POST" action="<portlet:actionURL/>" class="wip_form"
          onsubmit="saveJavascriptUrls(); saveScriptIgnoredUrls(); saveScriptDeletedUrls();">
        <input type="hidden" name="form" value="5"/>

        <p style="margin-left:15px; margin-bottom: 25px;">
            <label for="jsRegex"><fmt:message key="wip.config.jsregex"/> :</label>
            <input type="text" name="jsRegex" id="jsRegex" value="<%=wipConf.getJsRegex()%>" size="60"/>
            <%=printHelp("wip.help.jsregex", locale)%>
        </p>
        <table>
            <tr>
                <td>
                    <label><fmt:message key="wip.config.ajaxurls"/></label>
                    <%=printHelp("wip.help.urllistajax", locale)%>
                    <ul id="javascriptUrlList">
                        <%
                        	Map<String, RequestBuilder.ResourceType> l = wipConf.getJavascriptResourcesMap();
                                                                                                    for (String s : l.keySet()) {
                                                                                                        String entry = s + "::::" + l.get(s).name();
                                                                                                        out.println(
                                                                                                                "<li id=\"javascriptUrl" + entry + "\">"
                                                                                                                        + "<a href=\"JavaScript:removeUrl('javascriptUrl" + entry + "')\">"
                                                                                                                        + "<img src=\"" + src + "\" alt=\"remove\" />"
                                                                                                                        + "</a>"
                                                                                                                        + "<span style='padding-left:15px'>" + s + " - " + l.get(s).name() + "</span>"
                                                                                                                        + "</li>"
                                                                                                        );
                                                                                                    }
                        %>
                    </ul>
                    <input type="text" name="javascriptUrlToAdd" id="javascriptUrlToAdd"/>
                    <select name="javascriptUrlTypeToAdd" id="javascriptUrlTypeToAdd">
                        <%
                        	for (RequestBuilder.ResourceType type : RequestBuilder.ResourceType.values()) {
                        %>
                        <option value="<%=type.name()%>"><%=type.name()%>
                        </option>
                        <%
                        	}
                        %>
                    </select>
                    <a href="javascript:addUrl('javascriptUrl')">
                        <fmt:message key="wip.config.add"/>
                    </a>
                </td>
            </tr>
            <tr>
                <td>
                    <label><fmt:message key="wip.config.scriptignoredurls"/></label>
                    <%=printHelp("wip.help.ignorelist", locale)%>
                    <ul id="scriptIgnoredUrlList">
                        <%
                        	List<String> l3 = wipConf.getScriptsToIgnore();
                                                    for (String s : l3) {
                                                        out.println(
                                                                "<li id=\"scriptIgnoredUrl" + s + "\">"
                                                                        + "<a href=\"JavaScript:removeUrl('scriptIgnoredUrl" + s + "')\">"
                                                                        + "<img src=\"" + src + "\" alt=\"remove\" />"
                                                                        + "</a>"
                                                                        + "<span style='padding-left:15px'>" + s + "</span>"
                                                                        + "</li>"
                                                        );
                                                    }
                        %>
                    </ul>
                    <input type="text" name="scriptIgnoredUrlToAdd" id="scriptIgnoredUrlToAdd"/>
                    <a href="javascript:addUrl('scriptIgnoredUrl')">
                        <fmt:message key="wip.config.add"/>
                    </a>
                </td>
             </tr>
             <tr>
                <td>
                    <label><fmt:message key="wip.config.scriptdeletedurls"/></label>
                    <%=printHelp("wip.help.deletelist", locale)%>
                    <ul id="scriptDeletedUrlList">
                        <%
                        	List<String> l4 = wipConf.getScriptsToDelete();
                                                    for (String s : l4) {
                                                        out.println(
                                                                "<li id=\"scriptDeletedUrl" + s + "\">"
                                                                        + "<a href=\"JavaScript:removeUrl('scriptDeletedUrl" + s + "')\">"
                                                                        + "<img src=\"" + src + "\" alt=\"remove\" />"
                                                                        + "</a>"
                                                                        + "<span style='padding-left:15px'>" + s + "</span>"
                                                                        + "</li>"
                                                        );
                                                    }
                        %>
                    </ul>
                    <input type="text" name="scriptDeletedUrlToAdd" id="scriptDeletedUrlToAdd"/>
                    <a href="javascript:addUrl('scriptDeletedUrl')">
                        <fmt:message key="wip.config.add"/>
                    </a>
                </td>
            </tr>
        </table>
        <p class="submit">
            <input type="hidden" name="javascriptUrls" id="javascriptUrlToSave"/>
            <input type="hidden" name="scriptIgnoredUrls" id="scriptIgnoredUrlToSave"/>
            <input type="hidden" name="scriptDeletedUrls" id="scriptDeletedUrlToSave"/>
       		<%
       			if(!AbstractConfigurationDAO.DEFAULT_CONFIG_NAME.equals(wipConf.getName())) {
       		%>
	            <input type="submit" value="<fmt:message key='wip.config.save' />"/>
    	    <%} %>
            <input type="submit" value="<fmt:message key='wip.config.save' />"/>
        </p>
    </form>
    <% session.removeAttribute("errors"); %>
</div>


<script type="text/javascript">

    function saveScriptIgnoredUrls() {
        var list = document.getElementById("scriptIgnoredUrlList");
        var urls = list.getElementsByTagName("li");
        var s = "";
        for (var i = 0; i < urls.length; i++) {
            s += urls[i].getElementsByTagName("span")[0].innerHTML;
            if (i != urls.length - 1) s += ";";
        }
        document.getElementById("scriptIgnoredUrlToSave").value = s;
    }

    function saveScriptDeletedUrls() {
        var list = document.getElementById("scriptDeletedUrlList");
        var urls = list.getElementsByTagName("li");
        var s = "";
        for (var i = 0; i < urls.length; i++) {
            s += urls[i].getElementsByTagName("span")[0].innerHTML;
            if (i != urls.length - 1) s += ";";
        }
        document.getElementById("scriptDeletedUrlToSave").value = s;
    }

    function saveJavascriptUrls() {
        var list = document.getElementById("javascriptUrlList");
        var urls = list.getElementsByTagName("li");
        var s = "";
        for (var i = 0; i < urls.length; i++) {
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
        var l = document.getElementById(type + "List");
        var e = document.getElementById(type + "ToAdd");
        if (e.value != "") {
            if (type == 'javascriptUrl') {
                var urlType = document.getElementById("javascriptUrlTypeToAdd").value;
                var toAdd = e.value + "::::" + urlType;
                l.innerHTML += "<li id='" + type + toAdd + "'><a href=\"JavaScript:removeUrl('" + type + toAdd + "')\"><img src=\"<%=src%>\" alt=\"remove\" /></a><span>" + e.value + " - " + urlType + "</span></li>";
                e.value = "";
            } else {
                l.innerHTML += "<li id='" + type + e.value + "'><a href=\"JavaScript:removeUrl('" + type + e.value + "')\"><img src=\"<%=src%>\" alt=\"remove\" /></a><span>" + e.value + "</span></li>";
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