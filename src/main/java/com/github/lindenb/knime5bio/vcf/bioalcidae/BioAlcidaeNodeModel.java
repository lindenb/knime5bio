package com.github.lindenb.knime5bio.vcf.bioalcidae;

import java.io.File;
import java.util.Optional;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import com.github.lindenb.jvarkit.tools.bioalcidae.BioAlcidae;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVcfIterator;

import htsjdk.samtools.util.CloserUtil;


public class BioAlcidaeNodeModel extends AbstractBioAlcidaeNodeModel {
     BioAlcidaeNodeModel() {
     }
@Override
    protected BufferedDataTable[] execute(
    		final BufferedDataTable headerTable, 
    		final BufferedDataTable bodyTable, 
    		final ExecutionContext exec) throws Exception
        {   
		VcfIterator iter=null;
		final BioAlcidae application = new BioAlcidae();
		this.assureNodeWorkingDirectoryExists();
		final File outFile = super.createFileForWriting(Optional.of("BioAlcidae"), ".txt");
		try {
	    	final DataTableSpec spec0 = createOutTableSpec0();
	    	final BufferedDataContainer container = exec.createDataContainer(spec0);

	    	application.setFormatString("VCF");
			application.setOutputFile(outFile);
			
			final String javascriptExpr = super.getSettingsModelScriptExpr().getStringValue().trim();
			if(!javascriptExpr.isEmpty())
				{
				application.setJavascriptExpr(javascriptExpr);
				}
			if(super.getSettingsModelJavascriptFileFile().isPresent())
				{
				application.setJavascriptFile(super.getSettingsModelJavascriptFileFile().get());
				}
			super.checkEmptyListOfThrowables(application.initializeKnime());
			
			
			application.initializeJavaScript();
			iter = new KnimeVcfIterator(headerTable,bodyTable);
			application.executeAsVcf(iter);
	 	
			
			if (!outFile.exists()) {
				throw new RuntimeException("Output file was not created");
			}
			container.addRowToTable(new DefaultRow(RowKey.createRowKey(1L),
					super.createDataCellsForOutTableSpec0(outFile.getPath())));

			
			container.close();
	        BufferedDataTable out = container.getTable();
	        return new BufferedDataTable[]{out};
		} catch (Exception e) {
			getLogger().error("boum", e);
			throw e;
		} finally {
			CloserUtil.close(iter);
			application.disposeKnime();
		}
    }
}
