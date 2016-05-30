package com.github.lindenb.knime5bio.vcf.burdenfisherv;
import java.io.File;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.burden.VcfBurdenFisherV;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;


public class BurdenFisherVNodeModel extends AbstractBurdenFisherVNodeModel {
	
	
	@Override
	protected void transform(File inFile, File outFile) throws Exception {
		final VcfBurdenFisherV application = new VcfBurdenFisherV();
		VcfIterator in=null;
		VariantContextWriter w=null;
		application.setAcceptFiltered(super.getSettingsModelAcceptFiltered().getBooleanValue());
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
