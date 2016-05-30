package com.github.lindenb.knime5bio.vcf.burdenexac;
import java.io.File;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.burden.VcfBurdenFilterExac;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;


public class BurdenExacNodeModel extends AbstractBurdenExacNodeModel {
	
	
	@Override
	protected void transform(final File inFile, final File outFile) throws Exception {
		final VcfBurdenFilterExac application = new VcfBurdenFilterExac();
		VcfIterator in=null;
		VariantContextWriter w=null;
		application.setExacFile(super.getSettingsModelExacFileFile());
		application.setExacPopulationStr(super.getSettingsModelExacPopulationStrString());
		application.setIfNotInExacThenDiscard(super.getSettingsModelIfNotInExacThenDiscard().getBooleanValue());
		application.setMaxFreq(super.getSettingsModelMaxFreq().getDoubleValue());
		application.setUseTabixIndex(super.getSettingsModelUseTabixIndex().getBooleanValue());
		
		
		application.setOutputFile(outFile);
		
		checkEmptyListOfThrowables(application.initializeKnime());

		try
		{
			in = VCFUtils.createVcfIteratorFromFile(inFile);
			w =  VCFUtils.createVariantContextWriter(outFile);
     		checkEmptyListOfThrowables(application.doVcfToVcf(inFile.getName(),in,w));

		}
		finally
		{
			CloserUtil.close(in);
			CloserUtil.close(w);
			application.disposeKnime();
		}
	}

	@Override
	protected BufferedDataTable[] execute(BufferedDataTable inData0, ExecutionContext exec) throws Exception {
		return transform(inData0, __vcf, ".vcf.gz", exec);
		}
	
}
