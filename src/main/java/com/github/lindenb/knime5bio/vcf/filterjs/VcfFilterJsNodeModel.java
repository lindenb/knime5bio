package com.github.lindenb.knime5bio.vcf.filterjs;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.vcffilterjs.VCFFilterJS;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVariantContextWriter;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVcfIterator;


public class VcfFilterJsNodeModel extends AbstractVcfFilterJsNodeModel {
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable headerData,BufferedDataTable bodyData, 
    		final ExecutionContext exec) throws Exception
        {     	
		final VCFFilterJS application = new VCFFilterJS();
		
     	try {
    		application.setJavascriptExpr(super.getSettingsModelVcfExprString());     		
    		checkEmptyListOfThrowables(application.initializeKnime());

     		final VcfIterator vcfIn = new KnimeVcfIterator(
     				headerData,bodyData
     				);
     		final KnimeVariantContextWriter vcfOut = new KnimeVariantContextWriter(exec);
     		checkEmptyListOfThrowables(application.doVcfToVcf(this.getNodeName(),vcfIn,vcfOut));
     		
			vcfIn.close();
			vcfOut.close();
            return vcfOut.getTables();
		} catch (Exception e) {
			getLogger().error("boum", e);
			e.printStackTrace();
			throw e;
		} finally {
			application.disposeKnime();
		}
        }
}
