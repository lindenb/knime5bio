package com.github.lindenb.knime5bio;

import htsjdk.samtools.util.CloserUtil;

import java.io.File;
import java.util.Collections;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.port.PortType;

import com.github.lindenb.jvarkit.knime.KnimeApplication;

public abstract class AbstractJVarkitNodeModel<T extends KnimeApplication> extends
	AbstractKnime5BioNodeModel
	{
	protected AbstractJVarkitNodeModel(int inport,int outport)
		{
		/* super(inport,outport) */
		super(inport,outport);
		}

	protected AbstractJVarkitNodeModel(PortType[] inPortTypes, PortType[] outPortTypes)
		{
		super(inPortTypes, outPortTypes);
		}
	
	/** return table index in BufferedDataTable[] first by default */
	protected int getInputBufferedDataTableIndex()
		{
		return 0;
		}
	
	protected abstract DataTableSpec createDefaultOuputTableSpec();
	protected abstract T createInstanceOfKnimeApplication() throws Exception;
	protected abstract int getInputUriColumnIndex(DataTableSpec inSpec) throws Exception;
	
	
	
	protected abstract String getOutputFileSuffix();
	
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception
		{
		T instance=null;
        CloseableRowIterator iter=null;
        int inTableIndex = this.getInputBufferedDataTableIndex();
        if( inTableIndex<0 || inTableIndex>= inData.length)
        	{
        	throw new IllegalStateException("Bad index inData[]");
        	}
        BufferedDataTable inTable=inData[0];
        DataTableSpec dataOutSpec = this.createDefaultOuputTableSpec();
        BufferedDataContainer out_container = null;
        int inUriIndex = getInputUriColumnIndex(inTable.getDataTableSpec());


		try {
			instance= this.createInstanceOfKnimeApplication();
			instance.initializeKnime();
			out_container = exec.createDataContainer(dataOutSpec);
            int nRows=0;
            double total=inTable.getRowCount();
            iter=inTable.iterator();
	        while(iter.hasNext())
	                {
	                DataRow row=iter.next();
	                ++nRows;
	                DataCell cell =row.getCell(inUriIndex);

		            if(cell.isMissing())
		            	{
		            	getLogger().warn("Missing cells in "+getNodeName());
		            	continue;
		            	}
		            if(!cell.getType().equals(StringCell.TYPE))
		            	{
		            	getLogger().error("not a StringCell type in "+cell);
		            	continue;
		            	}
	                String uri = StringCell.class.cast(cell).getStringValue();
	                if(uri.isEmpty())
	                	{
		            	getLogger().error("Empty uri");
		            	continue;
	                	}
	                File fileout = new File(
	                		getKnime5BiNodeWorkingDirectory(),
	                		md5(uri)+getOutputFileSuffix()
	                		);

	                if(fileout.getParentFile()!=null)
	                	{
	                	fileout.getParentFile().mkdirs();
	                	}
	                instance.setOutputFile(fileout);
	                if(instance.executeKnime(Collections.singletonList(uri))!=0)
	                	{
	                	throw new RuntimeException("error during processing"+getNodeName()+" "+uri);
	                	}
	                
	                out_container.addRowToTable(new DefaultRow(
	                	RowKey.createRowKey(nRows),
	                	new StringCell(fileout.getPath()))
	                	);
	                
	                
	                exec.checkCanceled();
	                exec.setProgress(nRows/total);
	                }
	        	iter.close();
	        	iter=null;
	        	out_container.close();
	            BufferedDataTable out0 = out_container.getTable();
	            out_container=null;
	            return new BufferedDataTable[]{out0};
	            }
	        finally
	            {
	        	CloserUtil.close(iter);
	        	CloserUtil.close(out_container);
	        	instance.disposeKnime();
	            }
	        }

	}
