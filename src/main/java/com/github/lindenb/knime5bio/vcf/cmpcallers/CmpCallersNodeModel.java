package com.github.lindenb.knime5bio.vcf.cmpcallers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import com.github.lindenb.jvarkit.tools.vcfcmp.VcfCompareCallers;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.FileToTable;

import htsjdk.samtools.util.CloserUtil;


public class CmpCallersNodeModel extends AbstractCmpCallersNodeModel {
     CmpCallersNodeModel() {
     }
     
     @Override
    protected BufferedDataTable[] execute(BufferedDataTable inData0, ExecutionContext exec) throws Exception {
		VcfIterator iterators[]=new VcfIterator[]{null,null};
		final VcfCompareCallers application = new VcfCompareCallers();
		this.assureNodeWorkingDirectoryExists();
		final File outFile = super.createFileForWriting(Optional.of("VcfCompareCallers"), ".txt");
		outFile.deleteOnExit();
		List<File> files = new ArrayList<>( super.collectFilesInOneColumn(inData0, __VCF));
		if(files.size()!=2)
			{
			throw new RuntimeException("Expected Only two VCF file on input but got "+files.size());
			}
		try {
			
	    	
	    	application.setNumberOfExampleVariants(super.__numberOfExampleVariants.getIntValue());
	    	application.setHomRefIsNoCall(super.__homRefIsNoCall.getBooleanValue());
			application.setOutputFile(outFile);
			
			if(!super.__captureFile.getStringValue().trim().isEmpty()) {
				application.setCaptureFile(new File(super.__captureFile.getStringValue()));
				}
			
			if(!super.__exampleFile.getStringValue().trim().isEmpty()) {
				application.setExampleFile(new File(super.__exampleFile.getStringValue()));
				}
			

			checkEmptyListOfThrowables(application.initializeKnime());
			iterators[0] = VCFUtils.createVcfIteratorFromFile(files.get(0));
			iterators[1] = VCFUtils.createVcfIteratorFromFile(files.get(1));
			checkEmptyListOfThrowables(application.compare(iterators[0],iterators[1]));
			
			
			if (!outFile.exists()) {
				throw new RuntimeException("Output file was not created");
			}
			FileToTable ft2= new FileToTable(exec);
			final BufferedDataTable out = ft2.convert(outFile);
			outFile.delete();
	        return new BufferedDataTable[]{out};
		} catch (Exception e) {
			getLogger().error("boum", e);
			throw e;
		} finally {
			CloserUtil.close(iterators[0]);
			CloserUtil.close(iterators[1]);
			application.disposeKnime();
		}
    }
}
