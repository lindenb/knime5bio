package com.github.lindenb.knime5bio.htsjdk.variant;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.util.vcf.VCFUtils;

import htsjdk.samtools.util.RuntimeIOException;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFEncoder;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeader.HEADER_FIELDS;

public class KnimeVariantContextWriter
	implements VariantContextWriter {

	private final ExecutionContext exec;
	private DataTableSpec outBodyDataTableSpec=null;
	private BufferedDataContainer bodyContainer = null;
	private BufferedDataContainer headContainer = null;
	private long rowIndex=0L;
	private VCFEncoder vcfEncoder=null;
	private VCFHeader header=null;
	private Pattern tabRegex = Pattern.compile("[\t]");
	
	public KnimeVariantContextWriter(ExecutionContext exec)
		{
		this.exec=exec;
		}

	@Override
	public void writeHeader(final VCFHeader header)
		{
		/** create data spec for header */
		final DataColumnSpec headColSpecs[]={
			new DataColumnSpecCreator("Header",DataType.getType(StringCell.class)).createSpec()
			};
		final DataTableSpec outheaderDataTableSpec=new DataTableSpec(headColSpecs);
		/* create head container */
		this.headContainer = this.exec.createDataContainer(outheaderDataTableSpec);
		try {
			long rowIdx=1;
			ByteArrayOutputStream sw=new ByteArrayOutputStream();
			VariantContextWriter vcw = VCFUtils.createVariantContextWriterToOutputStream(sw);
			vcw.writeHeader(header);
			vcw.close();
			sw.flush();
			sw.close();
			BufferedReader r= new BufferedReader(new StringReader(new String(sw.toByteArray())));
			String line;
			while((line=r.readLine())!=null) {
				if(line.startsWith("#CHROM")) break;
				this.headContainer.addRowToTable(new DefaultRow(
					RowKey.createRowKey(rowIdx++),
					new StringCell(line)));
				}
			r.close();
			} catch(Exception err) {
				throw new RuntimeIOException(err);
			}
		this.headContainer.close();
		/** close container for header */
		
		
		this.header= header;		
		this.vcfEncoder = new VCFEncoder(this.header, false, false);
		final DataColumnSpec colspecs[]= this.createColumnSpecs(header);   	
     	this.outBodyDataTableSpec=new DataTableSpec(colspecs);
     	this.bodyContainer = this.exec.createDataContainer(this.outBodyDataTableSpec);
		}

	protected DataColumnSpec[] createColumnSpecs(final VCFHeader header)
		{
		final DataColumnSpec colspecs[]=new DataColumnSpec[
		     VCFHeader.HEADER_FIELDS.values().length +
		     (header.getNGenotypeSamples()>0? 1+header.getNGenotypeSamples() : 0)
		     ];
		
		int colIdx=0;
		for(VCFHeader.HEADER_FIELDS hf:VCFHeader.HEADER_FIELDS.values())
			{
			final DataType dt;
			if(hf == HEADER_FIELDS.POS)
				{
				dt= org.knime.core.data.DataType.getType(org.knime.core.data.def.IntCell.class);
				}
			else if(hf == HEADER_FIELDS.QUAL)
				{
				dt= org.knime.core.data.DataType.getType(org.knime.core.data.def.DoubleCell.class);
				}
			else
				{
				dt= org.knime.core.data.DataType.getType(org.knime.core.data.def.StringCell.class);
				}
			colspecs[ colIdx ] = new org.knime.core.data.DataColumnSpecCreator(hf.name(),dt).createSpec();
			++colIdx;
			}
		
		if(header.getNGenotypeSamples()>0)
			{
			final DataType dt= org.knime.core.data.DataType.getType(org.knime.core.data.def.StringCell.class);
			colspecs[ colIdx ] = new org.knime.core.data.DataColumnSpecCreator("FORMAT",dt).createSpec();
			++colIdx;
			for(final String sampleName:header.getSampleNamesInOrder())
				{
		 		colspecs[ colIdx ] = new org.knime.core.data.DataColumnSpecCreator(sampleName,dt).createSpec();
		 		++colIdx;
				}
			}
		return colspecs;
		}
	
	@Override
	public void close() {
		if(this.bodyContainer!=null && !this.bodyContainer.isClosed()) this.bodyContainer.close();
		if(this.headContainer!=null && !this.headContainer.isClosed()) this.headContainer.close();
		}
	
	public BufferedDataContainer getBodyContainer() {
		return bodyContainer;
	}
	
	public BufferedDataContainer getHeadContainer() {
		return headContainer;
	}
	
	public BufferedDataTable[] getTables() {
		return new BufferedDataTable[]{
				this.getHeadContainer().getTable(),
				this.getBodyContainer().getTable()
		};
	}
	
	@Override
	public boolean checkError() {
		try {
			this.exec.checkCanceled();
			return false;
			}
		catch(final CanceledExecutionException err)
			{
			close();
			return true;
			}
		}

	@Override
	public void add(final VariantContext ctx) {
		final List<DataCell[]> cells= this.createDataCellRows(ctx);
		for(int i=0;i< cells.size();++i)
			{
			final DataRow row = new DefaultRow(
					RowKey.createRowKey(this.rowIndex),
					cells.get(i));
			++rowIndex;;
			this.bodyContainer.addRowToTable(row);
			}
		}
	/** give a chance to generate more than one row from one variant */
	protected List<DataCell[]> createDataCellRows(final VariantContext ctx) 
		{
		return Collections.singletonList(createDataCells(ctx));
		}
	
	/** called by createDataCellArray: convert variant context to DataCell[] */
	protected DataCell[] createDataCells(final VariantContext ctx) 
		{
		if(this.outBodyDataTableSpec==null) {
			throw new IllegalStateException("writthis.container.eHeader not called");
		}
		if(this.bodyContainer.isClosed()) {
			throw new IllegalStateException("container is closed");
		}

		
		final DataCell cells[]=new DataCell[this.outBodyDataTableSpec.getNumColumns()];
		Arrays.fill(cells, DataType.getMissingCell());
		final String vcfLine = this.vcfEncoder.encode(ctx);
		final String tokens[]=this.tabRegex.split(vcfLine);
				
		cells[0]= new StringCell(ctx.getContig()); 
		cells[1]= new IntCell(ctx.getStart()); 
		cells[2]= ctx.hasID()?new StringCell(ctx.getID()):DataType.getMissingCell();
		cells[3]= new StringCell(ctx.getReference().getDisplayString());
		if(!ctx.isVariant())
			{
			cells[4] = DataType.getMissingCell();
			}
		else
			{
			cells[4] = new StringCell(tokens[4]);
			}
		if(ctx.hasLog10PError())
			{
			cells[5] = new DoubleCell(ctx.getPhredScaledQual());
			}
		else
			{
			cells[5] = DataType.getMissingCell();
			}
		if(!tokens[6].equals("."))
			{
			cells[6] = new StringCell(tokens[6]);
			}
		else
			{
			cells[6] = DataType.getMissingCell();
			}
		if(tokens[7].isEmpty() || tokens[7].equals(".")) {
			cells[7] = DataType.getMissingCell();
			}
		else
			{
			cells[7] = new StringCell(tokens[7]);
			}
		if(header.getNGenotypeSamples()>0)
			{
			cells[8] = new StringCell(tokens[8]);
			for(int i=0;i< header.getNGenotypeSamples();++i)
     			{
				cells[ 9+i ] =  new StringCell(tokens[ 9+i]);
     			}
			}
		return cells;
		}
	
	
}
