<%@include file="/WEB-INF/jsp/header.jsp" %>

<% String authType = (String )request.getAttribute("authType"); %>

<div id="auth">
	<h2><fmt:message key="wip.auth.title" /></h2>
	<% if (!authType.equals("unknown"))  {%>
	<form method="POST" action="<portlet:actionURL/>" class="wip_form">
		<input type="hidden" name="auth" value="login"/>
		<input type="hidden" name="authType" value="<%= request.getAttribute("authType") %>"/>
		<p class="line">
			<table id="wiptable" cellspacing="0" cellpadding="0">
				<tr>
					<td align="right" width="35%"><label for="login"> <fmt:message key="wip.auth.login" /> :&nbsp; </label></td>
					<td align="left" width="65%"><input type="text" name="login" id="login" /></td>
				</tr>
				<tr>
					<td align="right" width="35%"><label for="password"> <fmt:message key="wip.auth.password" /> :&nbsp; </label></td>
					<td align="left" width="65%"><input type="password" name="password" id="password" /></td>
				</tr>
			</table>
		</p>
		<p class="submit">
			<input type="submit" value="<fmt:message key='wip.auth.submit' />" />
		</p>
	</form>
	<% } else { %>
	<p class="line">
		<strong><fmt:message key="wip.auth.unsupported" /></strong>
	</p>
	<% } session.removeAttribute("errors"); %>
</div>