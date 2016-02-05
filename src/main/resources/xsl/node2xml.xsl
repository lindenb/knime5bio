<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	 xmlns="http://knime.org/node/v2.8"
	version="1.0"
	exclude-result-prefixes="xsl"
	>
<xsl:import href="mod.node.xsl"/>
<xsl:output method="xml" indent="yes" encoding="UTF-8" />

<xsl:template match="/">
<xsl:apply-templates select="node"/>
</xsl:template>

<xsl:template match="node">



<knimeNode icon="./default.png" type="Source"  xmlns="http://knime.org/node/v2.8"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://knime.org/nod
e/v2.10 http://knime.org/node/v2.10.xsd">
<xsl:comment><xsl:value-of select="$license"/> </xsl:comment>
    <name><xsl:value-of select="$nodeName"/></name>
    
    <shortDescription>
        <xsl:apply-templates select="." mode="description"/>
       
    </shortDescription>
    
    <fullDescription>
        <intro>
        <xsl:apply-templates select="." mode="description"/><br/>
         <xsl:apply-templates select="documentation"/>
        
        </intro>
        
        <xsl:for-each select="settings/setting">
        	<option>
        		<xsl:attribute name="name">
        			<xsl:apply-templates select="." mode="label"/>
        		</xsl:attribute>
        		<xsl:apply-templates select="." mode="description"/>
        	</option>
    	</xsl:for-each>
    </fullDescription>
    
    
    <ports>
    	<xsl:for-each select="ports/inPort">
    		<inPort>
    		  <xsl:attribute name="index"><xsl:value-of select="position() -1"/></xsl:attribute>
        		<xsl:attribute name="name">
        			<xsl:apply-templates select="." mode="label"/>
        		</xsl:attribute>
        		<xsl:apply-templates select="." mode="description"/>
    		</inPort>
    	</xsl:for-each>
    	<xsl:for-each select="ports/outPort">
    		<outPort>
    		  <xsl:attribute name="index"><xsl:value-of select="position() -1"/></xsl:attribute>
        		<xsl:attribute name="name">
        			<xsl:apply-templates select="." mode="label"/>
        		</xsl:attribute>
        		<xsl:apply-templates select="." mode="description"/>
    		</outPort>
    	</xsl:for-each>
    </ports>  
      <views>  
      </views>
   <!-- 
  
        <view index="0" name="name of first view">Description of first view...</view>
       view index="1" name="name of second view">Description of second view...</view>
    
    -->
</knimeNode>
</xsl:template>

<xsl:template match="documentation">
<xsl:apply-templates select="p|h|br|b|i|u|tt|a|url|ul|ol|li|h2|h3|h4|pre|sub|table|tr|td|img|text()"/>
<h3>Author</h3>
Pierre Lindenbaum PhD <a href="http://twitter.com/yokofakun">@yokofakun</a> Institut du Thorax, U1087, 44000 Nantes.<p/>
<h3>Source Code</h3>
<xsl:variable name="src" select="concat('https://github.com/lindenb/knime5bio/blob/master/src/main/java/',translate($package,'.','/'),'/',$nodeName,'NodeModel.java')"/>
<a>
<xsl:attribute name="href"><xsl:value-of select="$src"/></xsl:attribute>
<xsl:value-of select="$src"/>
</a><p/>
<h3>Report bugs / Contribute</h3>
<a href="https://github.com/lindenb/knime5bio/issues">https://github.com/lindenb/knime5bio/issues</a><p/>
<h3>License</h3>
The project is licensed under the MIT license.<p/>
</xsl:template>

<xsl:template match="p|h|br|b|i|u|tt|ul|ol|h2|h3|h4|pre|sub|table|tr|td|li">
<xsl:variable name="tag" select="name()"/>
<xsl:element name="{$tag}">
<xsl:apply-templates select="p|h|br|b|i|u|tt|a|url|ul|ol|li|h2|h3|h4|pre|sub|table|tr|td|img|text()"/>
</xsl:element>
</xsl:template>

<xsl:template match="a|url">
<a>
<xsl:choose>
<xsl:when test="@href"><xsl:value-of select="@href"/></xsl:when>
<xsl:otherwise><xsl:apply-templates/></xsl:otherwise> 
</xsl:choose>
</a>
</xsl:template>

<xsl:template match="img">
<img>
<xsl:attribute name="src">
<xsl:choose>
<xsl:when test="@src"><xsl:value-of select="@src"/></xsl:when>
<xsl:otherwise><xsl:apply-templates/></xsl:otherwise> 
</xsl:choose>
</xsl:attribute>
</img>
</xsl:template>

</xsl:stylesheet>
