package com.github.lindenb.knime5bio;


import htsjdk.samtools.util.CloserUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.port.PortType;

public abstract class AbstractKnime5BioNodeModel extends
	AbstractNodeModel
	{
	/** attach jvarkit logger to knime  */
	private NodeLoggerAdapter nodeLoggerAdapter=null;
	
	protected AbstractKnime5BioNodeModel(int inport,int outport)
		{
		/* super(inport,outport) */
		super(inport,outport);
		attachvarkitLogger();
		}

	protected AbstractKnime5BioNodeModel(PortType[] inPortTypes, PortType[] outPortTypes)
		{
		super(inPortTypes, outPortTypes);
		attachvarkitLogger();
		}
	
	/** attach jvarkit logger to knime 
	 * called by constructor, output jvarkit log to knime
	 **/
	private void attachvarkitLogger()
		{
		Logger LOG=Logger.getLogger("jvarkit");
		this.nodeLoggerAdapter=new NodeLoggerAdapter();
		LOG.addHandler(this.nodeLoggerAdapter);
		}
	@Override
	protected String getPluginBaseDirectoryVariable() {
		return "com.github.lindenb.knime5bio.working.directory";
		}
	

		
	/* A wrapper for execute method in a model */
	abstract protected class AbstractExecuteNodeModelHandler
		{
		protected BufferedDataTable[] inData=null;
		protected ExecutionContext exec=null;
		
		public AbstractExecuteNodeModelHandler(final BufferedDataTable[] inData, final ExecutionContext exec)
			{
			this.inData=inData;
			this.exec=exec;
			}
		
		public AbstractExecuteNodeModelHandler()
			{
			}
		
		public void setInputBufferedDataTables(BufferedDataTable[] inData)
			{
			this.inData = inData;
			}
		
		public BufferedDataTable[] getInputBufferedDataTables() {
			return inData;
			}
		
		/** get the ExecutionContext to be used to se progress, etc*/
		public void ExecutionContext(ExecutionContext exec)
			{
			this.exec = exec;
			}
		
		public ExecutionContext getExecutionContext() {
			return exec;
			}
		
		public void checkCanceled() throws CanceledExecutionException
			{
			getExecutionContext().checkCanceled();
			}
		
		public abstract BufferedDataTable[] execute()  throws Exception;
		}
	
	@Override
	protected void onDispose() {
		super.onDispose();
		if(this.nodeLoggerAdapter!=null)
			{
			Logger LOG=Logger.getLogger("jvarkit");
			LOG.removeHandler(this.nodeLoggerAdapter);
			this.nodeLoggerAdapter=null;
			}
		}
	
	
	private class NodeLoggerAdapter
		extends java.util.logging.Handler
		{
		
		@Override
		public void publish(LogRecord record) {
			if(record.getLevel().equals(Level.OFF))
				return;
			String msg=String.valueOf(record.getMessage());
			Throwable thrown = record.getThrown();
			if(record.getLevel()==null)
				{
				
				}
			else if(record.getLevel().equals(Level.WARNING))
				{
				if(thrown==null)
					{
					getLogger().warn(msg);
					}
				else
					{
					getLogger().warn(msg,thrown);
					}
				}
			else if(record.getLevel().equals(Level.INFO))
				{
				if(thrown==null)
					{
					getLogger().warn(msg);
					}
				else
					{
					getLogger().warn(msg,thrown);
					}
				}
			else if(record.getLevel().equals(Level.SEVERE))
				{
				if(thrown==null)
					{
					getLogger().fatal(msg);
					}
				else
					{
					getLogger().fatal(msg,thrown);
					}
				}
			else
				{
				if(thrown==null)
					{
					getLogger().error(msg);
					}
				else
					{
					getLogger().error(msg,thrown);
					}
				}
			}
		@Override
		public void flush()
			{
			//do nothing
			}
		@Override
		public void close()
			{
			}
		}

	protected String getOneRequiredResource(BufferedDataTable inTable2,int inUri2Index)
		{
		CloseableRowIterator iter=null;
		String resource2=null;
		try
			{
			 iter = inTable2.iterator();
		     while(iter.hasNext())
	                {
	                DataRow row=iter.next();
	                DataCell cell =row.getCell(inUri2Index);
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
		            if(uri.trim().isEmpty())
		            	{
		            	getLogger().error("ignore empty in "+cell);
		            	continue;
		            	}
		            if(resource2!=null)
		            	{
		            	throw new RuntimeException("found two Resources but expected one:\n"+
		            		uri+"\n"+resource2	
		            		);
		            	}	
		            resource2=uri;
	                }
		     CloserUtil.close( iter);iter=null;
		     if(resource2==null)
		     	{
	            throw new RuntimeException("input resource not found in "+inTable2.getSummary());
		     	}
		     return resource2;
			}
		finally
			{
			CloserUtil.close( iter);
			}
		}
	protected List<String> getResourceSet(BufferedDataTable inTable2,int inUri2Index)
		{
		List<String> set=new ArrayList<>();
		CloseableRowIterator iter=null;
		try
			{
			 iter = inTable2.iterator();
		     while(iter.hasNext())
	                {
	                DataRow row=iter.next();
	                DataCell cell =row.getCell(inUri2Index);
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
		            if(uri.trim().isEmpty())
		            	{
		            	getLogger().error("ignore empty in "+cell);
		            	continue;
		            	}
		            set.add(uri.trim());
	                }
		     CloserUtil.close( iter);iter=null;
		     return set;
			}
		finally
			{
			CloserUtil.close( iter);
			}
		}
	}
