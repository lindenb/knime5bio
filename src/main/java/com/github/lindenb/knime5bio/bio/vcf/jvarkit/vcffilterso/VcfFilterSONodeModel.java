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
package com.github.lindenb.knime5bio.bio.vcf.jvarkit.vcffilterso;
import htsjdk.samtools.util.CloserUtil;

import java.io.File;
import java.util.Collections;

import org.knime.core.node.*;
import org.knime.core.data.*;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.*;

import com.github.lindenb.jvarkit.tools.misc.VcfFilterSequenceOntology;
  	
public class VcfFilterSONodeModel
	extends AbstractVcfFilterSONodeModel
	{
	public VcfFilterSONodeModel()
		{
		}

	protected VcfFilterSequenceOntology createInstanceOfKnimeApplication()
  			throws Exception {
  		
  		com.github.lindenb.jvarkit.tools.misc.VcfFilterSequenceOntology instance=new VcfFilterSequenceOntology();
  		instance.setInverseResult(super.isPropertyInverseSelectionValue());
  		instance.setReasoning(super.isPropertyEnableReasoningValue());
  		for(String acn: this.getPropertyTermsValue().split("[ ,\n\t]"))
  			{
  			instance.addTerm(acn);
  			}
  		return instance;
  		}
  	
	@Override
	protected BufferedDataTable[] execute(BufferedDataTable[] inData,
			ExecutionContext exec) throws Exception
		{
		VcfFilterSequenceOntology instance=null;
        CloseableRowIterator iter=null;
        BufferedDataTable inTable=inData[0];
        DataTableSpec dataOutSpec = this.createOutTableSpec0();
        BufferedDataContainer out_container = null;
        int inUriIndex = this.findVcfInputRequiredColumnIndex(inTable.getDataTableSpec());

		try {
			instance= this.createInstanceOfKnimeApplication();
			instance.initializeKnime();
			out_container = exec.createDataContainer(dataOutSpec);
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
	                File fileout = new File(
	                		getKnime5BiNodeWorkingDirectory(),
	                		md5(uri)+".vcf.gz"
	                		);

	                if(fileout.getParentFile()!=null)
	                	{
	                	fileout.getParentFile().mkdirs();
	                	}
	                instance.setOutputFile(fileout);
	                
	            
	                if(instance.executeKnime(Collections.singletonList(uri))!=0)
	                	{
	                	throw new RuntimeException("error during processing"+getNodeName()+" "+uri);
	                	}
	               
	                
	                out_container.addRowToTable(new DefaultRow(
	                	RowKey.createRowKey(nRows),
	                	createDataCellsForOutTableSpec0(
	                			fileout.getPath(),
	                			instance.geVariantCount()
	                			)
	                	));
	               
	                
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
	        	instance.disposeKnime();
	            }
	        }

  	
	}
