<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dyn="http://exslt.org/dynamic" extension-element-prefixes="dyn">

	<xsl:output method="html" encoding="UTF-8" indent="yes"
		standalone="no" omit-xml-declaration="yes" />
		
	<xsl:param name="type" />
	<xsl:param name="xpath" />
	
	<xsl:template match="/HTML">
		<HTML>
			<xsl:apply-templates select="/HTML/HEAD" />
			<xsl:apply-templates select="/HTML/BODY" />
		</HTML>
	</xsl:template>

	<xsl:template match="/HTML/HEAD">
		<xsl:copy-of select="."/>
	</xsl:template>

	<xsl:template match="/HTML/BODY">
        <xsl:copy>
			<xsl:for-each select="./@*">
				<xsl:attribute name="{name()}"> 
					<xsl:value-of select="."/>    
				</xsl:attribute>
			</xsl:for-each>
        </xsl:copy>
		<xsl:apply-templates select="//STYLE" />
		<xsl:choose>
			<xsl:when test="$type = 'xpath'">
				<xsl:copy-of select=" dyn:evaluate( $xpath )"/>
			</xsl:when>
			<xsl:otherwise>
			<!-- Insert code here for xslt clipping -->
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="//STYLE">
		<xsl:copy-of select="."/>
	</xsl:template>
	
</xsl:stylesheet>