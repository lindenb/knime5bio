package com.github.lindenb.knime5bio.vcf.groupbygene;
import java.io.File;
import java.util.Optional;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.groupbygene.GroupByGene;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.FileToTable;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVcfIterator;


public class GroupByGeneNodeModel extends AbstractGroupByGeneNodeModel {
@Override
    protected BufferedDataTable[] execute(
    		final BufferedDataTable headerData, 
    		final BufferedDataTable bodyData, 
    		final ExecutionContext exec) throws Exception
        {     	
		final GroupByGene application = new GroupByGene();
		super.assureNodeWorkingDirectoryExists();
     	try {
     		final File outputFile = super.createFileForWriting(Optional.of("groupbygene"), ".tsv");
     		outputFile.deleteOnExit();
     		application.setMaxRecordsInRam(this.__maxRecordsInRam.getIntValue());
    		application.addTmpDirectory(this.getNodeWorkingDirectory());	
    		super.checkEmptyListOfThrowables(application.initializeKnime());

    		application.initializeSortingCollections();
     		application.setOutputFile(outputFile);

    		
    		
     		VcfIterator vcfIn = new KnimeVcfIterator(headerData,bodyData);
     		application.readVcf(vcfIn);
     		
     		vcfIn.close(); vcfIn=null;
     		application.dump();
     		
     		final FileToTable file2table = new FileToTable(exec);
     		final BufferedDataTable table = file2table.convert(outputFile);
     		outputFile.delete();
     		
     		return new BufferedDataTable[]{table};
     		
		
		} catch (Exception e) {
			getLogger().error("boum", e);
			e.printStackTrace();
			throw e;
		} finally {
			application.disposeKnime();
		}
        }
	}
