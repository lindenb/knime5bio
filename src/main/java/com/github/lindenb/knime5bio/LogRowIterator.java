package com.github.lindenb.knime5bio;

import java.io.Closeable;
import java.util.Iterator;

import org.knime.core.data.DataRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

public class LogRowIterator implements Iterator<DataRow>,Closeable {
private CloseableRowIterator delegate =null;
private long _size=0L;
private long _seen=0L;
private final String prefix;
private ExecutionContext context;

public LogRowIterator(final BufferedDataTable table,ExecutionContext context) {
	this(null,table,context);
}


public LogRowIterator(final String prefix,final BufferedDataTable table,ExecutionContext context) {
	this.delegate = table.iterator();
	this.context = context;
	_size = table.size();
	if(prefix==null || prefix.trim().isEmpty()) {
		this.prefix = null;
		}
	else
		{
		this.prefix=prefix.trim()+" ";
		}
}

@Override
public void close() {
	if(this.delegate!=null) this.delegate.close();
	this.delegate=null;
}

@Override
public boolean hasNext() {
	return this.delegate!=null && this.delegate.hasNext();
}

@Override
public DataRow next() {
	if(this.delegate==null) throw new IllegalStateException("delegate iterator is null/closed");
	
	double progress= (double)_seen/(double)_size;
	this._seen++;
	if(this.prefix==null || this.prefix.isEmpty())
		{
		this.context.setProgress(progress);
		}
	else
		{
		this.context.setProgress(progress,this.prefix+" "+String.format("%.2f%%", progress*100.0));
		}
	return this.delegate.next();
	}


}
