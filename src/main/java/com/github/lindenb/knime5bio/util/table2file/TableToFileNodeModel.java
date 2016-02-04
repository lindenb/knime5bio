package com.github.lindenb.knime5bio.util.table2file;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import com.github.lindenb.knime5bio.FileToTable;
import com.github.lindenb.knime5bio.TableToFile;

import htsjdk.samtools.util.CloserUtil;


public class TableToFileNodeModel extends AbstractTableToFileNodeModel {
     TableToFileNodeModel() {
     }
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable inData, 
    		final ExecutionContext exec) throws Exception
        {   
		String suffix = super.__extension.getStringValue().trim();
		if(suffix.isEmpty()) suffix=".tsv";
		if(!suffix.startsWith(".")) suffix="."+suffix;
		File outputFile=null;
		BufferedDataContainer container =null;
		try {
			assureNodeWorkingDirectoryExists();
			TableToFile table2file = new TableToFile(exec);
			table2file.setNullValue(__nullValue.getStringValue());
			table2file.setPrintHeader(__printHeader.getBooleanValue());
			outputFile = super.createFileForWriting(Optional.of("table2file"),suffix);
			table2file.convert(inData, outputFile);
			
			if (!outputFile.exists()) {
				throw new RuntimeException("Output file was not created");
			}


			
	    	final DataTableSpec spec0 = createOutTableSpec0();
	    	container = exec.createDataContainer(spec0);
			container.addRowToTable(new DefaultRow(RowKey.createRowKey(1L),
					super.createDataCellsForOutTableSpec0(outputFile.getPath())));
			container.close();
	        BufferedDataTable out = container.getTable();
	        container=null;
	        return new BufferedDataTable[]{out};
		} catch (Exception e) {
			if(outputFile!=null) outputFile.delete();
			getLogger().error("boum", e);
			throw e;
		} finally {
			CloserUtil.close(container);
		}
    }
}
