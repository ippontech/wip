<%@include file="/WEB-INF/jsp/header.jsp" %>

<%
List<String> urls = WIPLogging.INSTANCE.getUrlsLogged();
if(urls.size() == 0) {
	out.print("no log");
} else {
%>
<table class="wip_table">
		<tr>
			<th><fmt:message key="wip.config.configName"/></th>
			<th><fmt:message key="wip.config.actions"/></th>
		</tr>
	<%
        for (String url : urls) {
	  %>
			<tr>
				<td>
					<%=url%>
				</td>
				<td>
					<a href="<portlet:resourceURL><portlet:param name="<%=Attributes.ACTION_EXPORT_LOG.name() %>" value="<%=url%>" /></portlet:resourceURL>"><fmt:message key="wip.logs.show_log"/></a>
				</td>
			</tr>
  	  <%
		}
    %>
</table>
<% 
}

boolean debugMode = WIPUtil.isDebugMode(pReq);%>
<a href="<portlet:actionURL>
	<portlet:param	name="<%=Attributes.DEBUG_MODE.name() %>"
					value="<%=Boolean.toString(!debugMode)%>" />
	</portlet:actionURL>">
	
	<%if(debugMode) { %>
		<fmt:message key="wip.config.debug.desactivate" />
	<%} else { %>
		<fmt:message key="wip.config.debug.activate" />
	<%}%>
</a>

<style>

	.wip_table {
		border: 1px solid #000000;
	}
	
	.wip_table tr th {
		text-align: center;
		padding:7px 7px 7px 7px;
	}
	
	.wip_table tr td {
		padding:5px 5px 5px 5px;
		background-color: #fff;
	}
</style>