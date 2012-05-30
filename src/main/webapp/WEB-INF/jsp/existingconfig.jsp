<%@include file="/WEB-INF/jsp/header.jsp" %>

<% 
	String sconf = (String)pReq.getPortletSession().getAttribute("configPage");
	if (sconf != null) {
		if (sconf.equals("all")) {
%>
			<table class="wip_table">
				<%
					List<String> confs = WIPConfigurationManager.getInstance().getSavedConfigurations();
					if (confs.size() == 0) {
						out.print("<fmt:message key=\"wip.config.noconfig\" />");
					} else {
				%>
							<tr>
								<th>Configuration Name</th>
								<th>Action</th>
							</tr>
				<%
						for (String conf : confs) {
				%>
							<tr>
								<td><%=conf%></td>
								<td>
									<a href="<portlet:actionURL><portlet:param name="configPage" value="<%=conf%>" /></portlet:actionURL>">show</a>
									<c:out value=" | "></c:out>
									<a href="<portlet:actionURL><portlet:param name="changeConfig" value="<%=conf%>" /></portlet:actionURL>">select</a>
									<c:out value=" | "></c:out>
									<a href="<portlet:actionURL><portlet:param name="deleteConfig" value="<%=conf%>" /></portlet:actionURL>">delete</a>
								</td>
							</tr>
				<%
						}
					}
				%>
			</table>
<%
		} else { 
			String conf = WIPConfigurationManager.getInstance().getSavedConfiguration(sconf);
			out.print(conf
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