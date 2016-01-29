package com.github.lindenb.knime5bio.htsjdk.variant;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;

import com.github.lindenb.jvarkit.util.vcf.VCFUtils;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.variant.vcf.VCFHeader;

class VcfLineIteratorTable implements LineIterator,Closeable
	{
	public static final String WORKFLOW_VARIABLE_HEADER="vcf.header.in.workflow";
	private final List<String> headerLines;
	private final CloseableRowIterator iter;
	private int indexInHeaderLines=-1;
	private VCFHeader header;
	private String peekLine=null;
	
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
		if(!DataType.getType(clazz).isASuperTypeOf(colspec.getType()))
			{
			throw new IllegalArgumentException("Bad type in column $("+(index+1)+") expected  "+clazz.getName());
			}
		}
	
	private VcfLineIteratorTable(final BufferedDataTable dataTable,final String headerAsString)
		{
		this.headerLines = Arrays.asList(headerAsString.split("[\n]"));
		if(headerLines.isEmpty()) throw new IllegalArgumentException("no line in vcf header");
		final DataTableSpec specs=dataTable.getSpec();
		int idx=0;
		verify(VCFHeader.HEADER_FIELDS.CHROM.name(),specs,idx++,StringCell.class);
		verify(VCFHeader.HEADER_FIELDS.POS.name(),specs,idx++,IntCell.class);
		verify(VCFHeader.HEADER_FIELDS.ID.name(),specs,idx++,StringCell.class);
		verify(VCFHeader.HEADER_FIELDS.REF.name(),specs,idx++,StringCell.class);
		verify(VCFHeader.HEADER_FIELDS.ALT.name(),specs,idx++,StringCell.class);
		verify(VCFHeader.HEADER_FIELDS.QUAL.name(),specs,idx++,DoubleCell.class);
		verify(VCFHeader.HEADER_FIELDS.FILTER.name(),specs,idx++,StringCell.class);
		verify(VCFHeader.HEADER_FIELDS.INFO.name(),specs,idx++,StringCell.class);
		
	
		this.header= VCFUtils.parseHeader(this.headerLines).header;
		
		
		if(this.header.getNGenotypeSamples()>0)
			{
			verify("FORMAT",specs,idx++,StringCell.class);
			for(String sampleName : header.getSampleNamesInOrder())
				{
				verify(sampleName,specs,idx++,StringCell.class);
				}
			}
		
		this.iter = dataTable.iterator();
		}

	

	private static String stringCellToString(final DataCell cell)
		{
		return cell.isMissing()?".":StringCell.class.cast(cell).getStringValue();
		}
	private static String doubleCellToString(final DataCell cell)
		{
		return cell.isMissing()?".":String.valueOf(DoubleCell.class.cast(cell).getDoubleValue());
		}
	
	
	private void fill()
		{
		if(this.peekLine!=null) return;
		
		if(this.indexInHeaderLines +1 < this.headerLines.size()) 
			{
			this.indexInHeaderLines++;
			this.peekLine = this.headerLines.get(this.indexInHeaderLines);
			return;
			}
		if(!iter.hasNext()) return;
		StringBuilder sb=new StringBuilder();
		final DataRow row = iter.next();
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
		
		if(this.header.getNGenotypeSamples()>0)
			{
			sb.append("\t");
			sb.append(StringCell.class.cast(row.getCell(idx++)).getStringValue());//FORMAT
			for(int i=0;i<this.header.getNGenotypeSamples();++i )
				{
				sb.append(StringCell.class.cast(row.getCell(idx++)).getStringValue());//sample
				}
			}
		this.peekLine  = sb.toString();
		}
	@Override
	public boolean hasNext() {
	fill();
	return this.peekLine!=null;
	}
	
	@Override
	public String next() {
		
		fill();
		if(this.peekLine==null) throw new IllegalStateException();
		String line=this.peekLine;
		this.peekLine=null;
		return line;
	}
	@Override
	public void close() throws IOException {
		CloserUtil.close(this.iter);
		
	}

	@Override
	public String peek() {
		fill();
		return this.peekLine;
	}
	
	

	
	}
