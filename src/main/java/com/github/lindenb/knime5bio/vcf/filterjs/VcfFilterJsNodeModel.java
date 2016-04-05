package com.github.lindenb.knime5bio.vcf.filterjs;
import java.io.File;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.vcffilterjs.VCFFilterJS;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;


public class VcfFilterJsNodeModel extends AbstractVcfFilterJsNodeModel {
	
	@Override
	protected void transform(final File inFile, final File outFile) throws Exception {
		final VCFFilterJS application = new VCFFilterJS();

		VcfIterator r=null;
		VariantContextWriter w=null;
		try {
    		application.setJavascriptExpr(super.getSettingsModelVcfExprString());
    		application.setFilteredTag(__filteredTag.getStringValue());
    		checkEmptyListOfThrowables(application.initializeKnime());
    		r= VCFUtils.createVcfIteratorFromFile(inFile);
     		w= VCFUtils.createVariantContextWriter(outFile);
     		checkEmptyListOfThrowables(application.doVcfToVcf(inFile.getName(),r,w));
		}
		catch(final Exception err) 
			{
			err.printStackTrace();
			throw err;
			}
		finally
		{
			CloserUtil.close(r);
			CloserUtil.close(w);
			application.disposeKnime();
		}
		}

	
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable inTable, final ExecutionContext exec) throws Exception {
		return transform(inTable, __vcf, ".vcf.gz", exec);
	}
}
