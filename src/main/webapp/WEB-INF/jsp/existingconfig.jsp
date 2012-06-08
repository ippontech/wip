<%@include file="/WEB-INF/jsp/header.jsp" %>

<%
    String sconf = (String) pReq.getPortletSession().getAttribute("configPage");
    if (sconf != null) {
        if (sconf.equals("all")) {
%>
<table class="wip_table">
    <%
        List<String> confs = WIPConfigurationManager.getInstance().getConfigurationsNames();
        if (confs.size() == 0) {
            out.print("<fmt:message key=\"wip.config.noconfig\" />");
        } else {
        	
		%>
			<tr>
				<th><fmt:message key="wip.config.configName"/></th>
				<th><fmt:message key="wip.config.actions"/></th>
			</tr>
		<%
		
            for (String conf : confs) {
 	   %>
				<tr>
					<td><%=conf%></td>
					<td>
						<a href="<portlet:actionURL><portlet:param name="configPage" value="<%=conf%>" /></portlet:actionURL>"><fmt:message key="wip.config.action.show"/></a>
						<c:out value=" | "></c:out>
						<a href="<portlet:actionURL><portlet:param name="changeConfig" value="<%=conf%>" /></portlet:actionURL>"><fmt:message key="wip.config.action.select"/></a>
						<c:out value=" | "></c:out>
						<a href="<portlet:actionURL><portlet:param name="deleteConfig" value="<%=conf%>" /></portlet:actionURL>"><fmt:message key="wip.config.action.delete"/></a>
					</td>
				</tr>
  	  <%
            }
        }
    %>
</table>
<%
        } else {
            out.print(wipConf.getConfigAsString()
                    .replaceFirst("<", "&lt;")
                    .replaceAll(">[ |\\s]*<", "&gt;\n\n&lt;")
                    .replaceAll(">", "&gt;\n")
                    .replaceAll("</", "\n&lt;/")
                    .replaceAll("\n", "<br />"));
        }
    }
%>

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