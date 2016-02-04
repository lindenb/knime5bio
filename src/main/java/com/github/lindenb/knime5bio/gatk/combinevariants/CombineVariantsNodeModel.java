package com.github.lindenb.knime5bio.gatk.combinevariants;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.knime5bio.gatk.GatkRunner;


public class CombineVariantsNodeModel extends AbstractCombineVariantsNodeModel {
	@Override
    protected BufferedDataTable[] execute(final BufferedDataTable inData, final ExecutionContext exec) throws Exception
        {
		final GatkRunner gatkRunner=new GatkRunner();
		this.assureNodeWorkingDirectoryExists();
		final File outFile = super.createFileForWriting(Optional.of("CombineVariants"), ".vcf.gz");
		
			gatkRunner.setLogger(this.getLogger());
			gatkRunner.setGatkJarFile(super.getSettingsModelGatkpathFile());
			final Set<File> vcfFiles = super.collectFilesInOneColumn(inData, super.__vcf);
	    	if(vcfFiles.isEmpty())
	    		{
	    		throw new RuntimeException("no vcf to be merged");
	    		}
			
			List<String> args=new ArrayList<>();
	    	args.add("-T");
	    	args.add("CombineVariants");
	    	args.add("-R");
	    	args.add(super.__referenceGenome.getStringValue());
	    	for(File f:vcfFiles) {
		    	args.add("--variant");
		    	args.add(f.getPath());
		    	}
	    	if(super.getSettingsModelCaptureBedFile().isPresent())
	    		{
	    		args.add("-L");
		    	args.add(getSettingsModelCaptureBedFile().get().getPath());
	    		}
	    	
	    	args.add("-o");
		    args.add(outFile.getPath());
		    args.add("-genotypeMergeOptions");
		    args.add("UNIQUIFY");
		    
		    int ret = gatkRunner.execute(args);
		    if(ret!=0)
		    	{
		    	throw new RuntimeException("Return value!=SUCCESS for "+args);
		    	}
		    if(!outFile.exists())
		    	{
		    	throw new RuntimeException("Output file was not created");
		    	}
		    
	    	final DataTableSpec spec0 = createOutTableSpec0();
	    	final BufferedDataContainer container = exec.createDataContainer(spec0);
	    	container.addRowToTable(
	    			new DefaultRow(RowKey.createRowKey(1L),
	    				super.createDataCellsForOutTableSpec0(outFile.getPath())	
	    				)
	    			);
	    	container.close();
	        BufferedDataTable out = container.getTable();
	        return new BufferedDataTable[]{out};
        	}
	}
