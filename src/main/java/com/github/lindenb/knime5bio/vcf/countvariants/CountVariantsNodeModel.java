package com.github.lindenb.knime5bio.vcf.countvariants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.LogRowIterator;

import htsjdk.samtools.util.CloserUtil;

public class CountVariantsNodeModel extends AbstractCountVariantsNodeModel {
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable inData, final ExecutionContext exec) throws Exception
        {
    	final int vcfColumn = super.findColumnIndexByName(inData,super.__VCF);
    	BufferedDataContainer container0 = null;
        LogRowIterator iter =null;
        BufferedReader in=null;
        VcfIterator vcfIn=null;
        try
            {
			container0 = exec.createDataContainer(super.createOutTableSpec0());

        	
        	iter =  new LogRowIterator("Count",inData, exec);
        	while(iter.hasNext())
        		{
				final DataRow row = iter.next();
				final DataCell cell = row.getCell(vcfColumn);
				if(!(cell instanceof StringCell))
					throw new InvalidSettingsException("not a string cell");
				if (cell.isMissing())
					continue;
				final File inFile = new File(StringCell.class.cast(cell).getStringValue());
				if (!inFile.exists())
					throw new FileNotFoundException("cannot find " + inFile);
				if (!inFile.isFile())
					throw new FileNotFoundException("not a file: " + inFile);
				vcfIn = VCFUtils.createVcfIteratorFromFile(inFile);
				int count=0;
				while(vcfIn.hasNext())
					{
					vcfIn.next();
					count++;
					}
				vcfIn.close();vcfIn=null;
				container0.addRowToTable(new DefaultRow(
						row.getKey(),
						cell,
						new IntCell(count)
						));
        		}
        	iter.close();iter=null;

			container0.close();
            BufferedDataTable out0 = container0.getTable();
            container0=null;
            return new BufferedDataTable[]{out0};
            }
        finally
            {
          	CloserUtil.close(vcfIn);
          	CloserUtil.close(in);
          	CloserUtil.close(iter);
          	CloserUtil.close(container0);
            }
        }

}
