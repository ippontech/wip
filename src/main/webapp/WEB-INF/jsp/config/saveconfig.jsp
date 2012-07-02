<%@include file="/WEB-INF/jsp/config/header.jsp" %>

<p><fmt:message key="wip.config.chooseconfigname"/></p>

<form id="saveConfig" action="<portlet:actionURL/>" method="POST">
    <input type="text" name="<%=Attributes.ACTION_SAVE_CONFIGURATION_AS.name()%>"/>
    <input type="submit" value="<fmt:message key="wip.config.save"/>"/>
</form>

