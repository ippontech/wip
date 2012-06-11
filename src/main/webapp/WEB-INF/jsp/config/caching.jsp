<%@include file="/WEB-INF/jsp/config/header.jsp" %>

<div id="editForm">
    <div id="editHeader">
        <div class="left">
			<h5>Configuration: <%= wipConf.getName() %></h5>
		</div>
        <div class="right">
             <form id="changePage" action="<portlet:actionURL/>" method="POST">
                <select name="<%= Attributes.PAGE.name() %>" onchange="this.form.submit();return(false);">
                    <option value="GENERAL_SETTINGS"><fmt:message key="wip.config.generalsettings"/></option>
                    <option value="CACHING" selected="selected"><fmt:message key="wip.config.caching"/></option>
                    <option value="HTML_REWRITING"><fmt:message
                            key="wip.config.htmlrewriting"/></option>
                    <option value="CLIPPING"><fmt:message key="wip.config.easyclipping"/></option>
                    <option value="CSS_REWRITING"><fmt:message key="wip.config.cssrewriting"/></option>
                    <option value="JS_REWRITING"><fmt:message key="wip.config.jsrewriting"/></option>
                    <option value="LTPA_AUTH"><fmt:message key="wip.config.ltpaauth"/></option>
                </select>
            </form>
        </div>
    </div>
    <form method="POST" action="<portlet:actionURL/>" name="wipform" class="wip_form">
        <input type="hidden" name="form" value="6"/>

        <p class="line">
            <label for="enableCache"><fmt:message key="wip.config.enablecache"/> :</label>
            <input type="checkbox" name="enableCache" id="enableCache"
                   onclick="checkCache();" <% if (wipConf.getEnableCache()) out.print("checked"); %> />
            <%= printHelp("wip.help.enablecache", locale) %>
        </p>

        <div id="enableCacheDiv" <% if (!wipConf.getEnableCache()) out.print("style=\"display:none;\""); %>>
            <h5><fmt:message key="wip.config.pagecache"/></h5>

            <p class="line">
                <label for="pageCachePrivate"><fmt:message key="wip.config.pagecacheprivate"/> :</label>
                <input type="checkbox" name="pageCachePrivate"
                       id="pageCachePrivate" <% if (wipConf.getPageCachePrivate()) out.print("checked"); %> />
                <%= printHelp("wip.help.pagecacheprivate", locale) %>
            </p>

            <p class="line">
                <label for="forcePageCaching"><fmt:message key="wip.config.forcepagecaching"/> :</label>
                <input type="checkbox" name="forcePageCaching" id="forcePageCaching"
                       onclick="checkForcePageCaching();" <% if (wipConf.getForcePageCaching())
                    out.print("checked"); %> />
                <%= printHelp("wip.help.forcepagecaching", locale) %>
            </p>

            <p class="line" id="pageCacheTimeoutBloc" <% if (!wipConf.getForcePageCaching())
                out.print("style=\"display:none;\""); %>>
                <label for="pageCacheTimeout"><fmt:message key="wip.config.pagecachetimeout"/> :</label>
                <input type="text" name="pageCacheTimeout" id="pageCacheTimeout"
                       value="<%= wipConf.getPageCacheTimeout() %>" size="10"/>
                <%= printHelp("wip.help.pagecachetimeout", locale) %>
            </p>

            <h5><fmt:message key="wip.config.resourcecache"/></h5>

            <p class="line">
                <label for="resourceCachePublic"><fmt:message key="wip.config.resourcecachepublic"/> :</label>
                <input type="checkbox" name="resourceCachePublic"
                       id="resourceCachePublic" <% if (wipConf.getResourceCachePublic()) out.print("checked"); %> />
                <%= printHelp("wip.help.resourcecachepublic", locale) %>
            </p>

            <p class="line">
                <label for="forceResourceCaching"><fmt:message key="wip.config.forceresourcecaching"/> :</label>
                <input type="checkbox" name="forceResourceCaching" id="forceResourceCaching"
                       onclick="checkForceResourceCaching();" <% if (wipConf.getForceResourceCaching())
                    out.print("checked"); %> />
                <%= printHelp("wip.help.forceresourcecaching", locale) %>
            </p>

            <p class="line" id="resourceCacheTimeoutBloc" <% if (!wipConf.getForceResourceCaching())
                out.print("style=\"display:none;\""); %>>
                <label for="resourceCacheTimeout"><fmt:message key="wip.config.resourcecachetimeout"/> :</label>
                <input type="text" name="resourceCacheTimeout" id="resourceCacheTimeout"
                       value="<%= wipConf.getResourceCacheTimeout() %>" size="10"/>
                <%= printHelp("wip.help.resourcecachetimeout", locale) %>
            </p>
        </div>
        <p class="submit">
            <input type="submit" value="<fmt:message key='wip.config.save' />"/>
        </p>
    </form>
    <% session.removeAttribute("errors"); %>
</div>

<script type="text/javascript">

    function checkCache() {
        if (document.forms.wipform.enableCache.checked) {
            document.getElementById('enableCacheDiv').style.display = 'block';
        } else {
            document.getElementById('enableCacheDiv').style.display = 'none';
        }
        return true;
    }

    function checkForcePageCaching() {
        if (document.forms.wipform.forcePageCaching.checked) {
            document.getElementById('pageCacheTimeoutBloc').style.display = 'block';
        } else {
            document.getElementById('pageCacheTimeoutBloc').style.display = 'none';
        }
        return true;
    }

    function checkForceResourceCaching() {
        if (document.forms.wipform.forceResourceCaching.checked) {
            document.getElementById('resourceCacheTimeoutBloc').style.display = 'block';
        } else {
            document.getElementById('resourceCacheTimeoutBloc').style.display = 'none';
        }
        return true;
    }

</script>
