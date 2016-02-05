<?xml version='1.0' ?>
<xsl:stylesheet
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'
	>
<xsl:output method="xml" indent="no" />


<xsl:template match="/">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="@*">
<xsl:copy select="."/>
</xsl:template>

<xsl:template match="node">
<node >
	<xsl:apply-templates select="@*"/>
	<xsl:apply-templates select="*|text()"/>
	<xsl:if test='not(documentation)'>
	<documentation></documentation>
	</xsl:if>
</node>
</xsl:template>

<xsl:template match="setting[@type='gatk.jar']">
<setting name="gatkpath" label="path to gatk" type="input-file" histoty-id="gatk.jar" required="true" default="GenomeAnalysisTK.jar">
	<description>GATK jar ( GenomeAnalysisTK.jar )</description>
	<suffix>.jar</suffix>
</setting>
<setting type="gatk.capture.rod"/>
<setting type="reference.fasta"/>
</xsl:template>

<xsl:template match="setting[@type='reference.fasta']">
<setting name="referenceGenome" label="path to REF" type="input-file" histoty-id="ref.faidx" required="true" >
	<description>Reference Genome. Indexed with Tabix (*.fai) and with Picard (*.dict)</description>
	<suffixes id="fasta"/>
</setting>
</xsl:template>


<xsl:template match="setting[@type='gatk.capture.rod']">
<setting name="captureBed" label="Capture for GATK" type="input-file" histoty-id="capture.rod" required="false" >
	<description>Capture for GATK</description>
	<suffixes id="rod"/>
</setting>
</xsl:template>

<xsl:template match="description[@id='column.vcf.files']">
<description>A column containing the path to some VCF files.</description>
</xsl:template>


<xsl:template match="suffixes[@id='fasta']">
	<suffix>.fa</suffix>
	<suffix>.fasta</suffix>
	<suffix>.fa.gz</suffix>
	<suffix>.fasta.gz</suffix>
</xsl:template>

<xsl:template match="suffixes[@id='rod']">
	<suffix>.bed</suffix>
	<suffix>.bed.gz</suffix>
</xsl:template>

<xsl:template match="outPort[@id='vcf']|inPort[@id='vcf']">
	<xsl:variable name="name"><xsl:value-of select="name(.)"/></xsl:variable>
	<xsl:variable name="index">
		<xsl:choose>
			<xsl:when test="@index"><xsl:value-of select="number(@index)"/></xsl:when>
			<xsl:otherwise><xsl:value-of select="number(0)"/></xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:comment>BEGIN VCF OUPUT </xsl:comment>
	<xsl:element name="{$name}">
		<xsl:attribute name="label">VCF Table Header</xsl:attribute>
		<xsl:attribute name="index"><xsl:value-of select="number($index)"/></xsl:attribute>
		<xsl:attribute name="at-runtime">true</xsl:attribute>
	</xsl:element>
	
	<xsl:element name="{$name}">
		<xsl:attribute name="label">VCF Table Body</xsl:attribute>
		<xsl:attribute name="index"><xsl:value-of select="number($index) + 1"/></xsl:attribute>
		<xsl:attribute name="at-runtime">true</xsl:attribute>
	</xsl:element>
	<xsl:comment>END VCF OUPUT </xsl:comment>
	
</xsl:template>


<xsl:template match="*">
<xsl:copy select=".">
<xsl:apply-templates select="@*"/>
<xsl:apply-templates select="*|text()"/>
</xsl:copy>
</xsl:template>






</xsl:stylesheet>
