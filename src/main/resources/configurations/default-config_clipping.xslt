<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dyn="http://exslt.org/dynamic"
                xmlns:html="http://www.w3.org/1999/xhtml"
                extension-element-prefixes="dyn">

    <xsl:output method="html" encoding="UTF-8" indent="no" cdata-section-elements=""
                standalone="no" omit-xml-declaration="yes" />

    <xsl:template name="body-clipping">
	<xsl:choose>
		<xsl:when test="$type = 'none'">
			<xsl:apply-templates/>
		</xsl:when>
		
		<xsl:otherwise>
			<xsl:choose>
            		<xsl:when test="$type = 'xpath'">
            			<xsl:apply-templates select=" dyn:evaluate( $xpath )"/>
	            	</xsl:when>
	            	
	            	<xsl:otherwise>
        	    		<!-- insert code here for xslt clipping -->
        	    	</xsl:otherwise>
			</xsl:choose>
		</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="head-clipping">
    </xsl:template>

</xsl:stylesheet>