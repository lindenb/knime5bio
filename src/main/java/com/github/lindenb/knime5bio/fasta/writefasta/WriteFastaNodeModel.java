package com.github.lindenb.knime5bio.fasta.writefasta;

import java.io.PrintWriter;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.io.IOUtils;

import htsjdk.samtools.util.CloserUtil;

public class WriteFastaNodeModel extends AbstractWriteFastaNodeModel{
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception
        {
        CloseableRowIterator iter=null;
        BufferedDataTable inTable=inData[0];
        int fold = super.__fold.getIntValue();
        int tIndex = super.findColumnIndexByName(inData[0],super.__title);
        int sIndex = super.findColumnIndexByName(inData[0],super.__sequence);
        PrintWriter w =null;
        try
            {
            w= IOUtils.openFileForPrintWriter(super.getSettingsModelOutputfileFile());
            int nRows=0;
            iter=inTable.iterator();
            while(iter.hasNext())
                {
                final DataRow row=iter.next();
                final DataCell tCell =row.getCell(tIndex);

                final DataCell sCell =row.getCell(sIndex);
                w.print(">");
                if(!tCell.isMissing())
                    { 
                    w.print(StringCell.class.cast(tCell).getStringValue());
                    }
                if(!sCell.isMissing())
                    {
                    String sequence = StringCell.class.cast(sCell).getStringValue();
                    for(int i=0;i < sequence.length();++i)
                        {
                        if(i%fold == 0) w.println();
                        w.print(sequence.charAt(i));
                        exec.checkCanceled();
                        }
                    }
                w.println();

                exec.checkCanceled();
                exec.setProgress("writing... "+nRows);
                ++nRows;
                }
            w.flush();
            return NO_BUFFERED_TABLE;
            }
        finally
            {
            CloserUtil.close(w);
            }
        }
}
