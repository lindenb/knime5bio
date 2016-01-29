package com.github.lindenb.knime5bio.vcf.multi2oneallele;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.misc.VcfMultiToOneAllele;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVariantContextWriter;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVcfIterator;


public class Multi2OneAlleleNodeModel extends AbstractMulti2OneAlleleNodeModel {
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, 
    		final ExecutionContext exec) throws Exception
        {     	
		final VcfMultiToOneAllele application = new VcfMultiToOneAllele();
		
     	try {
    		application.setPrint_samples(super.isSettingsModelPrint_samplesBoolean());     		
    		checkEmptyListOfThrowables(application.initializeKnime());

     		final VcfIterator vcfIn = new KnimeVcfIterator( inData[0],inData[1] );
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
