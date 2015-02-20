package com.github.lindenb.knime5bio;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFEncoder;
import htsjdk.variant.vcf.VCFFilterHeaderLine;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
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

	
	/** create a TableDataSpec from a VCF header */
	protected DataTableSpec createDataTableSpecFromVcfHeader(final VCFHeader header)
		{
		DataColumnSpecCreator dcsc=null;
		Map<String,String> props=new HashMap<String,String>();
		List<String> samples= header.getSampleNamesInOrder();
		DataColumnSpec cols[]=new DataColumnSpec[9+samples.size()];
		cols[0] = new DataColumnSpecCreator("CHROM",StringCell.TYPE).createSpec();
		cols[1] = new DataColumnSpecCreator("POS",IntCell.TYPE).createSpec();
		cols[2] = new DataColumnSpecCreator("ID",StringCell.TYPE).createSpec();
		cols[3] = new DataColumnSpecCreator("REF",StringCell.TYPE).createSpec();
		cols[4] = new DataColumnSpecCreator("ALT",StringCell.TYPE).createSpec();
		cols[5] = new DataColumnSpecCreator("QUAL",DoubleCell.TYPE).createSpec();
		
		//FILTER
		props=new HashMap<String,String>();
		for(VCFFilterHeaderLine h:header.getFilterLines())
			{
			props.put(h.getKey(), h.getValue());
			}
		dcsc = new DataColumnSpecCreator("FILTER",StringCell.TYPE);
		if(!props.isEmpty()) dcsc.setProperties(new DataColumnProperties(props));
		cols[6] = dcsc.createSpec();
		
		//INFO
		props=new HashMap<String,String>();
		for(VCFInfoHeaderLine h:header.getInfoHeaderLines())
			{
			props.put(h.getKey(), h.getDescription());
			}
		dcsc = new DataColumnSpecCreator("INFO",StringCell.TYPE);
		cols[7] = dcsc.createSpec();
		
		
		//FORMAT
		props=new HashMap<String,String>();
		for(VCFFormatHeaderLine h:header.getFormatHeaderLines())
			{
			props.put(h.getKey(), h.getDescription());
			}
		dcsc = new DataColumnSpecCreator("FORMAT",StringCell.TYPE);
		cols[8] = dcsc.createSpec();
		for(int i=0;i< samples.size();++i)
			{
			cols[9+i] = new DataColumnSpecCreator(samples.get(i),StringCell.TYPE).createSpec();
			}
		return new DataTableSpec(cols);
		}
	
	private final Pattern _tab=Pattern.compile("[\t]");
	protected  DataCell[] createDataRowFromVariantContext(final VCFEncoder vcfEncoder,final VariantContext ctx)
		{
		String cols[]=_tab.split(vcfEncoder.encode(ctx));
		DataCell cells[]=new DataCell[9+ ctx.getNSamples()];
		
		cells[0]= new StringCell(ctx.getChr());
		cells[1]= new IntCell(ctx.getStart());
		cells[2]= (!ctx.hasID()?DataType.getMissingCell():new StringCell(ctx.getID()));
		cells[3]= new StringCell(ctx.getReference().getDisplayString());
		
		if(!ctx.isVariant())
			{
			cells[4] = DataType.getMissingCell();
			}
		else
			{
			cells[4] = new StringCell(cols[4]);
			}
		if(!ctx.hasLog10PError())
			{
			cells[5] = DataType.getMissingCell();
			}
		else
			{
			cells[5] = new DoubleCell(ctx.getPhredScaledQual());
			}
		
		if(!ctx.isFiltered())
			{
			cells[6] = DataType.getMissingCell();
			}
		else
			{
			cells[7] = new StringCell(cols[7]);
			}
		cells[8] = new StringCell(cols[8]);
		cells[9] = new StringCell(cols[9]);
		for(int i=0;i< ctx.getNSamples();++i)
			{
			cells[9+i]= (!ctx.getGenotype(i).isAvailable()?
					 DataType.getMissingCell():
					 new StringCell(cols[9+i]))
					 ;
			}
		
		return cells;
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
	protected void reset() {	
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
