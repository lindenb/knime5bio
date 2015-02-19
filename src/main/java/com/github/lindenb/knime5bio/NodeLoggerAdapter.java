package com.github.lindenb.knime5bio;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.knime.core.node.NodeLogger;

/**
 * NodeLoggerAdapter used to bind the two logging system used by
 * jvarkit (java.util.logging) and knime (org.knime.core.node.NodeLogger )
 * @author lindenb
 *
 */
public class NodeLoggerAdapter
	extends java.util.logging.Handler
	{
	private NodeLogger nodeLoger = null;
	private String prefix;
	
	public NodeLoggerAdapter(String prefix,NodeLogger nodeLoger)
		{
		this.prefix=(prefix==null || prefix.isEmpty()?"[LOG]":prefix);
		this.nodeLoger = nodeLoger;
		}
	
	@Override
	public void publish(LogRecord record) {
		if(this.nodeLoger==null || record.getLevel().equals(Level.OFF))
			return;
		String msg=prefix + String.valueOf(record.getMessage());
		Throwable thrown = record.getThrown();
		if(record.getLevel()==null)
			{
			
			}
		else if(record.getLevel().equals(Level.WARNING))
			{
			if(thrown==null)
				{
				this.nodeLoger.warn(msg);
				}
			else
				{
				this.nodeLoger.warn(msg,thrown);
				}
			}
		else if(record.getLevel().equals(Level.INFO))
			{
			if(thrown==null)
				{
				this.nodeLoger.warn(msg);
				}
			else
				{
				this.nodeLoger.warn(msg,thrown);
				}
			}
		else if(record.getLevel().equals(Level.SEVERE))
			{
			if(thrown==null)
				{
				this.nodeLoger.fatal(msg);
				}
			else
				{
				this.nodeLoger.fatal(msg,thrown);
				}
			}
		else
			{
			if(thrown==null)
				{
				this.nodeLoger.error(msg);
				}
			else
				{
				this.nodeLoger.error(msg,thrown);
				}
			}
		}
	@Override
	public void flush()
		{
		//do nothing
		}
	@Override
	public void close() throws SecurityException
		{
		this.nodeLoger=null;
		}
	@Override
	public String toString() {
		return String.valueOf(this.prefix);
		}
	}
