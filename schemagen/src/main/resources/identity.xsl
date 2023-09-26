<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:xs="http://www.w3.org/2001/XMLSchema">
  <xsl:output
    method="xml"
    encoding="utf-8"
    indent="yes"
    suppress-indentation="xhtml:b xhtml:p" />
  <xsl:mode on-no-match="deep-copy" />
<!--
  <xsl:template match="/xs:schema">
    <xsl:apply-templates select="@*|xs:annotation"/>
    <xsl:apply-templates select="xs:element">
      <xsl:sort select="@name"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="xs:complexType">
      <xsl:sort select="@name"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="xs:simpleType[xs:restriction]">
      <xsl:sort select="@name"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="not(@*|xs:annotation|xs:element|xs:complexType|xs:simpleType[xs:restriction])"/>
  </xsl:template>
-->
</xsl:stylesheet>