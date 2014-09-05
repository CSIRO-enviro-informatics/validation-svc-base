<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:sch="http://www.ascc.net/xml/schematron"
    xmlns:iso="http://purl.oclc.org/dsdl/schematron"
    xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
    version="1.0">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>




    <xsl:template match="svrl:fired-rule"/>


    <xsl:template match="@test"/>

    <xsl:template match="@location"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>




</xsl:stylesheet>

