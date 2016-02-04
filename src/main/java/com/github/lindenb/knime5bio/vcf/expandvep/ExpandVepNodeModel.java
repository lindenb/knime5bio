package com.github.lindenb.knime5bio.vcf.expandvep;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
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
import htsjdk.variant.vcf.VCFInfoHeaderLine;


public class ExpandVepNodeModel extends AbstractExpandVepNodeModel {
	

	private final class ExtractVepContextWriter extends KnimeVariantContextWriter
		{
		final List<String> vepColumns= new ArrayList<>();
		
		ExtractVepContextWriter(final ExecutionContext exec)
			{
			super(exec);
			}
		@Override
		public void writeHeader(final VCFHeader header)
			{
 			/** ##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence annotations from Ensembl VEP. Format: Allele|Consequence|IMPACT|SYMBOL|Gene|Feature_type|Feature|BIOTYPE|EXON|INTRON|HGVSc|HGVSp|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|ALLELE_NUM|DISTANCE|STRAND|SYMBOL_SOURCE|HGNC_ID|CANONICAL|CCDS|ENSP|RefSeq|SIFT|PolyPhen|DOMAINS">
 			 */
 			final VCFInfoHeaderLine info = header.getInfoHeaderLine("CSQ");
 			if(info!=null) {
 				final String description = info.getDescription();
 				if( description!=null )
 					{
 					int format = description.indexOf("Format: ");
 					if(format!=-1) {
 						for(final String col: description.substring(format+8).split("\\|")) {
 							this.vepColumns.add("vep."+col.trim());
 							}
 						}
 					}
 				}
			super.writeHeader(header);
			}
		
		@Override
		protected List<DataCell[]> createDataCellRows(final VariantContext ctx) {
			
			final List<Object> csqs = ctx.getAttributeAsList("CSQ");
	
			final DataCell[] cells1 = super.createDataCells(ctx);//already has the good size
			final int idx= (cells1.length-this.vepColumns.size());

			if(csqs.isEmpty() || this.vepColumns.isEmpty()) {
				return Collections.singletonList(cells1);
			}
			List<DataCell[]> rows = new ArrayList<>(this.vepColumns.size());
			for(final Object vepObj : csqs)
				{
				final DataCell[] cells2 = new DataCell[cells1.length];//already has the good size
				System.arraycopy(cells1, 0, cells2, 0, cells1.length);

				String tokens[]= vepObj.toString().split("[\\|]");
				for(int i=0;i< tokens.length && idx+i<cells2.length;++i){
					cells2[i+idx] = (tokens[i].isEmpty()?DataType.getMissingCell():new StringCell(tokens[i]));
					}
				rows.add(cells2);
				}
			return rows;
		}
		
		@Override
		protected DataColumnSpec[] createColumnSpecs(final VCFHeader header) {
			final DataColumnSpec[] dcs1 = super.createColumnSpecs(header);
			
			final DataColumnSpec[] dcs2 = new DataColumnSpec[dcs1.length+ this.vepColumns.size()];
			int idx=0;
			for(idx=0;idx< dcs1.length;++idx) {
				dcs2[idx]=dcs1[idx];
			}
				
			for(final String col:this.vepColumns )
				{
				final DataColumnSpecCreator dcsCreator = new DataColumnSpecCreator(col, DataType.getType(StringCell.class));
				dcs2[idx]=dcsCreator.createSpec();
				idx++;
				}
			
			return dcs2;
		}
		

		@Override
		protected DataCell[] createDataCells(final VariantContext ctx) {
			throw new IllegalStateException("shouldn't be called");
			}
		}
	
	@Override
    protected BufferedDataTable[] execute(final BufferedDataTable headerData,
    		final BufferedDataTable bodyData,
    		final ExecutionContext exec) throws Exception
        {     
		final List<String> vepColumns= new ArrayList<>();

		VcfIterator vcfIn = null;
		KnimeVariantContextWriter vcfOut=null;
     	try {
     		vcfIn = new KnimeVcfIterator(headerData,bodyData);
     		final VCFHeader header = vcfIn.getHeader();
     		
 			final VCFInfoHeaderLine info = header.getInfoHeaderLine("CSQ");
 			if(info!=null) {
 				final String description = info.getDescription();
 				if( description!=null )
 					{
 					int format = description.indexOf("Format: ");
 					if(format!=-1) {
 						for(final String col: description.substring(format+8).split("\\|")) {
 							vepColumns.add("vep."+col.trim());
 							}
 						}
 					}
 				}

     		
     		vcfOut = new ExtractVepContextWriter(exec);
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
