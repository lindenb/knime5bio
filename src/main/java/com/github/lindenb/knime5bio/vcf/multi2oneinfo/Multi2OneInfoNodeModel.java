package com.github.lindenb.knime5bio.vcf.multi2oneinfo;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.misc.VcfMultiToOneInfo;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVariantContextWriter;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVcfIterator;


public class Multi2OneInfoNodeModel extends AbstractMulti2OneInfoNodeModel {
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, 
    		final ExecutionContext exec) throws Exception
        {     	
		final VcfMultiToOneInfo application = new VcfMultiToOneInfo();
		
     	try {
     		application.setInfoTag(super.getSettingsModelInfoTagString());
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
