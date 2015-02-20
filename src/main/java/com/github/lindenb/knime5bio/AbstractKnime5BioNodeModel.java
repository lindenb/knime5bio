package com.github.lindenb.knime5bio;


import java.io.File;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeModel;
import org.knime.core.node.port.PortType;

public abstract class AbstractKnime5BioNodeModel extends
	NodeModel
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
	
	protected File getKnime5BioBaseDirectory()
		{
		final String KNIME5DIR="com.github.lindenb.knime5bio.working.directory";
		String s= peekFlowVariableString(KNIME5DIR);
		if(s==null) throw new IllegalStateException(
				"Flow Variable "+KNIME5DIR+" undefined. "+
				"Add this variable in the KNIME workspace (right click in the workspace icon) and set its value to an existing directory."
				);
		File dir=new File(s);
		if(!dir.exists())
			{
			throw new IllegalStateException(KNIME5DIR+" defined as  "+s+" but doesn't exists");
			}
		if(!dir.isDirectory())
			{
			throw new IllegalStateException(KNIME5DIR+" defined as  "+s+" but it's not a directory");
			}
		return dir;
		}
	
	
	protected abstract  String getNodeUniqId();
	protected abstract String getNodeName();
	
	protected File getKnime5BiNodeWorkingDirectory()
		{
		File parent= getKnime5BioBaseDirectory();
		File me = new File(parent, getNodeUniqId()+"."+getNodeName());
		return me;
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

	
	}
