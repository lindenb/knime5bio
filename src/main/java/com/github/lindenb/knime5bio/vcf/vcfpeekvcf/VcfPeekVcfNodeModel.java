package com.github.lindenb.knime5bio.vcf.vcfpeekvcf;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.vcfvcf.VcfPeekVcf;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVariantContextWriter;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVcfIterator;


public class VcfPeekVcfNodeModel extends AbstractVcfPeekVcfNodeModel {
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable inDataHeader,
    		final BufferedDataTable inDataBody, 
    		final ExecutionContext exec) throws Exception
        {     	
		final VcfPeekVcf application = new VcfPeekVcf();
		
     	try {
    		application.setAltAlleleCheck(super.isSettingsModelAltAlleleCheckBoolean());
    		application.setPeekId(super.isSettingsModelPeekIdBoolean());
    		application.setPeekTagPrefix(super.getSettingsModelPeekTagPrefixString());
    		application.setTABIX(super.getSettingsModelTABIX().getStringValue());
    		application.setTagsAsString(super.getSettingsModelTagsAsStringString());
    		checkEmptyListOfThrowables(application.initializeKnime());

     		final VcfIterator vcfIn = new KnimeVcfIterator(inDataHeader,inDataBody);
     	
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
