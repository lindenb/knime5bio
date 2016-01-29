<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
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
        <intro><xsl:value-of select="$nodeName"/></intro>
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


</xsl:stylesheet>