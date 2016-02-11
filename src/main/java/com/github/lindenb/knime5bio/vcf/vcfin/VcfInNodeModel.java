package com.github.lindenb.knime5bio.vcf.vcfin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import com.github.lindenb.jvarkit.tools.vcfcmp.VcfIn;
import com.github.lindenb.knime5bio.LogRowIterator;

import htsjdk.samtools.util.CloserUtil;


public class VcfInNodeModel extends AbstractVcfInNodeModel {
     VcfInNodeModel() {
     }
     
     @Override
    protected BufferedDataTable[] execute(
    		final BufferedDataTable inData0,
    		final BufferedDataTable inData1,
    		final ExecutionContext exec)
    		throws Exception
     	{ 
		this.assureNodeWorkingDirectoryExists();
		final List<File> databaseFiles = new ArrayList<>( super.collectFilesInOneColumn(inData0, __VCFDatabase));
		if(databaseFiles.size()!=1)
			{
			throw new RuntimeException("Expected Only One VCF file on input but got "+databaseFiles.size());
			}
		final int vcfColumn = super.findColumnIndexByName(inData1, __VCF);
		LogRowIterator iter = null;
		org.knime.core.node.BufferedDataContainer container=null;

		try {
	    	final DataTableSpec spec0 = createOutTableSpec0();
	    	container = exec.createDataContainer(spec0);

	    	iter = new LogRowIterator(inData1,exec);
			while(iter.hasNext())
				{
				final DataRow row = iter.next();
				final DataCell cell = row.getCell(vcfColumn);
				if(!(cell instanceof StringCell))
					{
					iter.close();
					iter=null;
					throw new InvalidSettingsException("not a string cell");
					}
				if (cell.isMissing())
					continue;
				final java.io.File inFile = new java.io.File(StringCell.class.cast(cell).getStringValue());
				if (!inFile.exists())
					{
					iter.close();
					iter=null;
					throw new java.io.FileNotFoundException("cannot find " + inFile);
					}
				if (!inFile.isFile())
					{
					iter.close();
					iter=null;
					throw new java.io.IOException("not a file: " + inFile);
					}
				final File outFile = super.createFileForWriting(Optional.of("VcfIn"), ".vcf.gz");
				final VcfIn application =new VcfIn();
				application.setDatabaseIsTabix(__databaseIsTabix.getBooleanValue());
				application.setUserAltInDatabase(__userAltInDatabase.getBooleanValue());
				application.setInverse(__inverse.getBooleanValue());
				application.setOutputFile(outFile);
				application.setInputFiles(
						Arrays.asList(databaseFiles.get(0).getPath(),inFile.getPath())
						);
				super.checkEmptyListOfThrowables(application.initializeKnime());
				super.checkEmptyListOfThrowables(application.call());
				application.disposeKnime();
				
				if (!outFile.exists()) {
					iter.close();
					iter=null;
					throw new RuntimeException("Output file was not created");
				}
				container.addRowToTable(new org.knime.core.data.def.DefaultRow(
						row.getKey(),
						this.createDataCellsForOutTableSpec0(outFile.getPath())
						));
				}
			iter.close();iter=null;
			
			container.close();
	        BufferedDataTable out = container.getTable();
	        container=null;
	        return new BufferedDataTable[]{out};
		} catch (Exception e) {
			getLogger().error("boum", e);
			throw e;
		} finally {
			CloserUtil.close(iter);
			CloserUtil.close(container);
		}
    }
}
