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
package com.github.lindenb.knime5bio.bio.vcf.vcfheaders;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.vcf.VCFFilterHeaderLine;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFInfoHeaderLine;


import org.knime.core.node.*;
import org.knime.core.data.*;
import org.knime.core.data.def.*;

import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.bio.vcf.vcfheaders.AbstractVcfheadersNodeModel;

/** BEGIN user imports */

/** END user imports */


/**
 * VcfSamplesNodeModel
 * @author Pierre Lindenbaum
 */
@javax.annotation.Generated("xslt-sandbox/knime2java")
public class VcfheadersNodeModel
	extends AbstractVcfheadersNodeModel
	{
	public VcfheadersNodeModel()
		{
		}
	

	
  	
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception
		{
        CloseableIterator<String> iter=null;
        BufferedDataTable inTable=inData[0];
        BufferedDataContainer out_container[]=new BufferedDataContainer[3];
        VcfIterator vcfIiterator=null;
        int inUriIndex = findVcfInputRequiredColumnIndex(inTable.getDataTableSpec());//TODO
        int id_generator=0;
		try {
			out_container[0]= exec.createDataContainer(createOutTableSpec0());
			out_container[1]= exec.createDataContainer(createOutTableSpec1());
			out_container[2]= exec.createDataContainer(createOutTableSpec1());
			
			int nRows=0;
            double total=inTable.getRowCount();
            iter=stringColumnIterator(inTable, inUriIndex);
	        while(iter.hasNext())
	                {
	                String uri = iter.next();
	                vcfIiterator = VCFUtils.createVcfIterator(uri);
	                VCFHeader header=vcfIiterator.getHeader();
	                for(VCFFilterHeaderLine h:header.getFilterLines())
	                	{
	                	out_container[0].addRowToTable(new DefaultRow(
	    	                	RowKey.createRowKey(++id_generator),
	    	                	createDataCellsForOutTableSpec0(
	    	                			uri,
	    	                			h.getID(),
	    	                			h.getValue()
	    	                			)
	    	                	));
	                	}
	                
	                for(VCFInfoHeaderLine h:header.getInfoHeaderLines())
	                	{
            			  out_container[1].addRowToTable(new DefaultRow(
    	                	RowKey.createRowKey(++id_generator),
    	                	createDataCellsForOutTableSpec1(
    	                			uri,
    	                			h.getID(),
    	                			(h.getCountType()==VCFHeaderLineCount.INTEGER?h.getCount():null),
    	                			String.valueOf(h.getCountType()),
    	                			String.valueOf(h.getType()),
    	                			h.getDescription()
    	                			)
    	                	));
	                	}
	                
	                for(VCFFormatHeaderLine h:header.getFormatHeaderLines())
	                	{
	                	out_container[2].addRowToTable(new DefaultRow(
	    	                	RowKey.createRowKey(++id_generator),
	    	                	createDataCellsForOutTableSpec1(
	    	                			uri,
	    	                			h.getID(),
	    	                			(h.getCountType()==VCFHeaderLineCount.INTEGER?h.getCount():null),
	    	                			String.valueOf(h.getCountType()),
	    	                			String.valueOf(h.getType()),
	    	                			h.getDescription()
	    	                			)
	    	                	));
	                	}

	                
	                
	                vcfIiterator.close();
	                vcfIiterator=null;
	                
	                exec.checkCanceled();
	                exec.setProgress(nRows/total);
	                }
	        	iter.close();
	        	iter=null;
	        	BufferedDataTable outs[]=new BufferedDataTable[]{null,null,null};
	        	for(int i=0;i<3;++i)
	        		{
	        		out_container[i].close();
	        		outs[i] = out_container[i].getTable();
	        		out_container[i]=null;
	        		}
	            return outs;
	            }
	        finally
	            {
	        	CloserUtil.close(iter);
	        	CloserUtil.close(vcfIiterator);
	        	if(out_container!=null)
	        		for(int i=0;i< out_container.length;++i)
	        			{
	        			CloserUtil.close(out_container[i]);
	        			}
	            }
	        }
	
	}
