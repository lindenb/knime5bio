package com.github.lindenb.knime5bio.vcf.veprest;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.append.AppendedColumnRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;


public class VepRestNodeModel extends AbstractVepRestNodeModel {
@Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, 
    		final ExecutionContext exec) throws Exception
        {   
		BufferedDataContainer container=null;
		CloseableRowIterator iter=null;
		 KnimeVcfIterator vcfIn = null;
     	try {
     		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     		dbf.setIgnoringComments(true);
     		dbf.setNamespaceAware(false);
     		dbf.setXIncludeAware(false);
     		final DocumentBuilder domBuilder = dbf.newDocumentBuilder();
     		
     		long lastCall=System.currentTimeMillis();
     		final DataTableSpec spec0 = inData[0].getSpec();
     		final DataTableSpec outspec = new DataTableSpec(
     				spec0,
     				new DataTableSpec(new DataColumnSpecCreator("VEP",XMLCell.TYPE).createSpec())
     				);
     		
     		container = exec.createDataContainer(outspec);
     		
     		exec.createDataContainer(outspec);
     		
     		vcfIn = new KnimeVcfIterator( inData[0],inData[1] );
     		iter = inData[1].iterator();
     		while(iter.hasNext())
     			{
     			final DataRow row= iter.next();
     			final VariantContext ctx = vcfIn.decode(row);
     			
     			Document dom=null;
     			for(final Allele alt:ctx.getAlternateAlleles())
	     			{
	     			if(alt.isSymbolic()) continue;
	     			final  StringBuilder uri=new StringBuilder(super.__vepuri.getStringValue());
	     			uri.append(URLEncoder.encode(ctx.getContig(),"UTF-8"));
	     			uri.append(":").append(ctx.getStart()).append("-").append(ctx.getStart()).
	     			append(":1/").append(alt.getDisplayString());
	     			uri.append("?content-type=text/xml");
	     			if(!__extraParams.getStringValue().trim().isEmpty())
		     			{
		     			uri.append("&");
		     			uri.append(__extraParams.getStringValue().trim());
		     			}
	     			final long now= System.currentTimeMillis();
	     			
	     			final  long diff = now - lastCall;
	     			if(diff < (long)__waitSeconds.getIntValue()*1000L) {
	     				Thread.sleep((long)__waitSeconds.getIntValue()*1000L-diff);
	     				}
	     			
	     			final Document doc = domBuilder.parse(uri.toString());
	     			if(dom==null) {
	     				dom=doc;
	     				}
	     			else
	     				{
	     				final Element root1 = dom.getDocumentElement();
	     				final Element root2 = doc.getDocumentElement();
	     				for(Node c2=root2.getFirstChild();c2!=null;c2=c2.getNextSibling()) {
	     					if(c2.getNodeType()!=Node.ELEMENT_NODE) continue;
	     					root1.appendChild(dom.importNode(c2, true));
	     					}
	     				}
	     			}
     			
     			container.addRowToTable( new AppendedColumnRow(row,
     					dom==null?DataType.getMissingCell():XMLCellFactory.create(dom))
     					);
     			
     			exec.checkCanceled();
     			iter.next();
     			}
     		vcfIn.close();vcfIn=null;
     		iter.close();iter=null;
     		container.close();
     		final BufferedDataTable table= container.getTable();
     		container=null;
     		return new BufferedDataTable[]{table};
		} catch (Exception e) {
			getLogger().error("boum", e);
			e.printStackTrace();
			throw e;
		} finally {
			CloserUtil.close(vcfIn);
			CloserUtil.close(iter);
		}
        }
}
