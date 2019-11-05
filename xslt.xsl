<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="yes" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="entry[field=.]">
        <entry field="{field}">
            <xsl:apply-templates select="@*|node()"/>
        </entry>
    </xsl:template>

    <xsl:template match="field"/>
</xsl:stylesheet>