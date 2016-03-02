package com.github.lindenb.knime5bio.vcf.burdenf2;
import java.io.File;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.burden.VcfBurdenFilter2;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;


public class BurdenF2NodeModel extends AbstractBurdenF2NodeModel {
	
	
	@Override
	protected void transform(final File inFile, final File outFile) throws Exception {
		final VcfBurdenFilter2 application = new VcfBurdenFilter2();
		application.setCasesFile(super.getSettingsModelCasesFileFile());
		application.setControlsFile(super.getSettingsModelControlsFileFile());
		application.setMaxMAF(super.getSettingsModelMaxMAF().getDoubleValue());
		application.setMinFisherPValue(super.getSettingsModelMinFisherPValue().getDoubleValue());
		
		
		VcfIterator in=null;
		VariantContextWriter w=null;
		
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
