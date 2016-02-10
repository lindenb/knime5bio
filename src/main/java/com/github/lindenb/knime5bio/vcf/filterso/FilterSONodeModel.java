package com.github.lindenb.knime5bio.vcf.filterso;
import java.io.File;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.misc.VcfFilterSequenceOntology;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;


public class FilterSONodeModel extends AbstractFilterSONodeModel {

	@Override
	protected void transform(File inFile, File outFile) throws Exception {
		final VcfFilterSequenceOntology application = new VcfFilterSequenceOntology();
		VcfIterator r=null;
		VariantContextWriter w=null;
		try {
			application.setInvert(super.getSettingsModelInvert().getBooleanValue());    		
			application.setDisableReasoning(super.getSettingsModelDisableReasoning().getBooleanValue());
			application.setUserTermsAsString( super.split(super.getSettingsModelUserTermsAsString().getStringValue()));
			if(super.getSettingsModelUserAcnFileFile().isPresent()){
				application.setUserAcnFile(super.getSettingsModelUserAcnFileFile().get());
			}
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
