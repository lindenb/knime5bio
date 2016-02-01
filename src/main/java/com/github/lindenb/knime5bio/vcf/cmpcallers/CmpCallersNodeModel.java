package com.github.lindenb.knime5bio.vcf.cmpcallers;

import java.io.File;
import java.util.Optional;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import com.github.lindenb.jvarkit.tools.bioalcidae.BioAlcidae;
import com.github.lindenb.jvarkit.tools.vcfcmp.VcfCompareCallers;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.FileToTable;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVcfIterator;

import htsjdk.samtools.util.CloserUtil;


public class CmpCallersNodeModel extends AbstractCmpCallersNodeModel {
     CmpCallersNodeModel() {
     }
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, 
    		final ExecutionContext exec) throws Exception
        {   
		VcfIterator iterators[]=new VcfIterator[]{null,null};
		final VcfCompareCallers application = new VcfCompareCallers();
		this.assureNodeWorkingDirectoryExists();
		final File outFile = super.createFileForWriting(Optional.of("VcfCompareCallers"), ".txt");
		outFile.deleteOnExit();
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
			iterators[0] = new KnimeVcfIterator(inData[0], inData[1]);
			iterators[1] = new KnimeVcfIterator(inData[2], inData[3]);
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
