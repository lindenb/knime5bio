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
package com.github.lindenb.knime5bio.bio.vcf.vcfconcat;
import htsjdk.samtools.util.CloserUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.*;
import org.knime.core.data.*;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.*;

import com.github.lindenb.jvarkit.tools.vcfconcat.VcfConcat;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;


/**
 * VcfconcatNodeModel
 * @author Pierre Lindenbaum
 */
public class VcfconcatNodeModel
	extends AbstractVcfconcatNodeModel
	{
	public VcfconcatNodeModel()
		{
		}
	/* @inheritDoc */
	@Override
    protected BufferedDataTable[] execute(
    		final BufferedDataTable[] inData,
            final ExecutionContext exec
            ) throws Exception
		{
		Map<String,Set<String>> sample2list = new HashMap<String, Set<String>>();
        CloseableRowIterator iter=null;
        BufferedDataTable inTable=inData[0];
        BufferedDataContainer out_container = null;
        int inUriIndex = this.findVcfInputRequiredColumnIndex(inTable.getDataTableSpec());
        int id_generator=0;
        VcfIterator  vcfIiterator=null;
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
                vcfIiterator = VCFUtils.createVcfIterator(uri);
                List<String> samples= vcfIiterator.getHeader().getSampleNamesInOrder();
                vcfIiterator.close();
                vcfIiterator=null;
                if(samples.size()>1)
                	{
                	throw new RuntimeException("file "+uri+" shouldn't contain more than one sample. got: "+samples);
                	}
                
                String sampleName=samples.isEmpty()?"__VOID__":samples.get(0);
                Set<String> uris=sample2list.get(sampleName);
                if(uris==null)
                	{
                	uris=new HashSet<>();
                	sample2list.put(sampleName,uris);
                	}
                uris.add(uri);
                exec.checkCanceled();
                exec.setProgress(nRows/total);
                }
        	iter.close();
        	iter=null;

	        DataTableSpec dataOutSpec = this.createOutTableSpec0();
        	out_container = exec.createDataContainer(dataOutSpec);
        	for(String sampleName: sample2list.keySet())
        		{
        		List<String> vcfFiles=new ArrayList<>(sample2list.get(sampleName));
        		VcfConcat app=new VcfConcat();
        		File filout= this.createFileForWriting(sampleName, ".vcf.gz");
        		filout.getParentFile().mkdirs();
        		app.setOutputFile(filout);
        		if(app.initializeKnime()!=0)
	        		{
        			throw new RuntimeException("Cannot init KNIME");
	        		}
        		if(app.executeKnime(vcfFiles)!=0)
        			{
        			throw new RuntimeException("Cannot run "+VcfConcat.class.getName());
        			}
        		
        		exec.setProgress(sampleName);
        		out_container.addRowToTable(new DefaultRow(
	                	RowKey.createRowKey(++id_generator),
	                	createDataCellsForOutTableSpec0(
	                			filout.getPath(),
	                			app.getVariantCount()
	                			)
	                	));
        		app.disposeKnime();
        		}
        	
        	
        	out_container.close();
            BufferedDataTable out0 = out_container.getTable();
            out_container=null;
            return internalTables(new BufferedDataTable[]{out0});
            }
        finally
            {
        	CloserUtil.close(vcfIiterator);
        	CloserUtil.close(iter);
        	CloserUtil.close(out_container);
            }
        }
	
	}
