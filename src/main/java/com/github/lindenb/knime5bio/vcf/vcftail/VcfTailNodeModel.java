package com.github.lindenb.knime5bio.vcf.vcftail;
import java.io.File;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.misc.VcfHead;
import com.github.lindenb.jvarkit.tools.misc.VcfTail;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;


public class VcfTailNodeModel extends AbstractVcfTailNodeModel {
    @Override
   protected void transform(File inFile, File outFile) throws Exception {
		final VcfTail application = new VcfTail();
		application.setCount(super.__count.getIntValue());
		
		super.checkEmptyListOfThrowables(application.initializeKnime());
		VcfIterator r=null;
		VariantContextWriter w = null;
		try {
			r= VCFUtils.createVcfIteratorFromFile(inFile);
			w =  VCFUtils.createVariantContextWriter(outFile);
			application.doVcfToVcf(inFile.getName(),r,w);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally 
			{
			CloserUtil.close(r);
			CloserUtil.close(w);
			}
    	}
   
   @Override
   protected BufferedDataTable[] execute(BufferedDataTable inTable, ExecutionContext exec) throws Exception {
		return transform(inTable, __vcf, ".vcf.gz", exec);
    }

}
