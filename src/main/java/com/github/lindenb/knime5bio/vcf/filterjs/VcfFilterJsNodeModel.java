package com.github.lindenb.knime5bio.vcf.filterjs;
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

import com.github.lindenb.jvarkit.tools.vcffilterjs.VCFFilterJS;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.LogRowIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;


public class VcfFilterJsNodeModel extends AbstractVcfFilterJsNodeModel {
	
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable inTable, final ExecutionContext exec) throws Exception {
		final int vcfColumn = super.findColumnIndexByName(inTable,super.__vcf);
		this.assureNodeWorkingDirectoryExists();
		BufferedDataContainer container=null;
		LogRowIterator iter=null;
		try {
	    	final DataTableSpec spec0 = createOutTableSpec0();
	    	container = exec.createDataContainer(spec0);
	
			
			iter = new LogRowIterator(inTable,exec);
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
				final File outFile = super.createFileForWriting(Optional.of("VCFFilterJS"), ".vcf.gz");
				final VCFFilterJS application = new VCFFilterJS();

				VcfIterator r=null;
				VariantContextWriter w=null;
				try {
		    		application.setJavascriptExpr(super.getSettingsModelVcfExprString());     		
		    		checkEmptyListOfThrowables(application.initializeKnime());
		    		r= VCFUtils.createVcfIteratorFromFile(inFile);
		     		w= VCFUtils.createVariantContextWriter(outFile);
		     		checkEmptyListOfThrowables(application.doVcfToVcf(this.getNodeName(),r,w));
					r.close();
					w.close();
				}
				finally
				{
					CloserUtil.close(r);
					CloserUtil.close(w);
					application.disposeKnime();
				}
	
				if (!outFile.exists()) {
					throw new RuntimeException("Output file was not created");
				}
				container.addRowToTable(new DefaultRow(row.getKey(),
						super.createDataCellsForOutTableSpec0(outFile.getPath())));
	
			} //end while
			iter.close();iter=null;
			container.close();
	        BufferedDataTable out = container.getTable();
	        container=null;
	        return new BufferedDataTable[]{out};
	
		} finally {
			CloserUtil.close(iter);
			CloserUtil.close(container);
		}
	}
}
