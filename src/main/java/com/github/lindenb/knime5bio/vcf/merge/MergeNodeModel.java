package com.github.lindenb.knime5bio.vcf.merge;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.vcfmerge.VCFMerge2;

import htsjdk.samtools.util.CloserUtil;

public class MergeNodeModel extends AbstractMergeNodeModel {
     MergeNodeModel() {
     }
     
     @Override
    protected BufferedDataTable[] execute(
    		final BufferedDataTable inData0,
    		final ExecutionContext exec)
    		throws Exception
     	{ 
		this.assureNodeWorkingDirectoryExists();
		final List<File> databaseFiles = new ArrayList<>( super.collectFilesInOneColumn(inData0, __vcf));
		if(databaseFiles.isEmpty())
			{
			throw new RuntimeException("Expected at least one VCF file on input");
			}
		org.knime.core.node.BufferedDataContainer container=null;

		try {
	    	final DataTableSpec spec0 = createOutTableSpec0();
	    	container = exec.createDataContainer(spec0);
			final File outputFile = super.createFileForWriting(Optional.of("VcfMerge"), ".vcf.gz");

		
			final VCFMerge2 application =new VCFMerge2();
			application.setOutputFile(outputFile);
			application.setDoNotMergeRowLines(__doNotMergeRowLines.getBooleanValue());
			application.setFilesAreSorted(__filesAreSorted.getBooleanValue());
			application.setTmpdir(outputFile.getParentFile());;
			application.setMaxRecordsInRam(__maxRecordsInRam.getIntValue());
			application.setUseHomRefForUnknown(__useHomRefForUnknown.getBooleanValue());
			application.setInputFiles(databaseFiles.stream().map(F->F.getPath()).collect(Collectors.toList()));
			super.checkEmptyListOfThrowables(application.initializeKnime());
			super.checkEmptyListOfThrowables(application.call());
			application.disposeKnime();
				
			if (!outputFile.exists()) {
				throw new RuntimeException("Output file was not created");
				}
			container.addRowToTable(new org.knime.core.data.def.DefaultRow(
					RowKey.createRowKey(1L),
					this.createDataCellsForOutTableSpec0(outputFile.getPath())
					));
			
			container.close();
	        BufferedDataTable out = container.getTable();
	        container=null;
	        return new BufferedDataTable[]{out};
		} catch (Exception e) {
			getLogger().error("boum", e);
			throw e;
		} finally {
			CloserUtil.close(container);
		}
    }

}
