<%@include file="/WEB-INF/jsp/header.jsp" %>

<table class="wip_table">
		<tr>
			<th><fmt:message key="wip.config.configName"/></th>
			<th><fmt:message key="wip.config.actions"/></th>
		</tr>
	<%
	List<String> confs = wipConfigurationDAO.getConfigurationsNames();
        for (String conf : confs) {
	  %>
			<tr>
				<td>
					<%
					if(wipConf.getName().equals(conf))
						out.print("<strong>" + conf + "</string>"); 
					else
						out.print(conf);
					%>
				</td>
				<td>
					<a href="<portlet:actionURL><portlet:param name="<%=Attributes.ACTION_SELECT_CONFIGURATION.name()%>" value="<%=conf%>" /></portlet:actionURL>"><fmt:message key="wip.config.action.select"/></a>
				</td>
			</tr>
  	  <%
		}
    %>
</table>

<% boolean debugMode = WIPUtil.isDebugMode(pReq);%>
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