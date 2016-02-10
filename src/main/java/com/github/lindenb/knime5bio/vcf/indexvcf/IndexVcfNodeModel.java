package com.github.lindenb.knime5bio.vcf.indexvcf;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import com.github.lindenb.jvarkit.tools.misc.VcfIndexTabix;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;



public class IndexVcfNodeModel extends AbstractIndexVcfNodeModel {
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable inTable, final ExecutionContext exec) throws Exception
        {
		final int vcfColumn = super.findColumnIndexByName(inTable,super.getSettingsModelVcf());
		this.assureNodeWorkingDirectoryExists();
		CloseableRowIterator iter=null;
		try {
	    	final DataTableSpec spec0 = createOutTableSpec0();
	    	final BufferedDataContainer container = exec.createDataContainer(spec0);

			
			iter = inTable.iterator();
			while (iter.hasNext()) {
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
				final File outFile = super.createFileForWriting(Optional.of("TabixVcf"), ".vcf.gz");
				final VcfIndexTabix application = new VcfIndexTabix();
				VcfIterator r=null;
				try {
					application.setMaxRecordsInRam(super.__maxRecordsInRam.getIntValue());
					application.setSort(super.isSettingsModelSortBoolean());
					application.addTmpDirectory(outFile.getParentFile());
					super.checkEmptyListOfThrowables(application.initializeKnime());
					r= VCFUtils.createVcfIteratorFromFile(inFile);
					super.checkEmptyListOfThrowables(application.doVcfToVcf(inFile.getPath(), r, outFile));
				}
				catch(Exception err)
					{
					throw err;
					}
				finally
				{
					CloserUtil.close(r);
					application.disposeKnime();
				}
				
				
				application.setOutputFile(outFile);

				if (!outFile.exists()) {
					throw new RuntimeException("Output file was not created");
				}
				container.addRowToTable(new DefaultRow(row.getKey(),
						super.createDataCellsForOutTableSpec0(outFile.getPath())));

			} //end while
			iter.close();
			container.close();
	        BufferedDataTable out = container.getTable();
	        return new BufferedDataTable[]{out};

		} finally {
			
		}
		    	
	
    	}
		
}
