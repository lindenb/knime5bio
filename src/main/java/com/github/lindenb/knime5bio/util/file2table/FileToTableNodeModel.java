package com.github.lindenb.knime5bio.util.file2table;

import java.io.File;
import java.util.Set;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import com.github.lindenb.knime5bio.FileToTable;


public class FileToTableNodeModel extends AbstractFileToTableNodeModel {
     FileToTableNodeModel() {
     }
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, 
    		final ExecutionContext exec) throws Exception
        {   
		final Set<File> files = super.collectFilesInOneColumn(inData[0],super.__path);
		if(files.size()!=1) {
			getLogger().info("expected one and only one file");
			throw new IllegalStateException("expected one and only one file");
			}
		final File inFile= files.iterator().next();
		try {
			FileToTable file2table = new FileToTable(exec);
	        return new BufferedDataTable[]{file2table.convert(inFile)};
		} catch (Exception e) {
			getLogger().error("boum", e);
			throw e;
		} finally {
		}
    }
}
