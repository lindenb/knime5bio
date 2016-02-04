package com.github.lindenb.knime5bio.vcf.table2vcf;
import java.io.File;
import java.util.Optional;

import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFHeader;


public class TableToVcfNodeModel extends AbstractTableToVcfNodeModel {
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable inDataHeader,
    		final BufferedDataTable inDataBody,
    		final ExecutionContext exec) throws Exception
        {
     	VcfIterator in=null;
     	VariantContextWriter w=null;
     	BufferedDataContainer container=null;
 		final File outFile = super.createFileForWriting(Optional.empty(), ".vcf.gz");

     	try {
     		in = new KnimeVcfIterator(inDataHeader,inDataBody);
			final VCFHeader header = in.getHeader();
			outFile.getParentFile().mkdirs();
     		w = VCFUtils.createVariantContextWriter(outFile);
     		w.writeHeader(header);
     		while(in.hasNext())
     			{
     			w.add(in.next());
     			}
			CloserUtil.close(in);in=null;
			CloserUtil.close(w);w=null;

     		container = exec.createDataContainer(super.createOutTableSpec0());
     		container.addRowToTable(new DefaultRow(
     				RowKey.createRowKey(1L),
     				super.createDataCellsForOutTableSpec0(outFile.getPath())));
			container.close();
			final BufferedDataTable retTables[]=new BufferedDataTable[]{container.getTable()};
			container=null;
			return retTables;
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().error("Boum", e);
			CloserUtil.close(in);
			CloserUtil.close(w);
			outFile.delete();
			throw e;
		}
        }
}
