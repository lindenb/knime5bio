package com.github.lindenb.knime5bio;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.io.IOUtils;

import htsjdk.samtools.util.CloserUtil;

public class TableToFile {
private final ExecutionContext exec;
private boolean printHeader=true;
private String nullValue="N/A";

public boolean isPrintHeader() {
	return printHeader;
}

public void setPrintHeader(boolean printHeader) {
	this.printHeader = printHeader;
}

public TableToFile(ExecutionContext exec) {
	this.exec=exec;
}

public void setNullValue(String nullValue) {
	this.nullValue = nullValue;
}

public String getNullValue() {
	return nullValue;
}

protected Object toValue(final DataCell cell) {
	if(cell==null || cell.isMissing()) return getNullValue();
	if(cell instanceof StringCell) {
		return StringCell.class.cast(cell).getStringValue();
	}
	if(cell instanceof IntCell) {
		return IntCell.class.cast(cell).getIntValue();
	}
	if(cell instanceof LongCell) {
		return LongCell.class.cast(cell).getLongValue();
	}
	
	if(cell instanceof DoubleCell) {
		return DoubleCell.class.cast(cell).getDoubleValue();
	}
	
	if(cell instanceof BooleanCell) {
		return BooleanCell.class.cast(cell).getBooleanValue();
	}
	
	throw new RuntimeException("Not implemented cell.type="+cell.getType());
}

public void convert(BufferedDataTable table,final File fileout) throws IOException
	{
	CloseableRowIterator iter=null;
	PrintWriter pw = IOUtils.openFileForPrintWriter(fileout);
	try {
		DataTableSpec spec=table.getDataTableSpec();
		if(isPrintHeader())
			{
			for(int i=0;i< spec.getNumColumns();++i)
				{
				if(i>0) pw.print('\t');
				pw.print(spec.getColumnSpec(i).getName());
				}
			pw.println();
			}
		iter= table.iterator();
		while(iter.hasNext())
			{
			final DataRow row = iter.next();
			for(int i=0;i< row.getNumCells();++i)
				{
				if(i>0) pw.print('\t');
				pw.print(toValue(row.getCell(i)));
				}
			pw.println();
			}
		pw.flush();
	} finally
		{
		CloserUtil.close(pw);
		}
	
	}
}
