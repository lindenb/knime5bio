package com.github.lindenb.knime5bio.htsjdk.variant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;

import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.AbstractVCFCodec;
import htsjdk.variant.vcf.VCFConstants;
import htsjdk.variant.vcf.VCFHeader;

public class KnimeVcfIterator implements VcfIterator {
	private CloseableRowIterator iter;
	private VCFUtils.CodecAndHeader cah;
	private KnimeVariantContext peekVariant;
	
	private static void verify(final String name,final DataTableSpec specs,final int index,Class<? extends DataCell> clazz)
		{
		if(index>=specs.getNumColumns())
			{
			throw new IllegalArgumentException("Not enough column in table. Expected column $("+(index+1)+") named "+name);
			}
		final DataColumnSpec colspec = specs.getColumnSpec(index);
		if(!name.equals(colspec.getName()))
			{
			throw new IllegalArgumentException("Not Name in column $("+(index+1)+") expected  "+name+" but got "+colspec.getName());
			}
		if(!DataType.getType(clazz).getCellClass().isAssignableFrom(clazz))
			{
			throw new IllegalArgumentException("Bad type in column $("+(index+1)+") expected  "+clazz.getName()+" got "+DataType.getType(clazz).getCellClass());
			}
		}

	
	public KnimeVcfIterator(final BufferedDataTable inDataHeader,final BufferedDataTable inDataBody) {
		CloseableRowIterator hiter=null;
		final List<String> lines= new ArrayList<>();
		try {
			hiter= inDataHeader.iterator();
			while(hiter.hasNext())
				{
				final DataRow dataRow = hiter.next();
				final DataCell cell = dataRow.getCell(0);
				if( cell.isMissing()) throw new IllegalArgumentException("nil value in header");
				if(cell.getType().isAdaptable(StringValue.class)) throw new IllegalArgumentException("nil value in header");
				lines.add(StringCell.class.cast(cell).getStringValue());
				}
		} finally {
			CloserUtil.close(hiter);
		}
		
		int idx=0;
		final DataTableSpec specs = inDataBody.getSpec();
		verify(VCFHeader.HEADER_FIELDS.CHROM.name(),specs,idx++,StringCell.class);
		verify(VCFHeader.HEADER_FIELDS.POS.name(),specs,idx++,IntCell.class);
		verify(VCFHeader.HEADER_FIELDS.ID.name(),specs,idx++,StringCell.class);
		verify(VCFHeader.HEADER_FIELDS.REF.name(),specs,idx++,StringCell.class);
		verify(VCFHeader.HEADER_FIELDS.ALT.name(),specs,idx++,StringCell.class);
		verify(VCFHeader.HEADER_FIELDS.QUAL.name(),specs,idx++,DoubleCell.class);
		verify(VCFHeader.HEADER_FIELDS.FILTER.name(),specs,idx++,StringCell.class);
		verify(VCFHeader.HEADER_FIELDS.INFO.name(),specs,idx++,StringCell.class);
		final StringBuilder lastLine = new StringBuilder();
		for(VCFHeader.HEADER_FIELDS hf:VCFHeader.HEADER_FIELDS.values()){
			lastLine.append(lastLine.length()==0?"#":VCFConstants.FIELD_SEPARATOR);
			lastLine.append(hf.name());
		}
		
		if(specs.getNumColumns()>8) {
			lastLine.append(VCFConstants.FIELD_SEPARATOR);
			verify("FORMAT",specs,idx++,StringCell.class);
			lastLine.append("FORMAT");
			for(int i=9;i< specs.getNumColumns();++i) {
				final DataColumnSpec colspec = specs.getColumnSpec(i);
				if(!colspec.getType().getCellClass().isAssignableFrom(StringCell.class)) {
					throw new IllegalArgumentException("Bad type in column $("+(i+1)+") expected  a String Cell for sample name, got "+colspec.getType());
					}
				lastLine.append(VCFConstants.FIELD_SEPARATOR);
				lastLine.append(colspec.getName());
			}
		}
		lines.add(lastLine.toString());
		this.cah = VCFUtils.parseHeader(lines);
		this.peekVariant = null;
		this.iter = inDataBody.iterator();
	}
	
	private static String stringCellToString(final DataCell cell)
		{
		return cell.isMissing()?".":StringCell.class.cast(cell).getStringValue();
		}
	private static String doubleCellToString(final DataCell cell)
		{
		return cell.isMissing()?".":String.valueOf(DoubleCell.class.cast(cell).getDoubleValue());
		}

	
	public KnimeVariantContext decode(final DataRow row) {
		final StringBuilder sb=new StringBuilder();
		int idx=0;
		sb.append(StringCell.class.cast(row.getCell(idx++)).getStringValue());
		sb.append("\t");
		sb.append(IntCell.class.cast(row.getCell(idx++)).getIntValue());
		sb.append("\t");
		sb.append(stringCellToString(row.getCell(idx++)));
		sb.append("\t");
		sb.append(StringCell.class.cast(row.getCell(idx++)).getStringValue());
		sb.append("\t");
		sb.append(stringCellToString(row.getCell(idx++)));//ALT
		sb.append("\t");
		sb.append(doubleCellToString(row.getCell(idx++)));//QUAL
		sb.append("\t");
		sb.append(stringCellToString(row.getCell(idx++)));//FILTER
		sb.append("\t");
		sb.append(StringCell.class.cast(row.getCell(idx++)).getStringValue());//INFO
		
		if(this.getHeader().getNGenotypeSamples()>0)
			{
			sb.append("\t");
			sb.append(StringCell.class.cast(row.getCell(idx++)).getStringValue());//FORMAT
			for(int i=0;i<this.getHeader().getNGenotypeSamples();++i )
				{
				sb.append("\t");
				sb.append(StringCell.class.cast(row.getCell(idx++)).getStringValue());//sample

				}
			}
		return new KnimeVariantContext(
					getCodec().decode(sb.toString()),
					row
					);
	}
	
	private void fill()
		{
		if(this.peekVariant!=null) return;
		if(this.iter==null || !this.iter.hasNext()) return;
		final DataRow row = iter.next();
		this.peekVariant  = this.decode(row);
		}
	@Override
	public KnimeVariantContext next() {
		fill();
		if(this.peekVariant==null) throw new IllegalStateException("no next in KnimeVcfIterator");
		final KnimeVariantContext line=this.peekVariant;
		this.peekVariant=null;
		return line;
		}
	
	@Override
	public void close() throws IOException {
		CloserUtil.close(this.iter);
		this.iter = null;
	}	

	
	@Override
	public AbstractVCFCodec getCodec() {
		return this.cah.codec;
	}

	@Override
	public VCFHeader getHeader() {
		return this.cah.header;
	}

	@Override
	public VariantContext peek() {
		fill();
		return this.peekVariant;
	}
	@Override
	public boolean hasNext() {
	fill();
	return this.peekVariant!=null;
	}

}
