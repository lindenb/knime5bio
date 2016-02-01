package com.github.lindenb.knime5bio.vcf.sortingindex;
import java.io.File;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomainCreator;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;


import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;
import htsjdk.samtools.util.CloserUtil;


public class SortingIndexNodeModel extends AbstractSortingIndexNodeModel {
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, 
    		final ExecutionContext exec) throws Exception
        {     	
	 	ReferenceSequenceFile reference=null;
		BufferedDataContainer container=null;
		CloseableRowIterator iter = null;
		final int chrom_index = super.findColumnIndexByName(inData[0], super.__CHROM);
		final int pos_index = super.findColumnIndexByName(inData[0], super.__CHROM);
     	try {
     		final File fasta = super.getSettingsModelReferenceGenomeFile();
     		reference = ReferenceSequenceFileFactory.getReferenceSequenceFile(fasta);
     		if(reference==null) throw new InvalidSettingsException("Cannot get sequence "+fasta);
     		final SAMSequenceDictionary dict = reference.getSequenceDictionary();
     		if(dict==null) throw new InvalidSettingsException("Cannot get dictionary of "+fasta);
     		
     		
     		final DataTableSpec spec0 = inData[0].getSpec();
     		final DataTableSpec outspec = new DataTableSpec(
     				spec0,
     				new DataTableSpec(new DataColumnSpecCreator("REFINDEX", LongCell.TYPE).createSpec())
     				);
     		
     		container = exec.createDataContainer(outspec);
     		iter = inData[0].iterator();
     		while(iter.hasNext())
     			{
     			final DataRow row = iter.next();
     			final DataCell c_chrom = row.getCell(chrom_index);
     			final DataCell c_pos = row.getCell(pos_index);
     			Long sortingIndex=null;
     			if(!(c_chrom.isMissing() || c_pos.isMissing()))
     				{
     				String chromname= StringCell.class.cast(c_chrom).getStringValue().trim();
     				int chromStart = IntCell.class.cast(c_pos).getIntValue();
     				int tid = dict.getSequenceIndex(chromname);
     				if(tid!=-1 ) {
     					sortingIndex = 0L;
     					for(int i=0;i< tid;++i)
     						{
     						sortingIndex+= dict.getSequence(i).getSequenceLength();
     						}
     					sortingIndex+= chromStart;
     					}
     				}
     			container.addRowToTable( new AppendedColumnRow(row,
     					sortingIndex==null?
	     					DataType.getMissingCell():
	     					new LongCell(sortingIndex)
     					)
     					);
     			
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
			CloserUtil.close(reference);
			CloserUtil.close(iter);
			CloserUtil.close(container);
		}
        }
}
