package com.github.lindenb.knime5bio.vcf.interbed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.LogRowIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.tribble.bed.BEDCodec;
import htsjdk.tribble.bed.BEDFeature;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;

public class InterBedNodeModel extends AbstractInterBedNodeModel {
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable  bedTable,final BufferedDataTable  vcftable, final ExecutionContext exec) throws Exception
        {
    	final int vcfColumn = super.findColumnIndexByName(bedTable,super.__VCF);
    	BufferedDataContainer container0 = null;
        LogRowIterator iter =null;
        BufferedReader in=null;
        IntervalTreeMap<Boolean> intervals = new IntervalTreeMap<>();
        VariantContextWriter vcw=null;
        VcfIterator vcfIn=null;
        
        try
            {
        	for(File bedFile:super.collectFilesInOneColumn(bedTable, super.__BED))
        		{
				final BEDCodec codec = new BEDCodec();
        		in = IOUtils.openFileForBufferedReading(bedFile);
				String line;
				while((line=in.readLine())!=null)
					{
					final BEDFeature feat = codec.decode(line);
					if(feat==null) continue;
					final Interval interval = new Interval(feat.getContig(), feat.getStart(), feat.getEnd());//feat use 1-based 
					intervals.put(interval, Boolean.TRUE);
					}
        		in.close();in=null;
        		}
			container0 = exec.createDataContainer(super.createOutTableSpec0());

        	
        	iter =  new LogRowIterator("Intersect",vcftable, exec);
        	while(iter.hasNext())
        		{
				final DataRow row = iter.next();
				final DataCell cell = row.getCell(vcfColumn);
				if(!(cell instanceof StringCell))
					throw new InvalidSettingsException("not a string cell");
				if (cell.isMissing())
					continue;
				final File inFile = new File(StringCell.class.cast(cell).getStringValue());
				if (!inFile.exists())
					throw new FileNotFoundException("cannot find " + inFile);
				if (!inFile.isFile())
					throw new FileNotFoundException("not a file: " + inFile);
				vcfIn = VCFUtils.createVcfIteratorFromFile(inFile);
				final File outFile = super.createFileForWriting(Optional.of("intersect"), ".vcf.gz");
				vcw =  VCFUtils.createVariantContextWriter(outFile);
				vcw.writeHeader(vcfIn.getHeader());
				
				while(vcfIn.hasNext())
					{
					final VariantContext ctx= vcfIn.next();
					boolean ininterval= intervals.containsOverlapping(new Interval(
							ctx.getContig(),
							ctx.getStart(),
							ctx.getEnd()
							));
					if(super.__inverse.getBooleanValue()) ininterval=!ininterval;
					
					if(ininterval)
						{
						vcw.add(ctx);
						}
					
					}
				vcw.close(); vcw=null;
				vcfIn.close();vcfIn=null;
				container0.addRowToTable(new DefaultRow(
						row.getKey(),
						super.createDataCellsForOutTableSpec0(outFile.getPath()))
						);
        		}
        	iter.close();iter=null;

			container0.close();
            BufferedDataTable out0 = container0.getTable();
            container0=null;
            return new BufferedDataTable[]{out0};
            }
        finally
            {
          	CloserUtil.close(vcfIn);
          	CloserUtil.close(vcw);
          	CloserUtil.close(in);
          	CloserUtil.close(iter);
          	CloserUtil.close(container0);
            }
        }

}
