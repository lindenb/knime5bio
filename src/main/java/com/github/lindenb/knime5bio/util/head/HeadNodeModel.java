package com.github.lindenb.knime5bio.util.head;


import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import htsjdk.samtools.util.CloserUtil;


public class HeadNodeModel extends AbstractHeadNodeModel {
     HeadNodeModel() {
     }
     @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, 
    		final ExecutionContext exec) throws Exception
        {   
    	BufferedDataContainer bdc=null;
    	CloseableRowIterator iter = null;
    	int count=0;
		try {
			bdc = exec.createDataContainer(inData[0].getDataTableSpec());
			iter = inData[0].iterator();
			while(iter.hasNext()) {
				bdc.addRowToTable(iter.next());
				++count;
				if(count ==  super.__count.getIntValue()) break;
				}
			iter.close();iter=null;
			bdc.close();
			final BufferedDataTable table = bdc.getTable();
			bdc.close();bdc=null;
	        return new BufferedDataTable[]{table};
		} catch (Exception e) {
			getLogger().error("boum", e);
			throw e;
		} finally {
			CloserUtil.close(iter);
			CloserUtil.close(bdc);
		}
    }
    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs) throws InvalidSettingsException {
    	return new DataTableSpec[]{inSpecs[0]};
    }
}
