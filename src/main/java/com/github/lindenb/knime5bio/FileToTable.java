package com.github.lindenb.knime5bio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.io.IOUtils;

import htsjdk.samtools.util.CloserUtil;

public class FileToTable {
private final ExecutionContext exec;
private BufferedReader r=null;
private Pattern pattern = Pattern.compile("[\t]");

private enum ColType {DOUBLE,LONG,INTEGER,BOOLEAN,STRING};

private static class ColDef {
	String name="";
	ColType colType= ColType.INTEGER;
	
	ColDef(String name)
	{
		this.name=name.trim();
		if(name.startsWith("#")) name=name.substring(1);
	}
		
	
	public DataCell createCell( String s) {
		if(s!=null ) s=s.trim();
		if(s==null || s.trim().isEmpty()) return DataType.getMissingCell();
		switch(colType)
			{
			case DOUBLE: if(isNA(s)) return DataType.getMissingCell();return new DoubleCell(Double.parseDouble(s));
			case INTEGER: if(isNA(s)) return DataType.getMissingCell(); return new IntCell(Integer.parseInt(s));
			case LONG: if(isNA(s)) return DataType.getMissingCell(); return new LongCell(Long.parseLong(s));
			case BOOLEAN: return BooleanCell.BooleanCellFactory.create(
					s.equalsIgnoreCase("true") || s.equalsIgnoreCase("T") ? true :  false
					);
			default: return new StringCell(s);
			}
		}
	
	public DataColumnSpec createSpec() {
		DataType dataType= null;
		switch(colType)
			{
			case DOUBLE: dataType = DataType.getType(DoubleCell.class); break;
			case INTEGER: dataType = DataType.getType(IntCell.class); break;
			case LONG: dataType = DataType.getType(LongCell.class); break;
			case BOOLEAN: dataType = DataType.getType(BooleanCell.class); break;
			default: dataType = DataType.getType(StringCell.class);  break;
			}
		return new DataColumnSpecCreator(name, dataType).createSpec();
		}
	boolean isNA(String s)
		{
		return s.equals("NA") || s.equals("N/A");
		}
	
	public ColType visit( String s) {
		if(s!=null ) s=s.trim();
		if(colType == ColType.STRING || s==null || s.isEmpty()) return colType;
		
		boolean gotType=false;
		while(!gotType)
			{
			gotType=true;
			switch(colType)
				{
				case INTEGER: {
					if(isNA(s)) return colType;
					try { new Integer(s); }
					catch (Exception e) {
						gotType=false;
						colType=ColType.LONG;
						}
					break;
					}
				case LONG: {
					if(isNA(s)) return colType;
					try { new Long(s); }
					catch (Exception e) {
						gotType=false;
						colType=ColType.DOUBLE;
						}
					break;
					}
				case DOUBLE: {
					if(isNA(s)) return colType;
					try { new Double(s); }
					catch (Exception e) {
						gotType=false;
						colType=ColType.BOOLEAN;
						}
					break;
					}
				case BOOLEAN: {
					if(!(s.equalsIgnoreCase("true") || s.equalsIgnoreCase("T") ||
							s.equalsIgnoreCase("false") || s.equalsIgnoreCase("F")))
						{
						gotType=false;
						colType=ColType.STRING;
						}
					break;
					}
				case STRING: {
					gotType=true;
					break;
					}
				default: throw new IllegalStateException(colType.name());
				}
			}
		return colType;
		}
	
}

public FileToTable(final ExecutionContext exec)	{
	this.exec = exec;
}



private String nextLine() throws IOException
	{
	for(;;) {
	String s=this.r.readLine();
	if(s==null) return null;
	return s;
	}
	}

public BufferedDataTable convert(final File f) throws IOException {
	BufferedDataContainer container=null;
	try {
			//first pass
			this.r = IOUtils.openFileForBufferedReading(f);
			String s = this.nextLine();
			if(s==null) throw new IOException("Cannot get first line of "+f);
			String headers[]=pattern.split(s);
			final List<ColDef> colDefs=new ArrayList<FileToTable.ColDef>(headers.length);
			for(int i=0;i< headers.length;++i)
				{
				ColDef cd=new ColDef(headers[i]);
				if(cd.name.isEmpty()) cd.name="Col."+(i+1);
				colDefs.add(cd);
				}
			long nLine=1;
			while((s=this.nextLine())!=null)
				{
				if(s.trim().isEmpty()) continue;
				String tokens[]=pattern.split(s);
				if(tokens.length> colDefs.size()) {
					throw new IOException("Too many columns line "+(nLine+1)+" "+s);
					}
				boolean allString=true;
				for(int i=0;i< tokens.length && i< colDefs.size();++i)
					{
					if(colDefs.get(i).visit(tokens[i])!=ColType.STRING)
						{
						allString=false;
						}
					}
				++nLine;
				if(allString) break;
				}
			//rewind
			this.r.close();
			this.r = IOUtils.openFileForBufferedReading(f);
			
			
			//second pass
			this.nextLine();//skip header line
			
			final DataColumnSpec colspecs[]=new DataColumnSpec[colDefs.size()];
			for(int i=0;i< colDefs.size();++i)
				{
				colspecs[i] = colDefs.get(i).createSpec();
				}
			final DataTableSpec tableSpec = new DataTableSpec(colspecs);
			container = this.exec.createDataContainer(tableSpec);
			nLine=1;
			while((s=this.nextLine())!=null)
				{
				if(s.trim().isEmpty()) continue;
				String tokens[]=pattern.split(s);
				if(tokens.length> colDefs.size()) {
					throw new IOException("Too many columns line "+(nLine+1)+" "+s);
					}
				DataCell cells[]=new DataCell[colDefs.size()];
				for(int i=0;i< tokens.length;++i)
					{
					cells[i]=colDefs.get(i).createCell(tokens[i].trim());
					}
				RowKey rowKey = RowKey.createRowKey(nLine);
				DataRow row = new DefaultRow(rowKey, cells);
				container.addRowToTable(row);
				nLine++;
				}
			r.close();
			r=null;
			
		container.close();
		return container.getTable();
	} catch (IOException e) {
		throw e;
	}finally
	{
	if(container!=null && !container.isClosed()) container.close();
	CloserUtil.close(this.r);
	this.r=null;
	}
}
}
