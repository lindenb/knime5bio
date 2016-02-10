package com.github.lindenb.knime5bio.vcf.cutsamples;
import java.io.File;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.misc.VcfCutSamples;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;


public class CutSamplesNodeModel extends AbstractCutSamplesNodeModel {

	@Override
	protected void transform(File inFile, File outFile) throws Exception {
		final VcfCutSamples application = new VcfCutSamples();
		VcfIterator r=null;
		VariantContextWriter w=null;
		try {
			application.setInvert(super.getSettingsModelInvert().getBooleanValue());   
			application.setMissingSampleIsError(super.__unknownIsFatal.getBooleanValue());
			application.setRemoveCtxIfNoCall(super.__removeUncalled.getBooleanValue());
			for(final String sample: super.__samples.getStringValue().split("[\n ,;]+"))
				{
				if(sample.isEmpty()) continue;
				application.getUserSamples().add(sample);
				}

			if(application.initializeKnime()!=0) throw new RuntimeException("cannot init");
			r= VCFUtils.createVcfIteratorFromFile(inFile);
			w= VCFUtils.createVariantContextWriter(outFile);
			application.doWork(inFile.getName(),r,w);
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
