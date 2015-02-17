package com.github.knime5bio.vcf;
import org.knime.core.node.*;
import org.knime.core.data.*;
import org.knime.core.data.def.*;
import java.io.File;
import htsjdk.variant.vcf.VCFFileReader;

public abstract class AbstractVcfFilterNodeModel
	extends com.github.lindenb.knime5bio.AbstractNodeModel
	{
	protected abstract String getPropertyVcfInValue();
	protected abstract int findVcfInputRequiredColumnIndex(final  DataTableSpec inSpec) throws InvalidSettingsException;
	
	protected AbstractVcfFilterNodeModel(int inport,int outport)
		{
		/* super(inport,outport) */
		super(inport,outport);
		}
	
	protected VCFFileReader openVcfInput(final  DataTableSpec inSpec) throws java.io.IOException
		{
		File file = new File( getPropertyVcfInValue() );
		return new VCFFileReader(file);
		}
    
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception
        {
        CloseableIterator<VariantContext> iter=null;
        VCFFileReader vcfFileReader=null;
        CloseableRowIterator iter=null;
        BufferedDataTable inTable=inData[0];
        getLogger().info("reading ");
        try		{
				int uriindex = this.findVcfInputRequiredColumnIndex(inTable.getDataTableSpec());
				DataTableSpec dataspec0 = this.createOutTableSpec0();
            	BufferedDataContainer container0 = exec.createDataContainer(dataspec0);
				int nRows=0;
	            double total=inTable.getRowCount();
		        iter=inTable.iterator();
		        while(iter.hasNext())
		            {
		            DataRow row=iter.next();
		            DataCell cell =row.getCell(uriindex);
		            if(!cell.isMissing()) continue;
		            String filename = StringCell.class.cast(cell).getStringValue();
					File file = new File(filename);
		            vcfFileReader = new VCFFileReader(file); 
		            CloseableIterator<VariantContext> iter=vcfFileReader.iterator() ;
		             VariantContextWriterBuilder builder = new VariantContextWriterBuilder()
		            while(iter.hasNext())
		            	{
		            	
		            	}
		            exec.checkCanceled();
		            exec.setProgress(nRows/total,"Running flter");
		            ++nRows;
					}
				container0.close();
		        BufferedDataTable out0 = container0.getTable();
		        return new BufferedDataTable[]{out0};
               	}
        finally
            {
            CloserUtil(container0);
            CloserUtil(iter);
            }
        }


	}
