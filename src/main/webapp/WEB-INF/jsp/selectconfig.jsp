<%@include file="/WEB-INF/jsp/header.jsp" %>

<table class="wip_table">
    <%
        List<String> confs = XMLWIPConfigurationDAO.getInstance().getConfigurationsNames();
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
					<td>
						<%
						if(wipConf.getName().equals(conf))
							out.print("<strong>" + conf + "</string>"); 
						else
							out.print(conf);
						%>
					</td>
					<td>
						<a href="<portlet:actionURL><portlet:param name="<%=Attributes.ACTION_SELECT.name() %>" value="<%=conf%>" /></portlet:actionURL>"><fmt:message key="wip.config.action.select"/></a>
					</td>
				</tr>
  	  <%
            }
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