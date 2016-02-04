package com.github.lindenb.knime5bio.vcf.renamechr;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import com.github.lindenb.jvarkit.tools.misc.ConvertVcfChromosomes;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.LogRowIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;


public class RenameContigNodeModel extends AbstractRenameContigNodeModel {
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable inTable, 
    		final ExecutionContext exec) throws Exception
        {     	
		BufferedDataContainer container = null;
		LogRowIterator iter = null;
		final int vcfColumn = super.findColumnIndexByName(inTable,super.getSettingsModelVcf());
		this.assureNodeWorkingDirectoryExists();
		ConvertVcfChromosomes application = new ConvertVcfChromosomes();
     	try {
     		application.setMappingFile(new File(super.__mappingFile.getStringValue()));
     		application.setIgnore_if_no_mapping(super.__ignore_if_no_mapping.getBooleanValue());
     		application.setUse_original_chrom_name_if_no_mapping(super.__use_original_chrom_name_if_no_mapping.getBooleanValue());
    		checkEmptyListOfThrowables(application.initializeKnime());

    		
    		
	    	final DataTableSpec spec0 = createOutTableSpec0();
	    	container = exec.createDataContainer(spec0);

			iter = new LogRowIterator("Rename Contig ",inTable,exec);
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
				final File outFile = super.createFileForWriting(Optional.of("RenameContig"), ".vcf.gz");
				VcfIterator r=null;
				VariantContextWriter w=null;
				try {
					r= VCFUtils.createVcfIteratorFromFile(inFile);
					w = VCFUtils.createVariantContextWriter(outFile);
					super.checkEmptyListOfThrowables(application.doVcfToVcf(inFile.getPath(), r, w));
				}
				finally
				{
					iter.close();
					CloserUtil.close(r);
					CloserUtil.close(w);
				}
				
				

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
		} catch (Exception e) {
			getLogger().error("boum", e);
			e.printStackTrace();
			throw e;
		} finally {
			application.disposeKnime();
		}
        }
}
