<%@include file="/WEB-INF/jsp/header.jsp" %>

<p><fmt:message key="wip.config.chooseconfigname"/></p>

<form id="saveConfig" action="<portlet:actionURL/>" method="POST">
    <input type="text" name="saveConfig"/>
    <input type="submit" value="<fmt:message key="wip.config.save"/>"/>
</form>

