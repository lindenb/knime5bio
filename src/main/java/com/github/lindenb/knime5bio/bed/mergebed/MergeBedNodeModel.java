package com.github.lindenb.knime5bio.bed.mergebed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Optional;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.knime5bio.LogRowIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.samtools.util.Interval;
import htsjdk.samtools.util.IntervalTreeMap;
import htsjdk.tribble.bed.BEDCodec;
import htsjdk.tribble.bed.BEDFeature;

public class MergeBedNodeModel extends AbstractMergeBedNodeModel {
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable inData, final ExecutionContext exec) throws Exception
        {
    	final int bedColumn = super.findColumnIndexByName(inData,super.__BED);
    	BufferedDataContainer container0 = null;
        LogRowIterator iter =null;
        BufferedReader in=null;
        final boolean mergeAllTogether = super.__mergeAllTogether.getBooleanValue();
        final IntervalTreeMap<Interval> intervals = new IntervalTreeMap<>();
        try
            {
        	super.assureNodeWorkingDirectoryExists();
			container0 = exec.createDataContainer(super.createOutTableSpec0());

        	
        	iter =  new LogRowIterator("Merge",inData, exec);
        	while(iter.hasNext())
        		{
				final DataRow row = iter.next();
				final DataCell cell = row.getCell(bedColumn);
				if(!(cell instanceof StringCell))
					throw new InvalidSettingsException("not a string cell");
				if (cell.isMissing())
					continue;
				final File inFile = new File(StringCell.class.cast(cell).getStringValue());
				if (!inFile.exists())
					throw new FileNotFoundException("cannot find " + inFile);
				if (!inFile.isFile())
					throw new FileNotFoundException("not a file: " + inFile);
				final BEDCodec codec = new BEDCodec();
				in = IOUtils.openFileForBufferedReading(inFile);
				
				
				
				String line;
				while((line=in.readLine())!=null)
					{
					final BEDFeature feat = codec.decode(line);
					if(feat==null) continue;
					boolean inserted=false;
					Interval interval = new Interval(feat.getContig(), feat.getStart(), feat.getEnd());//feat use 1-based 

					while(!inserted)
						{
						inserted=true;
						
						/* we need to extends by one base, to merge consecutive, but not overlaopping intervals. e.g: [1,9] and [10,20] */
						final Interval intervalx1 = new Interval(
								interval.getContig(),
								Math.max(1, feat.getStart()-1),
								feat.getEnd()+1);

						
						Collection<Interval> overlapping = intervals.getOverlapping(intervalx1);
						if(overlapping!=null && !overlapping.isEmpty())
							{
							inserted=false;
							Interval first = overlapping.iterator().next();
							intervals.remove(first);
							interval = new Interval(
									interval.getContig(),
									Math.min(interval.getStart(),first.getStart()),
									Math.max(interval.getEnd(),first.getEnd())
									);
							}
						}
					intervals.put(interval, interval);
					}
		        in.close();in=null;
		        
		        if(!mergeAllTogether)
		        	{
					final File outFile =dump(intervals);
					container0.addRowToTable(new DefaultRow(
							row.getKey(),
							super.createDataCellsForOutTableSpec0(outFile.getPath()))
							);
					intervals.clear();
		        	}
		        
        		}
        	iter.close();iter=null;
        	
        	 if(mergeAllTogether)
	        	 {
				final File outFile =dump(intervals);
	
				container0.addRowToTable(new DefaultRow(
						RowKey.createRowKey(1L),
						super.createDataCellsForOutTableSpec0(outFile.getPath()))
						);
	        	 }
			container0.close();
            BufferedDataTable out0 = container0.getTable();
            container0=null;
            return new BufferedDataTable[]{out0};
            }
        finally
            {
          	CloserUtil.close(in);
          	CloserUtil.close(iter);
          	CloserUtil.close(container0);
            }
        }
    private File dump( final IntervalTreeMap<Interval> intervals) throws IOException
    	{
    	PrintWriter pw=null;
    	try {
			final File outFile = super.createFileForWriting(Optional.of("merge"), ".bed.gz");
			pw = IOUtils.openFileForPrintWriter(outFile);
			for(Interval interval:intervals.keySet())
				{
				pw.print(interval.getContig());
				pw.print("\t");
				pw.print(interval.getStart()-1);//interval is 1-based
				pw.print("\t");
				pw.print(interval.getEnd());
				pw.println();
				}
			pw.flush();
			pw.close();pw=null;
			return outFile;
    		}
    	finally
    		{
    		CloserUtil.close(pw);
    		}
    	}
}
