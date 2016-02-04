package com.github.lindenb.knime5bio.gatk.leftalign;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.knime5bio.gatk.GatkRunner;


public class LeftAlignAndTrimVariantsNodeModel extends AbstractLeftAlignAndTrimVariantsNodeModel {
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable inTable, final ExecutionContext exec) throws Exception
        {
		final int vcfColumn = super.findColumnIndexByName(inTable,super.getSettingsModelVcf());
		CloseableRowIterator iter=null;
		long nRows=1;
		try {
	    	final DataTableSpec spec0 = createOutTableSpec0();
	    	final BufferedDataContainer container = exec.createDataContainer(spec0);

			
			iter = inTable.iterator();
			while(iter.hasNext()) {
				final DataRow row = iter.next();
				final DataCell cell = row.getCell(vcfColumn);
				if(cell.isMissing()) continue;
				final File inFile = new File(StringCell.class.cast(cell).getStringValue());
				if(!inFile.exists()) throw new FileNotFoundException("cannot find "+inFile);
				if(!inFile.isFile()) throw new FileNotFoundException("not a file: "+inFile);
				
				final GatkRunner gatkRunner=new GatkRunner();
				this.assureNodeWorkingDirectoryExists();
				final File outFile = super.createFileForWriting(Optional.of("LeftAlignAndTrimVariants"), ".vcf.gz");
				
					gatkRunner.setLogger(this.getLogger());
					gatkRunner.setGatkJarFile(super.getSettingsModelGatkpathFile());
			    	
					
					List<String> args=new ArrayList<>();
			    	args.add("-T");
			    	args.add("LeftAlignAndTrimVariants");
			    	args.add("-R");
			    	args.add(super.__referenceGenome.getStringValue());
			    	
				    args.add("--variant");
				    args.add(inFile.getPath());
				    	
			    	if(super.getSettingsModelCaptureBedFile().isPresent())
			    		{
			    		args.add("-L");
				    	args.add(getSettingsModelCaptureBedFile().get().getPath());
			    		}
			    	
			    	
			    	
			    	this.fillGatkArgs(args);
			    	
			    	
			    	args.add("-o");
				    args.add(outFile.getPath());
				    
				    int ret = gatkRunner.execute(args);
				    if(ret!=0)
				    	{
				    	throw new RuntimeException("Return value!=SUCCESS for "+args);
				    	}
				    if(!outFile.exists())
				    	{
				    	throw new RuntimeException("Output file was not created");
				    	}
			    	container.addRowToTable(
			    			new DefaultRow(RowKey.createRowKey(nRows++),
			    				super.createDataCellsForOutTableSpec0(outFile.getPath())	
			    				)
			    			);
	
				    exec.checkCanceled();
				} //end while
			iter.close();
			container.close();
	        BufferedDataTable out = container.getTable();
	        return new BufferedDataTable[]{out};

		} finally {
			
		}
		    	
	
    	}
		
}
