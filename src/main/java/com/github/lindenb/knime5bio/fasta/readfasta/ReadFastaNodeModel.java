package com.github.lindenb.knime5bio.fasta.readfasta;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.slf4j.LoggerFactory;

public class ReadFastaNodeModel extends AbstractReadFastaNodeModel {
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception
        {
        final int limit = super.__maxSequences.getIntValue();
        final String url = super.__fastaFile.getStringValue();
        boolean to_upper = this.__toUpper.getBooleanValue();
        getLogger().info("reading "+url);
        java.io.BufferedReader r= null;
        long n_sequences = 0;
        try
            {
            r = this.openUriForBufferedReader(url);

            final DataTableSpec dataspec0 = this.createOutTableSpec0(inData);
            final BufferedDataContainer container0 = exec.createDataContainer(dataspec0);

            String seqname="";
            StringBuilder sequence=new StringBuilder();
            for(;;)
                {
                exec.checkCanceled();
                exec.setMessage("Sequences "+n_sequences);
                String line= r.readLine();
                if(line==null || line.startsWith(">"))
                    {
                    if(!(sequence.length()==0 && seqname.trim().isEmpty()))
                        {
						  container0.addRowToTable(new  org.knime.core.data.def.DefaultRow(
						  org.knime.core.data.RowKey.createRowKey(n_sequences),
						this.createDataCellsForOutTableSpec0(seqname,sequence) ));
                        ++n_sequences;
                        }
                    if(line==null) break;
                    if(limit==1+n_sequences) break;
                    seqname=line.substring(1);
                    sequence=new StringBuilder();
                    }
                else
                    {
                    line= line.trim();
                    if( to_upper ) line= line.toUpperCase();
                    sequence.append(line);
                    }
                }
            container0.close();
            BufferedDataTable out0 = container0.getTable();
            return new BufferedDataTable[]{out0};
            }
        finally
            {
            r.close();
            }
        }

}
