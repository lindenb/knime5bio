package com.github.lindenb.knime5bio.bio.vcf.vcfintersectbed;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.util.htsjdk.HtsjdkVersion;
import com.github.lindenb.jvarkit.util.picard.SAMSequenceDictionaryProgress;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

public class VcfintersectbedNodeModel
	extends AbstractVcfintersectbedNodeModel
	{

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
        BufferedDataTable vcfTable=inData[0];
        BufferedDataTable bedTable=inData[1];
        DataTableSpec dataOutSpec = this.createOutTableSpec0();
        BufferedDataContainer out_container = null;
        int inVcfIndex = this.findVcfRequiredColumnIndex(vcfTable.getDataTableSpec());
        int inBedIndex = this.findBedColumnIndex(bedTable.getDataTableSpec());
        boolean inverse = this.isPropertyInverseValue();
       
        IntervalTreeMap<Boolean> bedMap = new IntervalTreeMap<Boolean>();
        BufferedReader bedIn=null;
		try {
			
			/* find BED */
			String bedFile = super.getOneRequiredResource(bedTable, inBedIndex);
			
		    getLogger().info("opening "+bedFile);
		    bedIn = IOUtils.openURIForBufferedReading(bedFile);
		    String line;
		    final Pattern tab=Pattern.compile("[\t]");
		    while((line=bedIn.readLine())!=null)
		    	{
		    	if(line.startsWith("track")) continue;
		    	if(line.startsWith("browser")) continue;
		    	if(line.trim().isEmpty()) continue;
		    	String tokens[] = tab.split(line,4);
		    	if(tokens.length<3) throw new IOException("Bad bed line in "+line);
		    	try {
					int start = Integer.parseInt(tokens[1]);
					int end = Integer.parseInt(tokens[2]);
					if(start<0 || start>end)  throw new IOException("Bad bed line in "+line);
					if(start==end) continue;
					start++;//htsjdk use 1-based data
					bedMap.put(
							new Interval(tokens[0], start, end),
							Boolean.TRUE
							);
					}
		    	catch (Exception e) {
					throw new IOException("Bad bed line in "+line);
					}
		    	}
		    CloserUtil.close(bedIn);bedIn=null;
		    
		    
			out_container = exec.createDataContainer(dataOutSpec);
            int nRows=0;
            double total = vcfTable.getRowCount();
            iter = vcfTable.iterator();
	        while(iter.hasNext())
	                {
	                DataRow row=iter.next();
	                ++nRows;
	                DataCell cell =row.getCell(inVcfIndex);

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

	                	Interval interval = new Interval(
            					ctx.getContig(),
            					ctx.getStart(),
            					ctx.getEnd())
            				;
	                	
	                	boolean found = bedMap.containsOverlapping(interval);
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
	            }
	        }
	}
