package com.github.lindenb.knime5bio.vcf.trios;
import java.io.File;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.vcftrios.VCFTrios;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;


public class TriosNodeModel extends AbstractTriosNodeModel {

	@Override
	protected void transform(File inFile, File outFile) throws Exception {
		final VCFTrios application = new VCFTrios();
		VcfIterator r=null;
		VariantContextWriter w=null;
		try {
			application.setPedigreeFile(new File(__pedigreeFile.getStringValue()));
			application.setCreate_filter(__create_filter.getBooleanValue());

			checkEmptyListOfThrowables(application.initializeKnime());
			r= VCFUtils.createVcfIteratorFromFile(inFile);
			w= VCFUtils.createVariantContextWriter(outFile);
			checkEmptyListOfThrowables(application.doVcfToVcf(inFile.getName(),r,w));
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
