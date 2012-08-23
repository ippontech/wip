<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:urlfactory="fr.ippon.wip.http.UrlFactory"
                xmlns:jstransformer="fr.ippon.wip.transformers.JSTransformer"
                xmlns:csstransformer="fr.ippon.wip.transformers.CSSTransformer"
                xmlns:html="http://www.w3.org/1999/xhtml"
                extension-element-prefixes="urlfactory jstransformer csstransformer"
                exclude-result-prefixes="urlfactory jstransformer csstransformer html">

    <xsl:output method="html" encoding="UTF-8" indent="no" cdata-section-elements=""
                standalone="no" omit-xml-declaration="yes" />

    <xsl:param name="request"/>
    <xsl:param name="response"/>
    <xsl:param name="wip_divClassName"/>
    <xsl:param name="retrieveCss"/>
    <xsl:param name="rewriteUrl"/>
    <xsl:param name="type"/>
    <xsl:param name="xpath"/>
    <xsl:param name="actualUrl"/>
    <xsl:param name="urlfact"/>

	<!-- WARNING: DON'T INSTANCIATE GENERIC CLASS IF THE XSLT IS PARSED BY NEKOHTML  -->
    <xsl:variable name="jstrans" select="jstransformer:new( $request, $response, $actualUrl )"/>
    <xsl:variable name="csstrans" select="csstransformer:new( $request, $response, $actualUrl )"/>

    <xsl:include href="clipping"/>

    <xsl:template match="/">
        <!-- Process nothing else outside of HTML/HEAD & HTML/BODY nodes -->
        <xsl:apply-templates select="/html:HTML/html:HEAD"/>
        <xsl:apply-templates select="/html:HTML/html:BODY"/>
    </xsl:template>

    <xsl:template match="/html:HTML/html:HEAD">
        <!-- Process only STYLE & SCRIPT nodes as well as CSS LINK -->
        <xsl:apply-templates select="html:STYLE"/>
        <xsl:apply-templates select="html:LINK[@rel='stylesheet']"/>
        <xsl:apply-templates select="html:SCRIPT"/>
        <!-- Call included 'head-clipping' template for additional processing -->
        <xsl:call-template name="head-clipping"/>
    </xsl:template>
    
    <xsl:template match="/html:HTML/html:BODY">
        <!-- Create a new SCRIPT node with BODY @onload content -->
        <xsl:apply-templates select="@onload"/>
        <!-- Create an enclosing DIV with a class attribute passed as a parameter to this stylesheet by the WIPortlet -->
        <xsl:element name="DIV">
            <xsl:attribute name="class">
                <xsl:value-of select=" $wip_divClassName "/>
            </xsl:attribute>
            <!-- Create another enclosing DIV with the same class attribute than the original BODY -->
            <xsl:element name="DIV">
                <xsl:attribute name="class">
                    <xsl:value-of select="@class"/>
                </xsl:attribute>
                <!-- Process children nodes of BODY with included 'body-clipping' template -->
                <xsl:call-template name="body-clipping"/>
            </xsl:element>
            <!-- Create a STYLE node with custom CSS from transformer -->
            <xsl:element name="STYLE">
                <xsl:value-of disable-output-escaping="yes" select="csstransformer:getCustomCss($csstrans)"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <!-- Rewrite SCRIPT nodes -->
    <xsl:template match="html:SCRIPT">
        <xsl:choose>
            <!-- If there is a src attribute, rewrite URL and force type to "text/javascript" -->
            <xsl:when test="string-length(./@src) > 0">
                <xsl:element name="SCRIPT">
                    <xsl:attribute name="type">
                        <xsl:text>text/javascript</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="src">
                        <xsl:value-of
                                select="urlfactory:createProxyUrl( $urlfact, ./@src, 'GET', 'JS', $response)"/>
                    </xsl:attribute>
                    <xsl:text></xsl:text>
                </xsl:element>
            </xsl:when>
            <!-- Else force type to "text/javascript" and insert transformed original content -->
            <xsl:otherwise>
                <xsl:element name="SCRIPT">
                    <xsl:attribute name="type">
                        <xsl:text>text/javascript</xsl:text>
                    </xsl:attribute>
                    <xsl:value-of disable-output-escaping="yes" select="jstransformer:transform( $jstrans, .)"/>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Rewrite CSS LINK nodes -->
    <xsl:template match="html:LINK[@rel='stylesheet']">
        <xsl:if test="$retrieveCss = 'true'">
            <!-- If 'retrieveCss' parameter is true, output LINK node with forced 'rel' and 'type' attributes, rewrite URL and set it in 'href' attribute' -->
            <xsl:element name="LINK">
                <xsl:attribute name="rel">
                    <xsl:text>stylesheet</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="type">
                    <xsl:text>text/css</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="href">
                    <xsl:value-of 
                            select="urlfactory:createProxyUrl( $urlfact, ./@href, 'GET', 'CSS', $response)"/>
                </xsl:attribute>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <!-- Rewrite STYLE nodes -->
    <xsl:template match="html:STYLE">
        <xsl:if test="$retrieveCss = 'true'">
            <!-- If 'retrieveCss' parameter is true, output STYLE and insert transformed original content -->
            <xsl:element name="STYLE">
                <xsl:value-of disable-output-escaping="yes" select="csstransformer:transform( $csstrans, .)"/>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <!-- Rewrite 'style' attributes -->
    <xsl:template match="@style">
        <!-- Ouput 'style' attribute' and insert transformed orginal content -->
        <xsl:attribute name="style">
            <xsl:value-of select="csstransformer:transform( $csstrans, .)"/>
        </xsl:attribute>
    </xsl:template>

    <!-- Rewrite A nodes -->
    <xsl:template match="html:A/@href">
        <xsl:choose>
            <xsl:when test="starts-with(., '#') or starts-with(., 'mailto')">
                <xsl:attribute name="href">
                    <xsl:value-of select="."/>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="starts-with(., 'javascript:')">
                <xsl:attribute name="href">
                    <xsl:value-of select="jstransformer:transform( $jstrans, .)"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="$rewriteUrl = 'true'">
                <xsl:attribute name="href">
                    <xsl:value-of select="urlfactory:createProxyUrl( $urlfact, ., 'GET', 'HTML', $response)"/>
                </xsl:attribute>
                <xsl:attribute name="target">
                    <xsl:text>_self</xsl:text>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="href">
                    <xsl:value-of select="."/>
                </xsl:attribute>
                <xsl:attribute name="target">
                    <xsl:text>_blank</xsl:text>
                </xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Rewrite forms -->
    <xsl:template match="html:FORM">
        <xsl:element name="FORM">
            <xsl:apply-templates select="@*"/>
            <xsl:choose>
                <xsl:when test="./@method='post' or ./@method='POST'">
                    <xsl:attribute name="action">
                        <xsl:value-of
                                select="urlfactory:createProxyUrl( $urlfact, ./@action, 'POST', 'HTML', $response)"/>
                    </xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="action">
                        <xsl:value-of
                                select="urlfactory:createProxyUrl( $urlfact, ./@action, 'GET', 'HTML', $response)"/>
                    </xsl:attribute>
                    <xsl:attribute name="method">
                        <xsl:text>post</xsl:text>
                    </xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="node()"/>
        </xsl:element>
    </xsl:template>

    <!-- Rewrite IMG src, INPUT src, OBJECT data & EMBED src attributes -->
    <xsl:template match="html:IMG/@src | html:INPUT/@src | html:OBJECT/@data | html:EMBED/@src">
        <xsl:attribute name="{name()}">
            <xsl:value-of select="urlfactory:createProxyUrl( $urlfact, ., 'GET', 'RAW', $response)"/>
        </xsl:attribute>
    </xsl:template>

    <!-- Rewrite onload attributes into SCRIPT elements -->
    <xsl:template match="@onload">
        <xsl:element name="SCRIPT">
            <xsl:attribute name="type">
                <xsl:text>text/javascript</xsl:text>
            </xsl:attribute>
            <xsl:value-of select="jstransformer:transform( $jstrans, .)"/>
        </xsl:element>
    </xsl:template>

    <!-- Rewrite various javascript attributes-->
    <xsl:template match="@onunload | @onclick | @onmousedown | @onmouseup | @onmouseover | @onmousemove | @onmouseout | @onfocus | @onblur | @onkeypress | @onkeydown | @onkeyup | @onreset | @onchange | @ondblclick | @onsubmit | @onselect">
        <xsl:attribute name="{name()}">
            <xsl:value-of select="jstransformer:transform( $jstrans, .)"/>
        </xsl:attribute>
    </xsl:template>

    <!-- Preserve whitespaces and line breaks for PRE nodes-->
    <xsl:template match="html:PRE" xml:space="preserve" >
		<xsl:copy-of select="."/>
	</xsl:template>

    <!-- Default : create node with same name (without namespace) and process attributes and children -->
    <xsl:template match="*">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <!-- Default : copy attributes, texts, comments and processing-instructions -->
    <xsl:template match="@*|text()|comment()|processing-instruction()">
        <xsl:copy/>
    </xsl:template>

</xsl:stylesheet>