<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:urlfactory="fr.ippon.wip.http.UrlFactory"
	xmlns:jstransformer="fr.ippon.wip.transformers.JSTransformer"
	xmlns:csstransformer="fr.ippon.wip.transformers.CSSTransformer"
	extension-element-prefixes="urlfactory jstransformer csstransformer"
	exclude-result-prefixes="urlfactory jstransformer csstransformer">

	<xsl:output method="html" encoding="UTF-8" indent="yes"
		standalone="no" omit-xml-declaration="yes" />

    <xsl:param name="request" />
    <xsl:param name="response" />
	<xsl:param name="wip_divClassName" />
	<xsl:param name="retrieveCss" />
	<xsl:param name="rewriteUrl" />
		
	<xsl:variable name="urlfact" select="urlfactory:new( $request )" />
	<xsl:variable name="jstrans" select="jstransformer:new( $request, $response )" />
	<xsl:variable name="csstrans" select="csstransformer:new( $request, $response )" />

	

	<xsl:template match="/HTML">
		<xsl:apply-templates select="/HTML/HEAD" />
		<xsl:apply-templates select="/HTML/BODY" />
	</xsl:template>

	<xsl:template match="/HTML/HEAD">
		<xsl:apply-templates select="STYLE" />
		<xsl:apply-templates select="LINK[@rel='stylesheet']" />
		<xsl:apply-templates select="SCRIPT" />
	</xsl:template>

	<xsl:template match="/HTML/BODY">
		<xsl:apply-templates select="@onload"/>
		<DIV>
			<xsl:attribute name="class">
		        <xsl:value-of select=" $wip_divClassName " />
		    </xsl:attribute>
			<DIV>
				<xsl:attribute name="class">
		        	<xsl:value-of select="@class" />
		      	</xsl:attribute>
				<xsl:apply-templates select="node()" />
			</DIV>
			
		    <STYLE>
		    	<xsl:value-of select="csstransformer:getCustomCss($csstrans)" />
		    </STYLE>
		</DIV>
	</xsl:template>

	<!-- Rewrite script resources and code -->
	<xsl:template match="SCRIPT">
		<xsl:choose>
			<xsl:when test="string-length(./@src) > 0">
				<SCRIPT>
					<xsl:attribute name="type">
						<xsl:text>text/javascript</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="src">
						<xsl:value-of
						select="urlfactory:createProxyUrl( $urlfact, ./@src, 'GET', 'JS', $response)" />
					</xsl:attribute>
				</SCRIPT>
			</xsl:when>
			<xsl:otherwise>
				<SCRIPT>
				<xsl:attribute name="type">
					<xsl:text>text/javascript</xsl:text>
				</xsl:attribute>
				<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
				</SCRIPT>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Rewrite CSS resources -->
	<xsl:template match="LINK[@rel='stylesheet']">
	<xsl:if test="$retrieveCss = 'true'">
			<xsl:element name="LINK">
				<xsl:attribute name="rel">
					<xsl:text>stylesheet</xsl:text>
				</xsl:attribute>
				<xsl:attribute name="type">
					<xsl:text>text/css</xsl:text>
				</xsl:attribute>
				<xsl:attribute name="href">
					<xsl:value-of
					select="urlfactory:createProxyUrl( $urlfact, ./@href, 'GET', 'CSS', $response)" />
				</xsl:attribute>
			</xsl:element>
	</xsl:if>
	</xsl:template>

	<!-- Rewrite CSS code -->
	<xsl:template match="STYLE">
		<xsl:if test="$retrieveCss = 'true'">
			<STYLE>
				<xsl:value-of select="csstransformer:transform( $csstrans, .)" />
			</STYLE>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="//@style">
		<xsl:attribute name="style">
				<xsl:value-of select="csstransformer:transform( $csstrans, .)" />
		</xsl:attribute>
	</xsl:template>

	<!-- Rewrite links -->
	<xsl:template match="//A">
		<xsl:copy>
			<xsl:for-each select="./@*">
				<xsl:attribute name="{name()}"> 
					<xsl:value-of select="."/>    
				</xsl:attribute>
			</xsl:for-each>
			<xsl:choose>
				<xsl:when test="starts-with(./@href, '#')">
					<xsl:attribute name="href">
						<xsl:value-of select="./@href" />
					</xsl:attribute>
				</xsl:when>
				<xsl:when test="starts-with(./@href, 'mailto')">
					<xsl:attribute name="href">
						<xsl:value-of select="./@href" />
					</xsl:attribute>
				</xsl:when>
				<xsl:when test="starts-with(./@href, 'javascript:')">
					<xsl:attribute name="href">
						<xsl:value-of select="jstransformer:transform( $jstrans, ./@href)" />
					</xsl:attribute>
				</xsl:when>
				<xsl:when test="contains( substring(./@href, 1, 12), '://')">
					<xsl:variable name="tmplink"><xsl:value-of select="urlfactory:createProxyUrl( $urlfact, ./@href, 'GET', 'HTML', $response)"/></xsl:variable>
					<xsl:choose>
						<xsl:when test="($tmplink = 'external') or ($rewriteUrl = 'false')">
							<xsl:attribute name="href">
								<xsl:value-of select="./@href" />
							</xsl:attribute>
							<xsl:attribute name="target">
								<xsl:text>_blank</xsl:text>
							</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="href">
								<xsl:value-of select=" $tmplink " />
							</xsl:attribute>
							<xsl:attribute name="target">
								<xsl:text>_self</xsl:text>
							</xsl:attribute>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="href">
						<xsl:value-of select="urlfactory:createProxyUrl( $urlfact, ./@href, 'GET', 'HTML', $response)" />
					</xsl:attribute>
					<xsl:if test="$rewriteUrl = 'false'">
						<xsl:attribute name="target">
							<xsl:text>_blank</xsl:text>
						</xsl:attribute>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="node()" />
		</xsl:copy>
	</xsl:template>

	<!-- Rewrite forms -->
	<xsl:template match="//FORM">
		<xsl:copy>
			<xsl:for-each select="./@*">
				<xsl:attribute name="{name()}"> 
					<xsl:value-of select="."/>    
				</xsl:attribute>
			</xsl:for-each>
			<xsl:choose>
				<xsl:when test="./@method='post' or ./@method='POST'">
					<xsl:attribute name="action">
						<xsl:value-of select="urlfactory:createProxyUrl( $urlfact, ./@action, 'POST', 'HTML', $response)" />
					</xsl:attribute>
				</xsl:when>
				<xsl:when test="./@method='get' or ./@method='GET' or ./@method='' or not(./@method)">
					<xsl:attribute name="action">
						<xsl:value-of select="urlfactory:createProxyUrl( $urlfact, ./@action, 'GET', 'HTML', $response)" />
					</xsl:attribute>
					<xsl:attribute name="method">
						<xsl:text>post</xsl:text>
					</xsl:attribute>
				</xsl:when>
			</xsl:choose>
			<xsl:apply-templates select="node()" />
		</xsl:copy>
	</xsl:template>
	
	<!-- Rewrite images -->
	<xsl:template match="//IMG/@src">
		<xsl:attribute name="src">
			<xsl:value-of select="urlfactory:createProxyUrl( $urlfact, ., 'GET', 'RAW', $response)" />
		</xsl:attribute>
	</xsl:template>
	
	<!-- Rewrite input src -->
	<xsl:template match="//INPUT/@src">
		<xsl:attribute name="src">
			<xsl:value-of select="urlfactory:createProxyUrl( $urlfact, ., 'GET', 'RAW', $response)" />
		</xsl:attribute>
	</xsl:template>
	
	<!-- Rewrite object data -->
	<xsl:template match="//OBJECT/@data">
		<xsl:attribute name="data">
			<xsl:value-of select="urlfactory:createProxyUrl( $urlfact, ., 'GET', 'RAW', $response)" />
		</xsl:attribute>
	</xsl:template>
	
	<!-- Rewrite embed src -->
	<xsl:template match="//EMBED/@src">
		<xsl:attribute name="src">
            <xsl:value-of select="urlfactory:createProxyUrl( $urlfact, ., 'GET', 'RAW', $response)" />
		</xsl:attribute>
	</xsl:template>
	
	<!-- Rewrite onload attributes to SCRIPT elements -->
	<xsl:template match="@onload">
		<SCRIPT>
			<xsl:attribute name="type">
				<xsl:text>text/javascript</xsl:text>
			</xsl:attribute>
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</SCRIPT>
	</xsl:template>
	
	<!-- Rewrite javascript attributes -->
	<xsl:template match="@onunload">
		<xsl:attribute name="onunload">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onclik">
		<xsl:attribute name="onclick">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onmousedown">
		<xsl:attribute name="onmousedown">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onmouseup">
		<xsl:attribute name="onmouseup">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onmouseover">
		<xsl:attribute name="onmouseover">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onmousemove">
		<xsl:attribute name="onmousemove">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onmouseout">
		<xsl:attribute name="onmouseout">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onfocus">
		<xsl:attribute name="onfocus">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onblur">
		<xsl:attribute name="onblur">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onkeypress">
		<xsl:attribute name="onkeypress">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onkeydown">
		<xsl:attribute name="onkeydown">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onkeyup">
		<xsl:attribute name="onkeyup">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onreset">
		<xsl:attribute name="onreset">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onchange">
		<xsl:attribute name="onchange">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@ondblclick">
		<xsl:attribute name="ondblclick">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onsubmit">
		<xsl:attribute name="onsubmit">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="@onselect">
		<xsl:attribute name="onselect">
			<xsl:value-of select="jstransformer:transform( $jstrans, .)" />
		</xsl:attribute>
	</xsl:template>

	<!-- Identity template -->
	<xsl:template match="@*|*">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
