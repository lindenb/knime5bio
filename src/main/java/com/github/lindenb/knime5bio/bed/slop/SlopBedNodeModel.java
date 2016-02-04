package com.github.lindenb.knime5bio.bed.slop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
import com.github.lindenb.knime5bio.LogRowIterator;

import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import htsjdk.samtools.reference.ReferenceSequenceFileFactory;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.tribble.bed.BEDCodec;
import htsjdk.tribble.bed.BEDFeature;

public class SlopBedNodeModel extends AbstractSlopBedNodeModel {
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception
        {
    	final int extend = super.__extendBases.getIntValue();
    	final int bedColumn = super.findColumnIndexByName(inData[0],super.__BED);
    	BufferedDataContainer container0 = null;
        LogRowIterator iter =null;
        BufferedReader in=null;
        PrintWriter pw=null;
        ReferenceSequenceFile refFile=null;
        try
            {
        	//get dict
        	refFile = ReferenceSequenceFileFactory.getReferenceSequenceFile(	super.getSettingsModelReferenceGenomeFile());
        	final SAMSequenceDictionary dict = refFile.getSequenceDictionary();
        	refFile.close();
        	refFile=null;
        	if(dict==null) throw new IOException("Cannot get dict for reference sequence");
        	
			container0 = exec.createDataContainer(super.createOutTableSpec0());

        	iter =  new LogRowIterator("Slop",inData[0], exec);
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
				final File outFile = super.createFileForWriting(Optional.of("slop"), ".bed.gz");
				pw = IOUtils.openFileForPrintWriter(outFile);
				in = IOUtils.openFileForBufferedReading(inFile);
				String line;
				while((line=in.readLine())!=null)
					{
					final BEDFeature feat = codec.decode(line);
					if(feat==null) continue;
					final SAMSequenceRecord ssr = dict.getSequence(feat.getContig());
					if(ssr==null) throw new IOException("Unknown sequence in "+line);
					
					int start = Math.min(ssr.getSequenceLength(),Math.max(1,feat.getStart() - extend));
					
					int end =  Math.max(1,Math.min(ssr.getSequenceLength(),feat.getEnd() + extend));
					if(start> end) continue;
					pw.print(ssr.getSequenceName());
					pw.print("\t");
					pw.print(start - 1 );//interval is 1-based
					pw.print("\t");
					pw.print(end);
					pw.println();
					
					}
		        in.close();in=null;
		        pw.flush();
				pw.close();pw=null;
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
          	CloserUtil.close(refFile);
          	CloserUtil.close(in);
          	CloserUtil.close(iter);
          	CloserUtil.close(container0);
            }
        }

}
