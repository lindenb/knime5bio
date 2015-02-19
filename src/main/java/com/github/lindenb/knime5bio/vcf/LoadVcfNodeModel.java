/*


The MIT License (MIT)

Copyright (c) 2014 Pierre Lindenbaum

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


*/
package com.github.lindenb.knime5bio.vcf;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFEncoder;
import htsjdk.variant.vcf.VCFHeader;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.knime.core.node.*;
import org.knime.core.data.*;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.*;

import com.github.lindenb.jvarkit.tools.misc.VcfFilterSequenceOntology;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.NodeLoggerAdapter;

/** BEGIN user imports */

/** END user imports */


/**
 * VcfSamplesNodeModel
 * @author Pierre Lindenbaum
 */
public class LoadVcfNodeModel
	extends AbstractKnime5BioNodeModel
	{
	public LoadVcfNodeModel()
		{
		}
	

	
  	
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception
		{
		VcfIterator vcfIterator=null;
        CloseableRowIterator iter=null;
        BufferedDataTable inTable=inData[0];
        if(inTable.getRowCount()>1)
        	{
        	throw new RuntimeException("Input should contains only one VCF");
        	}
        try
        	{
        	 iter=inTable.iterator();
 	        while(iter.hasNext())
 	                {
 	                DataRow row=iter.next();
 	                DataCell cell =row.getCell(inUriIndex);
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

	                vcfIterator = VCFUtils.createVcfIterator(uri);
 	                VCFHeader header= vcfIterator.getHeader();
 	                DataTableSpec dataOutSpec=super.createDataTableSpecFromVcfHeader(header);
 	                out_container = exec.createDataContainer(dataOutSpec);
 	                VCFEncoder encoder=new VCFEncoder(header,
 	                		false,
 	                		false
 	                		);
 	                int nVariants=0;
 	                while(vcfIterator.hasNext())
 	                	{
 	                	VariantContext ctx=vcfIterator.next();
 	                	exec.checkCanceled();
 	                	++nVariants;
 	                	DataCell cells[]=createDataRowFromVariantContext(vcfEncoder,ctx);
 	                	out_container.addRowToTable(new DefaultRow(
	    	                	RowKey.createRowKey(++nVariants),
	    	                	cells
	    	                	));
 	                	}
 	                vcfIterator.close();
 	                vcfIterator=null;
 	               exec.checkCanceled();
 	                }
        	}
        catch(Exception err)
        	{
        	
        	}
        finally
        	{
        	CloserUtil.close(iter);
        	CloserUtil.close(out_container);
        	}
        DataTableSpec dataOutSpec = suer.createOutTableSpec0();
        BufferedDataContainer out_container = null;
        int inUriIndex = this.findVcfInputRequiredColumnIndex(inTable.getDataTableSpec());
        int id_generator=0;
		try {
			
            int nRows=0;
            double total=inTable.getRowCount();
            iter=inTable.iterator();
	        while(iter.hasNext())
	                {
	                DataRow row=iter.next();
	                ++nRows;
	                DataCell cell =row.getCell(inUriIndex);

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
	                VcfIterator vcfIiterator = VCFUtils.createVcfIterator(uri);
	                List<String> samples= vcfIiterator.getHeader().getSampleNamesInOrder();
	                
	                if(samples.isEmpty() && isPropertyFilterOutNoSampleValue())
	                	{
	                	//do nothing
	                	}	
	                else if(samples.isEmpty())
	                	{
	                	out_container.addRowToTable(new DefaultRow(
	    	                	RowKey.createRowKey(++id_generator),
	    	                	createDataCellsForOutTableSpec0(
	    	                			uri,
	    	                			null
	    	                			)
	    	                	));
	                	}
	                else
	                	{
	                	for(String sample:samples)
	                		{
		                	out_container.addRowToTable(new DefaultRow(
		    	                	RowKey.createRowKey(++id_generator),
		    	                	createDataCellsForOutTableSpec0(
		    	                			uri,
		    	                			sample
		    	                			)
		    	                	));
	                		}
	                	}
	                
	                vcfIiterator.close();
	                
	                exec.checkCanceled();
	                exec.setProgress(nRows/total);
	                }
	        	iter.close();
	        	iter=null;
	        	out_container.close();
	            BufferedDataTable out0 = out_container.getTable();
	            out_container=null;
	            return new BufferedDataTable[]{out0};
	            }
	        finally
	            {
	        	CloserUtil.close(iter);
	        	CloserUtil.close(out_container);
	            }
	        }
	
	}
