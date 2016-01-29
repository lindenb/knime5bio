<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:import href="mod.node.xsl"/>
<xsl:output method="text" />

<xsl:template match="/">
<xsl:apply-templates select="node"/>
</xsl:template>


<xsl:template match="node">/* <xsl:value-of select="$license"/> */
package <xsl:value-of select="$package"/>;
<xsl:value-of select="$common-imports"/>


@Generated("xsl")
public class <xsl:value-of select="concat($nodeName,'NodeFactory')"/> extends
	 com.github.lindenb.knime5bio.AbstractNodeFactory&lt;<xsl:value-of select="$modelName"/>&gt;
	{
	public <xsl:value-of select="concat($nodeName,'NodeFactory')"/>() {
		}
	
	
	@Override
	protected NodeDialogPane createNodeDialogPane() {
		return new  <xsl:value-of select="concat($nodeName,'NodeDialog')"/>();
	}

	@Override
	public <xsl:value-of select="$modelName"/> createNodeModel() {
		return new  <xsl:value-of select="$modelName"/>();
	}

	@Override
	public NodeView&lt;<xsl:value-of select="$modelName"/>&gt; createNodeView(int arg0, <xsl:value-of select="$modelName"/> arg1) {
		 throw new IllegalStateException("No view");
	}

	@Override
	protected int getNrNodeViews() {
		return 0;
	}

	@Override
	protected boolean hasDialog() {
		return true;
	}
	
	
	}
</xsl:template>


</xsl:stylesheet>
