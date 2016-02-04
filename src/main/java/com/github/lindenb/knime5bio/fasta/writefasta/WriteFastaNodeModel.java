package com.github.lindenb.knime5bio.fasta.writefasta;

import java.io.PrintWriter;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.knime5bio.LogRowIterator;

import htsjdk.samtools.util.CloserUtil;

public class WriteFastaNodeModel extends AbstractWriteFastaNodeModel{
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable inData, final ExecutionContext exec) throws Exception
        {
    	LogRowIterator iter=null;
        int fold = super.__fold.getIntValue();
        int tIndex = super.findColumnIndexByName(inData,super.__title);
        int sIndex = super.findColumnIndexByName(inData,super.__sequence);
        PrintWriter w =null;
        try
            {
            w= IOUtils.openFileForPrintWriter(super.getSettingsModelOutputfileFile());
            iter= new LogRowIterator("Fasta",inData, exec);
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
                }
            iter.close();iter=null;
            w.flush();
            return NO_BUFFERED_TABLE;
            }
        finally
            {
            CloserUtil.close(iter);
            CloserUtil.close(w);
            }
        }
}
