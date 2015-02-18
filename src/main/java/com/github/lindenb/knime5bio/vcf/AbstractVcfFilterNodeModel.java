package com.github.lindenb.knime5bio.vcf;
import org.knime.core.node.*;
import org.knime.core.data.*;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.*;

import com.github.lindenb.knime5bio.AbstractKnime5BioNodeModel;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.readers.LineIteratorImpl;
import htsjdk.tribble.readers.LineReaderUtil;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.variantcontext.writer.VariantContextWriterBuilder;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;

public abstract class AbstractVcfFilterNodeModel
	extends AbstractKnime5BioNodeModel
	{	
	protected AbstractVcfFilterNodeModel(int inport,int outport)
		{
		/* super(inport,outport) */
		super(inport,outport);
		}
	
    
	protected abstract class VcfFilterHandler
		extends AbstractKnime5BioNodeModel.AbstractExecuteNodeModelHandler
		{
		/* number of rows in inputTable */
		protected int countInputTableRows=0;
		protected int inputTableIndex=0;
		protected int inVariantIndex=0;
		protected int outVariantCount=0;
		protected BufferedDataContainer outputDataContainer=null;
		protected VariantContextWriter variantContextWriter=null;

		
		
		public VcfFilterHandler(final BufferedDataTable[] inData, final ExecutionContext exec)
			{
			super(inData,exec);
			}
		
		/** return table index in BufferedDataTable[], called by getInputBufferedDataTable */
		public int getInputBufferedDataTableIndex()
			{
			BufferedDataTable[] inData = this.getInputBufferedDataTables();
			if(inData==null)
				throw new IllegalStateException("BufferedDataTable[] inData have not been set");
			if(inData.length==0)
				throw new IllegalStateException("BufferedDataTable[] inData is empty: no vcf to read");

			if(inData.length==1) return 0;
			throw new IllegalStateException("BufferedDataTable[] inData : index not specified");
			}
		
		/** return BufferedDataTable in 'BufferedDataTable[] inData' containing vcf urls */
		public BufferedDataTable getInputBufferedDataTable()
			{
			BufferedDataTable[] inData = this.getInputBufferedDataTables();
			if(inData==null)
				throw new IllegalStateException("BufferedDataTable[] inData have not been set");
			int index  = getInputBufferedDataTableIndex();
			if(index<0 || index>= inData.length)
				{
				throw new IndexOutOfBoundsException("index="+index+" but inData.length="+inData.length);
				}
			return inData[index];
			}
		
		public DataTableSpec createDefaultOutputDataTableSpec()
			{
			DataColumnSpec colspecs[]=new DataColumnSpec[2];
			colspecs[0] = new DataColumnSpecCreator("VCF",StringCell.TYPE).createSpec();
			colspecs[1] = new DataColumnSpecCreator("COUNT",IntCell.TYPE).createSpec();
			return new DataTableSpec(colspecs);
			}
		
		public BufferedDataContainer createDefaultOutputBufferedDataContainer()
			{
			DataTableSpec spec = this.createDefaultOutputDataTableSpec();
			return getExecutionContext().createDataContainer(spec);
			}
		
		/** in the input table, return the column containing the path of the VCF */
		public int getInputPathColumnIndex()  throws InvalidSettingsException 
			{
			/* input container */
			BufferedDataTable inputTable = this.getInputBufferedDataTable();
			DataTableSpec specs = inputTable.getDataTableSpec();
			if(specs.getNumColumns()==0)
				{
				throw new IllegalStateException("Input table is empty");
				}
			int found_index=-1;
			for(int i=0;i< specs.getNumColumns();++i)
				{
				DataColumnSpec  spec= specs.getColumnSpec(i);
				if(spec.getType().equals(StringCell.TYPE))
					{
					if(found_index==-1)
						{
						found_index=i;
						}
					else
						{
						throw new IllegalStateException("Found two String columns in the input table");
						}
					}
				}
			if(found_index==-1)
				{
				throw new IllegalStateException("Found no String columns in the input table");
				}
			return found_index;
			}
		
		/** give a chance to initialize data before execute. 
		 * default: do nothing */
		public void beginExecute()
			{
			}
		/** give a chance to dispose data after execute */
		public void endExecute()
			{
			}
		
		protected VCFHeader addCommonHeaders(VCFHeader h2)
			{
			h2.addMetaDataLine(new VCFHeaderLine(getNodeName()+"CompilationDate",getCompilationDate()));
			return h2;
			}
		
		protected VCFHeader createOuputHeader(final VCFHeader in)
			{
			VCFHeader out= new VCFHeader(in);
			out = addCommonHeaders(out);
			return out;
			}
		
		protected File createOutputFile(String uri)
			{
			File mydir= getKnime5BiNodeWorkingDirectory();
			return new File(mydir,md5(uri)+"."+getNodeName().toLowerCase()+".vcf.gz");
			}
		
		protected boolean acceptVariant(VariantContext ctx)
			{
			return true;
			}	
		
		protected WhatToDo processVariant(VariantContext ctx)
			{
			if(!acceptVariant(ctx)) return WhatToDo.CONTINUE;
			this.variantContextWriter.add(ctx);
			++this.outVariantCount;
			return WhatToDo.CONTINUE;
			}
		
		protected boolean acceptHeader(VCFHeader in)
			{
			return true;
			}
		
		protected void processVcfFile(String uri) throws Exception
			{
			System.err.println("[LOG] Process "+uri);
			InputStream vcfStream=null;
			File outFile =null;
			try
				{
				this.inVariantIndex=0;
				this.outVariantCount=0;
				getLogger().info("opening "+uri);
				vcfStream = openUriForInputStream(uri);
				VCFCodec codec=new VCFCodec();
				System.err.println("[LOG] vcf codec:" +codec);
				LineIterator iter= new LineIteratorImpl(LineReaderUtil.fromBufferedStream(vcfStream));
				System.err.println("[LOG]  iter ok");
				VCFHeader inHeader =(VCFHeader)codec.readActualHeader(iter);
				if(!acceptHeader(inHeader))
					{
					return;
					}
				VCFHeader outHeader = createOuputHeader(inHeader);
				outFile = createOutputFile(uri);
				
				if(outFile.getParentFile()!=null)
					{
					outFile.getParentFile().mkdirs();
					}
				VariantContextWriterBuilder vcwb=new VariantContextWriterBuilder();
				vcwb.setCreateMD5(false);
				vcwb.setReferenceDictionary(null);
				vcwb.setOutputFile(outFile);
				vcwb.clearOptions();
				this.variantContextWriter = vcwb.build();
				this.variantContextWriter.writeHeader(outHeader);
				while(iter.hasNext())
					{
					String line= iter.next();
					
					VariantContext ctx = codec.decode(line);
					++this.inVariantIndex;
					checkCanceled();
					WhatToDo choice = processVariant(ctx);
					if(choice== WhatToDo.BREAK) break;
					}
				this.variantContextWriter.close();
				this.variantContextWriter=null;
				
				DataCell cells[]=new DataCell[2];
				cells[0]=new StringCell(outFile.getPath());
				cells[1]=new IntCell(this.outVariantCount);
				
				this.outputDataContainer.addRowToTable(new DefaultRow(
					org.knime.core.data.RowKey.createRowKey(this.inputTableIndex),
					cells
					));
				
				}
			catch(Exception err)
				{
				err.printStackTrace();
				CloserUtil.close(this.variantContextWriter);
				this.variantContextWriter=null;
				if(outFile!=null) outFile.delete();
				throw err;
				}
			finally
				{
				CloserUtil.close(this.variantContextWriter);
				CloserUtil.close(vcfStream);
				}
			
			}
		
		protected void loopOverInputTable() throws Exception
			{
			/* iterator on input table */
			CloseableRowIterator rowIter=null;
			/* input container */
			BufferedDataTable inputTable = this.getInputBufferedDataTable();
			this.countInputTableRows = inputTable.getRowCount();
			/* get the column containing the uri */
			int inputPathColumnIndex = this.getInputPathColumnIndex();
			this.inputTableIndex = 0;
			try {
				rowIter=inputTable.iterator();
			    while(rowIter.hasNext())
			        {
			    	DataRow row= rowIter.next();
		            DataCell cell =row.getCell(inputPathColumnIndex);
		            this.checkCanceled();
		            if(cell.isMissing())
		            	{
		            	getLogger().warn("Missing cells in "+getNodeName());
		            	++this.inputTableIndex;
		            	continue;
		            	}
		            if(!cell.getType().equals(StringCell.TYPE))
		            	{
		            	getLogger().error("not a StringCell type in "+cell);
		            	++this.inputTableIndex;
		            	continue;
		            	}
		            String vcfuri = StringCell.class.cast(cell).getStringValue();
		            if(vcfuri.trim().isEmpty())
		            	{
		            	++this.inputTableIndex;
		            	continue;
		            	}
		            System.err.println("[LOG]vcfuri="+vcfuri);
		            processVcfFile(vcfuri);
			    	++this.inputTableIndex;
			    	this.getExecutionContext().setProgress(this.inputTableIndex/(double)this.countInputTableRows);
			        }
				}
			finally
				{
				CloserUtil.close(rowIter);
				}
			}
		
		@Override
		public BufferedDataTable[] execute() throws Exception
			{
			System.err.println("go for execute");
			/* output container */
			this.outputDataContainer = null;
			beginExecute();
			try
				{
				this.outputDataContainer = this.createDefaultOutputBufferedDataContainer();
				this.loopOverInputTable();
				/* we're done, create the output */
				this.outputDataContainer.close();
		        BufferedDataTable outtable = this.outputDataContainer.getTable();
		        this.outputDataContainer=null;
		    	System.err.println("end for execute");
		        return new BufferedDataTable[]{outtable};
				}
			catch(Exception err)
				{
				err.printStackTrace();
				getLogger().error(String.valueOf(err.getMessage()), err);
				throw err;
				}
			finally
				{	
				CloserUtil.close(this.outputDataContainer);
				this.outputDataContainer=null;
				endExecute();
				}
		
			}
		}
	
	protected abstract VcfFilterHandler createVcfFilterHandler(
			BufferedDataTable[] inData,
			ExecutionContext exec);
	
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception {
		 System.err.println("[LOG]"+getNodeName()+" execute0");
		 System.err.println("[LOG]"+getNodeName()+" execute");
		 VcfFilterHandler handler= createVcfFilterHandler(inData, exec);
		 System.err.println("[LOG]"+getNodeName()+" GOT handler");
		return handler.execute();
		}
	
	protected void removeVCFFiles()
		{
		FileFilter filter=new FileFilter() {
			@Override
			public boolean accept(File path) {
				return path.isFile() && path.getName().endsWith(".vcf.gz");
			}
		};
		File dir= getKnime5BiNodeWorkingDirectory();
		if(!dir.exists()) return;
		for(File f :dir.listFiles(filter))
			{
			getLogger().warn("remove "+f);
			f.delete();
			}
		}
	
	@Override
	protected void onDispose() {
		super.onDispose();
		getLogger().info(getNodeName()+": disposed");
		}
	@Override
	protected void reset() {
		super.reset();
		removeVCFFiles();
		getLogger().info(getNodeName()+": reset");
		}
	}
