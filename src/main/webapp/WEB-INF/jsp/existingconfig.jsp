<%@include file="/WEB-INF/jsp/header.jsp" %>

<% 
	String sconf = request.getParameter("configPage");
	if (sconf != null) {
		if (sconf.equals("all")) { 
%>
			<table class="configs">
				<%
					List<String> confs = WIPConfigurationManager.getInstance().getSavedConfigurations();
					if (confs.size() == 0) { 
						out.print("<fmt:message key=\"wip.config.noconfig\" />");
					} else {
						for (int i=0; i<confs.size(); i++) {
							String conf = confs.get(i);
				%>
							<tr>
								<td class="c1"><%=conf%></td>
								<td class="c2"><a href="<portlet:actionURL><portlet:param name="configPage" value="<%=conf%>" /></portlet:actionURL>">voir</a></td>
								<td class="c3"><a href="<portlet:actionURL><portlet:param name="changeConfig" value="<%=conf%>" /></portlet:actionURL>">choisir</a></td>
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

	table.configs.c2, table.configs.c3 {
		padding-left: 20px;
	}
	
</style>