<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:param name="package"/>
<xsl:param name="nodeName"/>
<xsl:param name="abstract">false</xsl:param>

<xsl:variable name="modelName">
	<xsl:value-of select="$nodeName"/>
	<xsl:text>NodeModel</xsl:text>
</xsl:variable>

<xsl:variable name="license">The MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
  
The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
  
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.</xsl:variable>

<xsl:variable name="common-imports">
import java.util.List;
import java.util.Set;
import javax.annotation.Generated;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.data.DataTableSpec;
</xsl:variable>


<xsl:template match="setting" mode="config-name">
<xsl:value-of select="concat('SETTING_',@name)"/>
</xsl:template>



<xsl:template match="setting" mode="config-default">
<xsl:value-of select="concat('DEFAULT_',@name)"/>
</xsl:template>


<xsl:template match="*" mode="label">
<xsl:choose>
	<xsl:when test="label"><xsl:value-of select="label"/></xsl:when>
	<xsl:when test="@label"><xsl:value-of select="@label"/></xsl:when>
	<xsl:when test="name(.) = 'node'"><xsl:value-of select="$nodeName"/></xsl:when>
	<xsl:otherwise><xsl:value-of select="@name"/></xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="*" mode="description">
<xsl:choose>
	<xsl:when test="description"><xsl:value-of select="description"/></xsl:when>
	<xsl:when test="@description"><xsl:value-of select="@description"/></xsl:when>
	<xsl:otherwise><xsl:apply-templates select="." mode="label"/></xsl:otherwise>
</xsl:choose>
</xsl:template>




<xsl:template name="escape">
    <xsl:param name="string"/>
    <xsl:variable name="newline" select="'&#10;'" /> <!--  http://stackoverflow.com/questions/1492736  -->
    
    <xsl:call-template name="java-string-replace">
      <xsl:with-param name="from">"</xsl:with-param>
      <xsl:with-param name="to">\"</xsl:with-param>
      <xsl:with-param name="string">
        <xsl:call-template name="java-string-replace">
          <xsl:with-param name="from" select="$newline"/>
          <xsl:with-param name="to">\n</xsl:with-param>
          <xsl:with-param name="string">
            <xsl:call-template name="java-string-replace">
              <xsl:with-param name="from">\</xsl:with-param>
              <xsl:with-param name="to">\\</xsl:with-param>
              <xsl:with-param name="string" select="$string"/>
            </xsl:call-template>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:call-template>
</xsl:template>




<xsl:template name="java-string-replace">
    <xsl:param name="string"/>
    <xsl:param name="from"/>
    <xsl:param name="to"/>
    
    <xsl:if test="string-length($from)=0"><xsl:message terminate="yes">BOUM:'<xsl:value-of select="$from"/>' vs '<xsl:value-of select="$to"/>
'</xsl:message></xsl:if>
    
    <xsl:choose>
      <xsl:when test="contains($string,$from)">
        <xsl:value-of select="substring-before($string,$from)"/>
        <xsl:value-of select="$to"/>
        <xsl:call-template name="java-string-replace">
          <xsl:with-param name="string" select="substring-after($string,$from)"/>
          <xsl:with-param name="from" select="$from"/>
          <xsl:with-param name="to" select="$to"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



<xsl:template name="titleize">
 <xsl:param name="name"/>
 <xsl:value-of select="translate(substring($name,1,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
 <xsl:value-of select="substring($name,2)"/>
</xsl:template>



</xsl:stylesheet>
