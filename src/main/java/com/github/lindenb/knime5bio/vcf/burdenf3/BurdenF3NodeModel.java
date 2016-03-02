package com.github.lindenb.knime5bio.vcf.burdenf3;
import java.io.File;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.burden.VcfBurdenFilter3;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;


public class BurdenF3NodeModel extends AbstractBurdenF3NodeModel {
	
	
	@Override
	protected void transform(File inFile, File outFile) throws Exception {
		final VcfBurdenFilter3 application = new VcfBurdenFilter3();
		VcfIterator in=null;
		VariantContextWriter w=null;
		application.setExacFile(super.getSettingsModelExacFileFile());
		application.setExacPopulationStr(super.getSettingsModelExacPopulationStr().toString().replaceAll("[\n ]", ","));
		application.setIfNotInExacThenDiscard(super.getSettingsModelIfNotInExacThenDiscard().getBooleanValue());
		application.setOutputFile(outFile);
		application.setMaxFreq(super.geSettingsModelMaxFreqDouble());
		
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
