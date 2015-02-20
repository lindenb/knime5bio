<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0">
<xsl:output method="text"/>

<xsl:param name="base.dir">TMP</xsl:param>


<xsl:template match="/">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="generate-code">
<xsl:apply-templates select="node"/>
</xsl:template>

<xsl:template match="node[@type='vcf-filter']">
<xsl:variable name="xmlfile"><xsl:value-of select="concat($base.dir,'/code.',@name,'.xml')"/></xsl:variable>
<xsl:document href="{$xmlfile}" method="xml">
<code>
<xsl:comment>Code generated for <xsl:value-of select="@name"/></xsl:comment>
<xsl:apply-templates select="." mode="execute-vcf-filter"/>
</code>
</xsl:document>
</xsl:template>


<xsl:template match="node">
<xsl:message terminate="yes">undefined node type</xsl:message>
</xsl:template>

<xsl:template match="import">
<import>

</import>
</xsl:template>

<xsl:template match="import-htsjdk-vcf">
<import>
<xsl:text>
import htsjdk.variant.vcf.VCFHeader;
</xsl:text>
</import>
</xsl:template>


<xsl:template match="node[@type='vcf-filter']" mode="execute-vcf-filter">
<body>
	/* @inheritDoc */
	@Override
	protected BufferedDataTable[] execute(
			BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception
		{
		if(inData.length!=1)
			{
			throw new RuntimeException("Boum");
			}
		<xsl:value-of select="@handler-class"/> instance=null;
		
        org.knime.core.data.container.CloseableRowIterator iter=null;
        BufferedDataTable inTable=inData[0];
        DataTableSpec dataOutSpec = this.createOutTableSpec0();
        BufferedDataContainer out_container = null;
        int inUriIndex = this.findVcfInputRequiredColumnIndex(inTable.getDataTableSpec());

		try {
			instance= new <xsl:value-of select="@handler-class"/>();
			<xsl:apply-templates select="." mode="initialize"/>
			out_container = exec.createDataContainer(dataOutSpec);
            int nRows=0;
            double total=inTable.getRowCount();
            iter = inTable.iterator();
	        while(iter.hasNext())
	                {
	                DataRow row=iter.next();
	                ++nRows;
	                DataCell cell =row.getCell(inUriIndex);

		            if(cell.isMissing())
		            	{
		            	getLogger().warn("Missing cells in "+getNodeName());
		            	continue;
		            	}
		            if(!cell.getType().equals(StringCell.TYPE))
		            	{
		            	getLogger().error("not a StringCell type in "+cell);
		            	continue;
		            	}
	                String uri = StringCell.class.cast(cell).getStringValue();
	                if(uri.isEmpty())
	                	{
		            	getLogger().error("Empty uri");
		            	continue;
	                	}
	                /* create output file */
	                java.io.File fileout = new java.io.File(
	                		this.getKnime5BiNodeWorkingDirectory(),
	                		md5(uri)+".vcf.gz"
	                		);
					/* create parent directory if it doesn't exist */
	                if(fileout.getParentFile()!=null)
	                	{
	                	fileout.getParentFile().mkdirs();
	                	}
	                instance.setOutputFile(fileout);
	                
	            
	                if(instance.executeKnime(java.util.Collections.singletonList(uri))!=0)
	                	{
	                	fileout.delete();
	                	throw new RuntimeException("error during processing"+getNodeName()+" "+uri);
	                	}
	               
	                
	                out_container.addRowToTable(new DefaultRow(
	                	RowKey.createRowKey(nRows),
	                	createDataCellsForOutTableSpec0(
	                			fileout.getPath(),
	                			instance.getVariantCount()
	                			)
	                	));
	               
	                
	                exec.checkCanceled();
	                exec.setProgress(nRows/total);
	                }
	        	iter.close();
	        	iter=null;
	        	out_container.close();
	            BufferedDataTable out0 = out_container.getTable();
	            out_container=null;
	            return new BufferedDataTable[]{out0};
	            }
	        finally
	            {
	        	htsjdk.samtools.util.CloserUtil.close(iter);
	        	htsjdk.samtools.util.CloserUtil.close(out_container);
	            }
	        }
</body>
</xsl:template>

</xsl:stylesheet>
