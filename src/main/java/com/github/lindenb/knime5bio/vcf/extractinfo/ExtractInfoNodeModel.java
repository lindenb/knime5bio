package com.github.lindenb.knime5bio.vcf.extractinfo;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.util.vcf.VcfIterator;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVariantContextWriter;
import com.github.lindenb.knime5bio.htsjdk.variant.KnimeVcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;


public class ExtractInfoNodeModel extends AbstractExtractInfoNodeModel {
	
	private class ExtractInfoContextWriter extends KnimeVariantContextWriter
		{
		final Map<String,VCFInfoHeaderLine> tags= new TreeMap<>();
		
		ExtractInfoContextWriter(final ExecutionContext exec)
			{
			super(exec);
			}
		@Override
		public void writeHeader(VCFHeader header)
			{
			final VCFHeader header2 = new VCFHeader(header);
			
			for(final String s: ExtractInfoNodeModel.this.getSettingsModelInfoTagString().split("[,= ;\n\r]"))
	 			{
	 			if(s.isEmpty()) continue;
	 			tags.put(s,null);
	 			}

			
     		for(final String tagid:new ArrayList<>(tags.keySet()))
     			{
     			VCFInfoHeaderLine info = header.getInfoHeaderLine(tagid);
     			if(info==null)
     				{
     				info= new VCFInfoHeaderLine(tagid,1,VCFHeaderLineType.String,"Tag was not found in input.");
     				header2.addMetaDataLine(info);
     				getLogger().warn("INFO "+tagid+" missing in input");
     				}
     			tags.put(tagid,info);
     			}
			
			super.writeHeader(header2);
			}
		
		@Override
		protected DataColumnSpec[] createColumnSpecs(final VCFHeader header) {
			final DataColumnSpec[] dcs1 = super.createColumnSpecs(header);
			
			final DataColumnSpec[] dcs2 = new DataColumnSpec[dcs1.length+ this.tags.size()];
			int idx=0;
			for(idx=0;idx< dcs1.length;++idx) {
				dcs2[idx]=dcs1[idx];
			}
				
			for(final String tagid:this.tags.keySet() )
				{
				final DataColumnSpecCreator dcsCreator = new DataColumnSpecCreator(tagid, DataType.getType(StringCell.class));
				dcs2[idx]=dcsCreator.createSpec();
				idx++;
				}
			
			return dcs2;
		}
		
	private DataCell formatVCFField(final VCFInfoHeaderLine info,final Object val) {
			if ( val == null ) return DataType.getMissingCell();
			if ( val instanceof java.util.List ) {
					return this.formatVCFField(info,((List<?>)val).toArray()); 
					}
			else if ( val.getClass().isArray() ) {
				final int length = Array.getLength(val);
				if ( length == 0 ) DataType.getMissingCell();
				final StringBuilder sb = new StringBuilder(String.valueOf(Array.get(val, 0)));
				for ( int i = 1; i < length; i++) {
					sb.append(",");
					sb.append(String.valueOf(Array.get(val, i)));
				}
				return new StringCell(sb.toString());
				}
			else{ 
				return new StringCell(String.valueOf(val));
			 	}
			}
			

		@Override
		protected DataCell[] createDataCells(final VariantContext ctx) {
			final DataCell[] cells1 = super.createDataCells(ctx);//alread has the good size
			

			int idx= (cells1.length-this.tags.size());
			for(final String tagid:this.tags.keySet()) {

				if(!ctx.hasAttribute(tagid))
					{
					cells1[idx]=DataType.getMissingCell();
					}
				else
					{
					final VCFInfoHeaderLine info = this.tags.get(tagid);
					cells1[idx] = this.formatVCFField(info, ctx.getAttribute(tagid));
					}
				idx++;
				}
			return cells1;
			}
		}
	
	@Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, 
    		final ExecutionContext exec) throws Exception
        {     	
		VcfIterator vcfIn = null;
		KnimeVariantContextWriter vcfOut=null;
     	try {
     		vcfIn = new KnimeVcfIterator( inData[0],inData[1] );
     		final VCFHeader header = vcfIn.getHeader();
     		
     		vcfOut = new ExtractInfoContextWriter(exec);
     		vcfOut.writeHeader(header);
     		while(vcfIn.hasNext())
     			{
     			vcfOut.add(vcfIn.next());
     			}         
     		vcfOut.close();
			return vcfOut.getTables();
		} catch (Exception e) {
			getLogger().error("boum", e);
			e.printStackTrace();
			throw e;
		} finally {
			CloserUtil.close(vcfIn);
			CloserUtil.close(vcfOut);
			}
        }
}
