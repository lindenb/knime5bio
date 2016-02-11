package com.github.lindenb.knime5bio.vcf.samplesin;
import java.io.File;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.LogRowIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.vcf.VCFHeader;


public class SamplesInNodeModel extends AbstractSamplesInNodeModel
	{
	@Override
    protected BufferedDataTable[] execute(final BufferedDataTable inData, 
    		final ExecutionContext exec) throws Exception
        {     	
		BufferedDataContainer container=null;
		LogRowIterator iter = null;
		final int vcfIndex = super.findColumnIndexByName(inData, super.__vcf);
		VcfIterator vcfIn = null;
		long rowIdx=0L;
     	try {     		
     		final DataTableSpec outspec = super.createOutTableSpec0();
     		container = exec.createDataContainer(outspec);
     		iter = new LogRowIterator(inData, exec);
     		while(iter.hasNext())
     			{
     			final DataRow row = iter.next();
     			final DataCell cell = row.getCell(vcfIndex);
     			if(cell.isMissing()) continue;
     			if(!(cell instanceof StringCell))
     				{
     				iter.close();
     				throw new RuntimeException("not a string cell");
     				}
     			final String cellStr = StringCell.class.cast(cell).getStringValue();
     			final File inVcf = new File(cellStr);
     			vcfIn = VCFUtils.createVcfIteratorFromFile(inVcf);
     			final VCFHeader header= vcfIn.getHeader();
     			vcfIn.close();vcfIn=null;
     			
     			if(header.getSampleNamesInOrder().isEmpty())
     				{
     				container.addRowToTable(
     						new DefaultRow(
     								RowKey.createRowKey(++rowIdx),
     								cell,
     								DataType.getMissingCell()
     								)
         					);
     				}
     			else for(final String sample:header.getSampleNamesInOrder())
     				{
     				container.addRowToTable(
     						new DefaultRow(
     								RowKey.createRowKey(++rowIdx),
     								cell,
     								new StringCell(sample)
     								)
         					);
     				}
     			}
     		
     		iter.close();iter=null;
     		container.close();
     		final BufferedDataTable table= container.getTable();
     		container=null;
     		return new BufferedDataTable[]{table};
		} catch (Exception e) {
			getLogger().error("boum", e);
			e.printStackTrace();
			throw e;
		} finally {
			CloserUtil.close(vcfIn);
			CloserUtil.close(iter);
			CloserUtil.close(container);
		}
        }
	}
