package com.github.lindenb.knime5bio.bio.vcf.vcfjoin;
import org.knime.core.node.*;
import org.knime.core.data.*;
import org.knime.core.data.def.*;

import com.github.lindenb.jvarkit.util.htsjdk.HtsjdkVersion;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;
import com.github.lindenb.jvarkit.util.vcf.IndexedVcfFileReader;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

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
	extends AbstractVcfJoinNodeModel
	{	

	public VcfJoinNodeModel()
		{
		/* super(inport,outport) */
		super();
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
		     CloserUtil.close( iter);iter=null;
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
	                /* open streams */
	                in = VCFUtils.createVcfIterator(uri);
	                w = VCFUtils.createVariantContextWriter(fileout);
	                /* create header */
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
	                /* loop over variants */
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
	                w.close();w=null;
	                in.close();in=null;
	                
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


	
	}
