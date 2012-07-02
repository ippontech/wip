<%@include file="/WEB-INF/jsp/config/header.jsp" %>

<form action="<portlet:actionURL/>" method="post" enctype="multipart/form-data">
	<input type="file" name="configuration">
	 <input type="submit" value="<fmt:message key='wip.config.save' />"/>
</form>

<table class=wip_table>
    <%
        List<String> confs = wipConfigurationDAO.getConfigurationsNames();
		%>
		<tr>
			<th><fmt:message key="wip.config.configName"/></th>
			<th><fmt:message key="wip.config.actions"/></th>
		</tr>
		<%
	
		for (String conf : confs) {
 	   %>
			<tr>
				<td>
					<%
						if(wipConf.getName().equals(conf))
							out.print("<strong>" + conf + "</strong>");
						else
							out.print(conf);
					%>
				</td>
				<td>
					<a href="<portlet:actionURL><portlet:param name="<%=Attributes.ACTION_SELECT_CONFIGURATION.name()%>" value="<%=conf%>" /></portlet:actionURL>"><fmt:message key="wip.config.action.select"/></a>
					<c:out value=" | "></c:out>
					<a href="<portlet:actionURL><portlet:param name="<%=Attributes.ACTION_DELETE_CONFIGURATION.name()%>" value="<%=conf%>" /></portlet:actionURL>"><fmt:message key="wip.config.action.delete"/></a>
					<c:out value=" | "></c:out>
					<a href="<portlet:resourceURL><portlet:param name="<%=Attributes.ACTION_EXPORT_CONFIGURATION.name()%>" value="<%=conf %>"/></portlet:resourceURL>">
						<fmt:message key="wip.config.action.export"/>
					</a>
				</td>
			</tr>
  	  <%
		}
    %>
</table>

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