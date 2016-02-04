package com.github.lindenb.knime5bio.bed.intersectbed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

public class IntersectBedNodeModel extends AbstractIntersectBedNodeModel {
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception
        {
    	final int bedColumn = super.findColumnIndexByName(inData[0],super.__BED);
    	BufferedDataContainer container0 = null;
        LogRowIterator iter =null;
        BufferedReader in=null;
        PrintWriter pw=null;
        IntervalTreeMap<Interval> intervals = null;
        try
            {
        	iter =  new LogRowIterator("Intersect",inData[0], exec);
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
				
				
		        final IntervalTreeMap<Interval> intervalsNew = new IntervalTreeMap<>();

				final BEDCodec codec = new BEDCodec();
				in = IOUtils.openFileForBufferedReading(inFile);
				String line;
				while((line=in.readLine())!=null)
					{
					final BEDFeature feat = codec.decode(line);
					if(feat==null) continue;
					final Interval interval = new Interval(feat.getContig(), feat.getStart(), feat.getEnd());//feat use 1-based 
					if(intervals==null)//first  BED file
						{
						intervalsNew.put(interval, interval);
						}
					else
						{
						for(final Interval overlap : intervals.getOverlapping(interval))
							{
							final int chromStart = Math.max(overlap.getStart(), interval.getStart());
							final int chromEnd = Math.min(overlap.getEnd(), interval.getEnd());
							if(chromStart>chromEnd) continue; 
							final Interval i = new Interval(interval.getContig(), chromStart, chromEnd);
							intervalsNew.put(i, i);
							}
						}
					}
		        in.close();in=null;
		        intervals = intervalsNew;
        		}
        	iter.close();iter=null;
        	
        	
			final File outFile = super.createFileForWriting(Optional.of("merge"), ".bed.gz");
			pw = IOUtils.openFileForPrintWriter(outFile);
			if(intervals!=null){
			for(Interval interval:intervals.keySet())
				{
				pw.print(interval.getContig());
				pw.print("\t");
				pw.print(interval.getStart()-1);//interval is 1-based
				pw.print("\t");
				pw.print(interval.getEnd());
				pw.println();
				}
			}
			pw.flush();
			pw.close();pw=null;
			intervals.clear();

			container0 = exec.createDataContainer(super.createOutTableSpec0());
			container0.addRowToTable(new DefaultRow(
					RowKey.createRowKey(1L),
					super.createDataCellsForOutTableSpec0(outFile.getPath()))
					);
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

}
