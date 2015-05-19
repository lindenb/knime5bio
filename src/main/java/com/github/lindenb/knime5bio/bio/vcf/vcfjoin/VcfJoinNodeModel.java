package com.github.lindenb.knime5bio.bio.vcf.vcfjoin;
import org.knime.core.node.*;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.data.*;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.*;

import com.github.lindenb.jvarkit.tools.vcfcmp.VcfIn;
import com.github.lindenb.jvarkit.util.htsjdk.HtsjdkVersion;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;
import com.github.lindenb.jvarkit.util.vcf.IndexedVcfFileReader;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.AbstractKnime5BioNodeModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;
import htsjdk.variant.vcf.VCFInfoHeaderLine;


/* read two VCFS, do something , output one vcf */
public class VcfJoinNodeModel
	extends AbstractKnime5BioNodeModel
	{	
	
	/** VCF path *************************************************************/


	final static String CONFIG_VCFINPUT1 = "CONFIG_VCFINPUT1";
	final static String DEFAULT_VCFINPUT1 = "COLUMN";

	
	private org.knime.core.node.defaultnodesettings.SettingsModelColumnName m_col1 = 
		new  org.knime.core.node.defaultnodesettings.SettingsModelColumnName(
			CONFIG_VCFINPUT1,
			DEFAULT_VCFINPUT1
			);

	protected org.knime.core.node.defaultnodesettings.SettingsModelColumnName getPropertyVcfInput1Settings()
		{
		return this.m_col1;
		}
	
	protected String getPropertyVcfInput1Value()
		{
		return getPropertyVcfInput1Settings().getStringValue();
		}

	protected int findVcfInput1ColumnIndex(final  DataTableSpec inSpec)
		{
		int index= inSpec.findColumnIndex(getPropertyVcfInput1Value());
		
		return index;
		}
	
	protected int findVcfInput1RequiredColumnIndex(final  DataTableSpec inSpec)
		throws InvalidSettingsException
		{
		int index =  findVcfInput1ColumnIndex(inSpec);
		if(index == -1 )
			{
			throw new InvalidSettingsException("Node "+this.getClass().getName()+": cannot find column title= \"VCF path\"");
			}

		return index;
		}

	
	/** VCF path *************************************************************/


	final static String CONFIG_VCFINPUT2 = "CONFIG_VCFINPUT2";
	final static String DEFAULT_VCFINPUT2 = "COLUMN";

	
	private org.knime.core.node.defaultnodesettings.SettingsModelColumnName m_col2 = 
		new  org.knime.core.node.defaultnodesettings.SettingsModelColumnName(
			CONFIG_VCFINPUT2,
			DEFAULT_VCFINPUT2
			);

	protected org.knime.core.node.defaultnodesettings.SettingsModelColumnName getPropertyVcfInput2Settings()
		{
		return this.m_col2;
		}
	
	protected String getPropertyVcfInput2Value()
		{
		return getPropertyVcfInput2Settings().getStringValue();
		}

	protected int findVcfInput2ColumnIndex(final  DataTableSpec inSpec)
		{
		int index= inSpec.findColumnIndex(getPropertyVcfInput2Value());
		
		return index;
		}
	
	protected int findVcfInput2RequiredColumnIndex(final  DataTableSpec inSpec)
		throws InvalidSettingsException
		{
		int index =  findVcfInput2ColumnIndex(inSpec);
		if(index == -1 )
			{
			throw new InvalidSettingsException("Node "+this.getClass().getName()+": cannot find column title= \"VCF path\"");
			}

		return index;
		}
	/* inverse property */
	final static String CONFIG_INVERSE = "CONFIG_INVERSE";
	final static boolean DEFAULT_INVERSE = false;

	private org.knime.core.node.defaultnodesettings.SettingsModelBoolean m_inverse = 
		new  org.knime.core.node.defaultnodesettings.SettingsModelBoolean(
			CONFIG_INVERSE,
			DEFAULT_INVERSE
			);
		
	protected org.knime.core.node.defaultnodesettings.SettingsModelBoolean getPropertyInverseSettings()
		{
		return this.m_inverse;
		}
	
	protected boolean isPropertyInverseValue()
		{
		return getPropertyInverseSettings().getBooleanValue();
		}

	/* alt match */
	final static String CONFIG_ALT_IN_DB = "ALT_IN_DB";
	final static boolean DEFAULT_ALT_IN_DB = false;

	private org.knime.core.node.defaultnodesettings.SettingsModelBoolean m_altInIndex = 
		new  org.knime.core.node.defaultnodesettings.SettingsModelBoolean(
			CONFIG_ALT_IN_DB,
			DEFAULT_ALT_IN_DB
			);
		
	protected org.knime.core.node.defaultnodesettings.SettingsModelBoolean getPropertyAltInIndexSettings()
		{
		return this.m_altInIndex;
		}
	
	protected boolean isPropertyAltInIndexValue()
		{
		return getPropertyAltInIndexSettings().getBooleanValue();
		}
	
	
	/* Peek Info Fields */
	final static String CONFIG_PEEK_INFO = "PEEK_INFO";
	final static String DEFAULT_PEEK_INFO = "";

	private org.knime.core.node.defaultnodesettings.SettingsModelString m_peekInfo = 
		new  org.knime.core.node.defaultnodesettings.SettingsModelString(
				CONFIG_PEEK_INFO,
				DEFAULT_PEEK_INFO
			);
		
	protected org.knime.core.node.defaultnodesettings.SettingsModelString getPropertyPeekInfoSettings()
		{
		return this.m_peekInfo;
		}
	
	protected String getPropertyPeekInfoValue()
		{
		return getPropertyPeekInfoSettings().getStringValue();
		}
	

	
	
	@Override
	protected String getSettingsModelSummary()
		{
		StringBuilder b=new StringBuilder();
		
		b.append("vcf1").append("=").append(String.valueOf(getPropertyVcfInput1Value()));
		b.append(" vcf2").append("=").append(String.valueOf(getPropertyVcfInput2Value()));
		b.append(" inverse").append("=").append(String.valueOf(isPropertyInverseValue()));
		b.append(" altInIndex").append("=").append(String.valueOf(isPropertyAltInIndexValue()));
		b.append(" peekinfo").append("=").append(String.valueOf(getPropertyPeekInfoValue()));
		
		return b.toString();
		}

	
	public VcfJoinNodeModel()
		{
		/* super(inport,outport) */
		super(2,1);
		}
	
	
	@Override 
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException
		{
    	if(inSpecs==null || inSpecs.length!= 1)
			{
			throw new InvalidSettingsException("Expected  1 tables");
			}
    	DataTableSpec datatablespecs[] = new DataTableSpec[1];
    	
    	datatablespecs[0] = 
    		configureOutDataTableSpec(0,inSpecs);
    		
    	return datatablespecs;
    	}

	
	/* @inheritDoc */
	@Override
    protected DataTableSpec createOutDataTableSpec(int index)
    	{
    	switch(index)
    		{
    		case 0 : return createOutTableSpec0();
    		
    		default: throw new IllegalStateException();
    		}
    	}
    /** configure output port for known-index for known inSpecs */	
	protected DataTableSpec configureOutDataTableSpec(int index,DataTableSpec[] inSpecs) throws InvalidSettingsException
		{
		switch(index)
    		{
    		case 0 : return configureOutTableSpec0(inSpecs);
    		
    		default: throw new IllegalStateException();
    		}
		}
	
	protected DataCell[] createDataCellsForOutTableSpec0(
			CharSequence  vcfOut,java.lang.Integer  countVariants
			)
			{
			DataCell __cells[]=new DataCell[2];
			__cells[0 ] = ( vcfOut !=null? new StringCell( vcfOut.toString() ) :  DataType.getMissingCell() ) ;
			__cells[1 ] =  ( countVariants !=null? new IntCell( countVariants ) :  DataType.getMissingCell() );
			
			return __cells;
			}
		
		
		/** create DataTableSpec for outport '0' outpout */
		protected DataTableSpec createOutTableSpec0()
			{
			
			DataColumnSpec colspecs[]=new DataColumnSpec[ 2 ];
			
			colspecs[ 0 ] = new org.knime.core.data.DataColumnSpecCreator("vcfOut",org.knime.core.data.DataType.getType(org.knime.core.data.def.StringCell.class)).createSpec();
			
			colspecs[ 1 ] = new org.knime.core.data.DataColumnSpecCreator("countVariants",org.knime.core.data.DataType.getType(org.knime.core.data.def.IntCell.class)).createSpec();
			
	    	return new DataTableSpec(colspecs);
			
			
			}
		/** create DataTableSpec for outport '0' outpout for known DataTableSpec */
		protected DataTableSpec configureOutTableSpec0(DataTableSpec[] inSpecs) throws InvalidSettingsException
			{
			return this.createOutTableSpec0();
			}	
		
		/* @inheritDoc */
		@Override
	    protected java.util.List<SettingsModel> fillSettingsModel(java.util.List<SettingsModel> list) {
	    	list = super.fillSettingsModel(list);
	    	
	    	list.add(this.getPropertyVcfInput1Settings());
	    	list.add(this.getPropertyVcfInput2Settings());
	    	list.add(this.getPropertyInverseSettings());
	    	list.add(this.getPropertyAltInIndexSettings());
	    	list.add(this.getPropertyPeekInfoSettings());
	    	return list;
	    	}

	
	
	/* @inheritDoc */
	@SuppressWarnings("resource")
	@Override
	protected BufferedDataTable[] execute(
			BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception
		{
		if(inData.length!=2)
			{
			throw new RuntimeException("Boum");
			}
		CloseableIterator<VariantContext> iter2=null;
		VariantContextWriter w=null;
		VcfIterator in=null;
        org.knime.core.data.container.CloseableRowIterator iter=null;
        BufferedDataTable inTable1=inData[0];
        BufferedDataTable inTable2=inData[1];
        DataTableSpec dataOutSpec = this.createOutTableSpec0();
        BufferedDataContainer out_container = null;
        int inUri1Index = this.findVcfInput1RequiredColumnIndex(inTable1.getDataTableSpec());
        int inUri2Index = this.findVcfInput1RequiredColumnIndex(inTable2.getDataTableSpec());
        String resource2=null;
        IndexedVcfFileReader indexedVcfFileReader=null;
        Set<String> infoToPeek = new HashSet<>(Arrays.asList(this.getPropertyPeekInfoValue().split("[\n ,\t;]+")));
		try {
			infoToPeek.remove("");
			/* find resource 2 */
				
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
		            	throw new RuntimeException("port n°2: found two VCF but expected one:\n"+
		            		uri+"\n"+resource2	
		            		);
		            	}	
		            resource2=uri;
	                }
		     CloserUtil.close( iter);
		     if(resource2==null)
		     	{
	            throw new RuntimeException("port n°2: input resource not found");
		     	}
		    getLogger().info("opening "+resource2);
		    indexedVcfFileReader = new IndexedVcfFileReader(resource2);
		    
			out_container = exec.createDataContainer(dataOutSpec);
            int nRows=0;
            double total=inTable1.getRowCount();
            iter = inTable1.iterator();
	        while(iter.hasNext())
	                {
	                DataRow row=iter.next();
	                ++nRows;
	                DataCell cell =row.getCell(inUri1Index);

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
	                
	                /* create output file */
	                java.io.File fileout = this.createFileForWriting(uri,".vcf.gz" );
					/* create parent directory if it doesn't exist */
	                if(fileout.getParentFile()!=null)
	                	{
	                	fileout.getParentFile().mkdirs();
	                	}
	                in = VCFUtils.createVcfIterator(uri);
	                w = VCFUtils.createVariantContextWriter(fileout);
	                VCFHeader header=in.getHeader();
	                VCFHeader h2=new VCFHeader(header);
            		h2.addMetaDataLine(new VCFHeaderLine(getClass().getSimpleName()+"HtsJdkVersion",HtsjdkVersion.getVersion()));
            		h2.addMetaDataLine(new VCFHeaderLine(getClass().getSimpleName()+"HtsJdkHome",HtsjdkVersion.getHome()));
            		for(String info:infoToPeek)
            			{
            			if(info.isEmpty()) continue;
            			VCFInfoHeaderLine h=indexedVcfFileReader.getHeader().getInfoHeaderLine(info);
            			if(h2.getInfoHeaderLine(info)!=null)
            				{
            				throw new RuntimeException("Cannot insert tag "+info+" because it already exists in "+uri);
            				}
            			if(h!=null) h2.addMetaDataLine(h);
            			}
	                
	                SAMSequenceDictionaryProgress progress=new SAMSequenceDictionaryProgress(header);
	                w.writeHeader(h2);
	                int count=0;
	                while(in.hasNext())
	                	{
	                	VariantContext ctx = progress.watch(in.next());
	                	boolean keep=false;
	                	
	                	iter2 = indexedVcfFileReader.iterator(
	                			ctx.getChr(),
	                			Math.max(0, ctx.getStart()-1),
	                			(ctx.getEnd()+1)
	                			);
	                	Map<String,Object> atts=new HashMap<>();
	                	while(iter2.hasNext())
	                		{
	                		VariantContext ctx2=iter2.next();
	                		if(!ctx.getChr().equals(ctx2.getChr())) continue;
	                		if(ctx.getStart()!=ctx2.getStart()) continue;
	                		if(!ctx.getReference().equals(ctx2.getReference())) continue;
	                		
	                		if(isPropertyAltInIndexValue())
	                			{
	                			boolean found_all_alt=true;
	                			for(Allele a1:ctx.getAlternateAlleles())
	                				{
	                				if(!ctx2.hasAlternateAllele(a1))
	                					{
	                					found_all_alt=false;
	                					break;
	                					}
	                				}
	                			if(!found_all_alt) continue;
	                			}
	                		keep=true;
	                		for(String info:infoToPeek)
		            			{
		            			if(info.isEmpty()) continue;
		            			Object o= ctx2.getAttribute(info);
		            			if(o!=null) atts.put(info, o);
		            			}
	                		}
	                	CloserUtil.close(iter2); iter2=null;
	                	if(isPropertyInverseValue()) keep=!keep;
	                	
	                	if(keep)
	                		{
	                		VariantContextBuilder vcb=new VariantContextBuilder(ctx);
	                		for(String key: atts.keySet())
	                			{
	                			vcb.attribute(key, atts.get(key));
	                			}
	                		w.add(vcb.make());
	                		++count;
	                		}
	                	}
	                w.close();
	                in.close();
	                
					out_container.addRowToTable(new DefaultRow(
							RowKey.createRowKey(nRows),
							createDataCellsForOutTableSpec0(
									fileout.getPath(),
									count
									)
							));

	                exec.checkCanceled();
	                exec.setProgress(nRows/total);
	                }
	        	iter.close();
	        	iter=null;
	        	out_container.close();
	            BufferedDataTable out0 = out_container.getTable();
	            out_container=null;
	            return this.internalTables(new BufferedDataTable[]{out0});
	            }
	        finally
	            {
	        	htsjdk.samtools.util.CloserUtil.close(iter);
	        	htsjdk.samtools.util.CloserUtil.close(out_container);
	        	htsjdk.samtools.util.CloserUtil.close(iter2);
	        	CloserUtil.close(indexedVcfFileReader);
	        	CloserUtil.close(w);
	        	CloserUtil.close(in);
	            }
	        }

		/** END user code/body */

	
	}
