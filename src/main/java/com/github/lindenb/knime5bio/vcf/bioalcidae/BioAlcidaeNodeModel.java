package com.github.lindenb.knime5bio.vcf.bioalcidae;

import java.io.File;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import com.github.lindenb.jvarkit.tools.bioalcidae.BioAlcidae;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;


public class BioAlcidaeNodeModel extends AbstractBioAlcidaeNodeModel {
     BioAlcidaeNodeModel() {
     }
     
     @Override
    protected void transform(final  File inFile, final File outFile) throws Exception {
 		final BioAlcidae application = new BioAlcidae();
    	application.setFormatString("VCF");
		application.setOutputFile(outFile);
		
		final String javascriptExpr = super.getSettingsModelScriptExpr().getStringValue().trim();
		if(!javascriptExpr.isEmpty())
			{
			application.setJavascriptExpr(javascriptExpr);
			}
		if(super.getSettingsModelJavascriptFileFile().isPresent())
			{
			application.setJavascriptFile(super.getSettingsModelJavascriptFileFile().get());
			}
		super.checkEmptyListOfThrowables(application.initializeKnime());
		VcfIterator r=null;
		try {
			application.initializeJavaScript();
			r= VCFUtils.createVcfIteratorFromFile(inFile);
			this.checkEmptyListOfThrowables(application.executeAsVcf(r));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally 
			{
			application.disposeKnime();
			CloserUtil.close(r);
			}
     	}
    
    @Override
    protected BufferedDataTable[] execute(BufferedDataTable inTable, ExecutionContext exec) throws Exception {
		String suffix = super.__extension.getStringValue().trim();
		if(suffix.isEmpty()) suffix=".txt";
		if(!suffix.startsWith(".")) suffix="."+suffix;
		return transform(inTable, __vcf, suffix, exec);
     }
}
