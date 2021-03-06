package com.github.lindenb.knime5bio.vcf.veprest;
import java.io.PrintWriter;
import java.io.StringWriter;
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
    protected BufferedDataTable[] execute(
    		final BufferedDataTable inDataheader,
    		final BufferedDataTable inDatabody,
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
     		final DataTableSpec spec0 = inDatabody.getSpec();
     		final DataTableSpec outspec = new DataTableSpec(
     				spec0,
     				new DataTableSpec(new DataColumnSpecCreator("VEP",XMLCell.TYPE).createSpec())
     				);
     		
     		container = exec.createDataContainer(outspec);
     		
     		exec.createDataContainer(outspec);
     		
     		vcfIn = new KnimeVcfIterator( inDataheader,inDatabody );
     		iter = inDatabody.iterator();
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
	     			
	     			Document lastdoc=null ;
	     			try
	     				{
	     				lastdoc=domBuilder.parse(uri.toString());
	     				}
	     			catch(Exception err) {
	     				lastdoc=domBuilder.newDocument();
	     				Element errorTag = lastdoc.createElement("error");
	     				errorTag.setAttribute("message", String.valueOf(err.getMessage()));
	     				lastdoc.appendChild(errorTag);
	     				StringWriter sw= new StringWriter();
	     				err.printStackTrace(new PrintWriter(sw));
	     				errorTag.appendChild(lastdoc.createTextNode(sw.toString()));
	     				}
	     			if(dom==null) {
	     				dom=lastdoc;
	     				}
	     			else
	     				{
	     				final Element root1 = dom.getDocumentElement();
	     				final Element root2 = lastdoc.getDocumentElement();
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
