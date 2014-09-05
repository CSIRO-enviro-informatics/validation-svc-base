<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:sch="http://www.ascc.net/xml/schematron"
                xmlns:iso="http://purl.oclc.org/dsdl/schematron"
                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
				version="1.0">
				<xsl:output method="text" indent="no"/>

				<xsl:template match="/">
                    <xsl:apply-templates/>
                </xsl:template>


<xsl:template match="svrl:successful-report" >
<xsl:choose>

<xsl:when test="@flag = 'fatal'">
 FATAL: <xsl:value-of select="normalize-space(svrl:text)"/>
<xsl:for-each select="svrl:diagnostic-reference">
   <xsl:value-of select="."/>
</xsl:for-each>
<xsl:text>
</xsl:text>
</xsl:when>

<xsl:when test="@flag = 'error'">
ERROR: <xsl:value-of select="normalize-space(svrl:text)"/>
<xsl:for-each select="svrl:diagnostic-reference">
   <xsl:value-of select="."/>
</xsl:for-each>
<xsl:text>
</xsl:text>
</xsl:when>

<xsl:when test="@flag = 'warning'">
WARNING: <xsl:value-of select="normalize-space(svrl:text)"/>
<xsl:for-each select="svrl:diagnostic-reference">
   <xsl:value-of select="."/>
</xsl:for-each>
<xsl:text>
</xsl:text>
</xsl:when>

<xsl:otherwise>
INFO: <xsl:value-of select="normalize-space(svrl:text)"/>
<xsl:for-each select="svrl:diagnostic-reference">
   <xsl:value-of select="."/>
</xsl:for-each>
<xsl:text>
</xsl:text>
</xsl:otherwise>
</xsl:choose>



</xsl:template>

<xsl:template match="svrl:failed-assert">
<xsl:choose>

<xsl:when test="@flag = 'fatal'">
FATAL: <xsl:value-of select="normalize-space(svrl:text)"/>
<xsl:for-each select="svrl:diagnostic-reference">
   <xsl:value-of select="."/>
</xsl:for-each>
<xsl:text>
</xsl:text>
</xsl:when>

<xsl:when test="@flag = 'error'">
ERROR: <xsl:value-of select="normalize-space(svrl:text)"/>
<xsl:for-each select="svrl:diagnostic-reference">
   <xsl:value-of select="."/>
</xsl:for-each>
<xsl:text>
</xsl:text>
</xsl:when>

<xsl:when test="@flag = 'warning'">
WARNING: <xsl:value-of select="normalize-space(svrl:text)"/>
<xsl:for-each select="svrl:diagnostic-reference">
   <xsl:value-of select="."/>
</xsl:for-each>
<xsl:text>
</xsl:text>
</xsl:when>

<xsl:otherwise>
INFO: <xsl:value-of select="normalize-space(svrl:text)"/>
<xsl:for-each select="svrl:diagnostic-reference">
   <xsl:value-of select="."/>
</xsl:for-each>
<xsl:text>
</xsl:text>
</xsl:otherwise>
</xsl:choose>



</xsl:template>




<xsl:template match="svrl:text">
<xsl:text></xsl:text>
</xsl:template>



<xsl:template match="text()">
<xsl:text></xsl:text>
</xsl:template>



</xsl:stylesheet>

