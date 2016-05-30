package com.github.lindenb.knime5bio.vcf.injectpedigree;
import java.io.File;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.burden.VcfInjectPedigree;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;


public class InjectPedigreeNodeModel extends AbstractInjectPedigreeNodeModel {
	
	
	@Override
	protected void transform(File inFile, File outFile) throws Exception {
		final VcfInjectPedigree application = new VcfInjectPedigree();
		VcfIterator in=null;
		VariantContextWriter w=null;
		application.setPedigreeFile(super.getSettingsModelPedigreeFileFile());
		application.setCleanPreviousPedigree(super.isSettingsModelCleanPreviousPedigreeBoolean());
		application.setIgnoreMissingInHeader(super.isSettingsModelIgnoreMissingInHeaderBoolean());
		application.setIgnoreMissingInPedigree(super.isSettingsModelIgnoreMissingInPedigreeBoolean());
		application.setIgnoreMissingInPedigree(super.isSettingsModelIgnorePedigreeValidationBoolean());
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
