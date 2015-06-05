package com.github.lindenb.knime5bio.bio.vcf.vcfintersectbed;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;

import java.io.BufferedReader;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.util.bio.bed.BedLine;
import com.github.lindenb.jvarkit.util.bio.bed.IndexedBedReader;
import com.github.lindenb.jvarkit.util.htsjdk.HtsjdkVersion;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

public class VcfintersectbedNodeModel
	extends AbstractVcfintersectbedNodeModel
	{

	/* @inheritDoc */
	@Override
	protected BufferedDataTable[] execute(
			BufferedDataTable[] inData,
			final ExecutionContext exec) throws Exception
		{
		if(inData.length!=2)
			{
			throw new RuntimeException("Boum");
			}
		CloseableIterator<BedLine> iter2=null;
		VariantContextWriter w=null;
		VcfIterator in=null;
       CloseableIterator<String> iter=null;
        BufferedDataTable vcfTable=inData[0];
        BufferedDataTable bedTable=inData[1];
        DataTableSpec dataOutSpec = this.createOutTableSpec0();
        BufferedDataContainer out_container = null;
        int inVcfIndex = this.findVcfRequiredColumnIndex(vcfTable.getDataTableSpec());
        int inBedIndex = this.findBedColumnIndex(bedTable.getDataTableSpec());
        boolean inverse = this.isPropertyInverseValue();
        IndexedBedReader indexedBed=null;
        BufferedReader bedIn=null;
		try {
			
			/* find BED */
			String bedFile = super.getOneRequiredResource(bedTable, inBedIndex);
			
			indexedBed = new IndexedBedReader(bedFile);
		    
		    
			out_container = exec.createDataContainer(dataOutSpec);
            int nRows=0;
            double total = vcfTable.getRowCount();
            iter=  stringColumnIterator(vcfTable, inVcfIndex);
            while(iter.hasNext())
	                {
	                String uri = iter.next();
	                
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
	                VCFHeader header = in.getHeader();
	                VCFHeader h2 = new VCFHeader(header);
            		h2.addMetaDataLine(new VCFHeaderLine(getClass().getSimpleName()+"HtsJdkVersion",HtsjdkVersion.getVersion()));
            		h2.addMetaDataLine(new VCFHeaderLine(getClass().getSimpleName()+"HtsJdkHome",HtsjdkVersion.getHome()));
            		
	                SAMSequenceDictionaryProgress progress=new SAMSequenceDictionaryProgress(header);
	                w.writeHeader(h2);
	                int count=0;
	                /* loop over variants */
	                while(in.hasNext())
	                	{
	                	exec.checkCanceled();
	                	VariantContext ctx = progress.watch(in.next());

	                	
	                	boolean found = false;
	                	iter2= indexedBed.iterator(
	                			ctx.getContig(),
	                			Math.max(0,ctx.getStart()-1),
	                			ctx.getEnd()+1
	                			);
	                	while(iter2.hasNext())
	                		{
	                		BedLine bed=iter2.next();
	                		if(!bed.getContig().equals(ctx.getContig())) continue;
	                		if(ctx.getEnd()-1< bed.getStart()) continue;
	                		if(bed.getEnd()<=ctx.getStart()) continue;
	                		found=true;
	                		break;
	                		}
	                	CloserUtil.close(iter2);iter2=null;
	                	
	                	if(inverse) found = !found;
	                	if(!found) continue;
	                	w.add(ctx);
	                	++count;
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
	            return internalTables(this.internalTables(new BufferedDataTable[]{out0}));
	            }
	        finally
	            {
	        	htsjdk.samtools.util.CloserUtil.close(iter);
	        	htsjdk.samtools.util.CloserUtil.close(out_container);
	        	htsjdk.samtools.util.CloserUtil.close(iter2);
	        	CloserUtil.close(bedIn);
	        	CloserUtil.close(w);
	        	CloserUtil.close(in);
	        	CloserUtil.close(indexedBed);
	            }
	        }
	}
