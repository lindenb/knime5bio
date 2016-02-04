package com.github.lindenb.knime5bio.vcf.vcf2table;
import java.io.File;
import java.util.Set;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVariantContextWriter;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.vcf.VCFHeader;


public class VcfToTableNodeModel extends AbstractVcfToTableNodeModel {
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable inData, 
    		final ExecutionContext exec) throws Exception
        {
     	final Set<File> files= super.collectFilesInOneColumn(inData, super.getSettingsModelVcf());
     	if(files.size()!=1)
     		{
     		throw new RuntimeException("Expected ONE VCF file in table but got "+files.size());
     		}
     	final File vcfFile = files.iterator().next();
     	VcfIterator in=null;
     	KnimeVariantContextWriter w=null;
     	try {
     		
     		in = VCFUtils.createVcfIteratorFromFile(vcfFile);
			final VCFHeader header = in.getHeader();
     		w = new KnimeVariantContextWriter(exec);
     		w.writeHeader(header);
     		while(in.hasNext())
     			{
     			w.add(in.next());
     			}
			in.close();
			w.close();
			return w.getTables();
		} catch (Exception e) {
			CloserUtil.close(in);
			CloserUtil.close(w);
			throw e;
		}
        }
}
