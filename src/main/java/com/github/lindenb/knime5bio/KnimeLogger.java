package com.github.lindenb.knime5bio;

import org.knime.core.node.NodeLogger;

public class KnimeLogger extends org.slf4j.helpers.MarkerIgnoringBase {
	private static final long serialVersionUID = 1L;
	private static final NodeLogger nodeLogger = NodeLogger.getLogger("jvarkit");
	@Override
	public void debug(String arg0) {
		nodeLogger.debug(String.valueOf(arg0));
		
	}

	@Override
	public void debug(String arg0, Object arg1) {
		nodeLogger.debug(String.valueOf(arg0)+" "+String.valueOf(arg1));
		
	}

	@Override
	public void debug(String arg0, Object... arg1) {
		nodeLogger.debug(String.valueOf(arg0)+" "+String.valueOf(arg1));
		
	}

	@Override
	public void debug(String arg0, Throwable arg1) {
		nodeLogger.debug(arg0,arg1);
		
	}

	@Override
	public void debug(String arg0, Object arg1, Object arg2) {
		nodeLogger.debug(String.valueOf(arg0)+" "+String.valueOf(arg1)+" "+String.valueOf(arg2));
		
	}

	@Override
	public void error(String arg0) {
		nodeLogger.error(String.valueOf(arg0));
		
	}

	@Override
	public void error(String arg0, Object arg1) {
		nodeLogger.error(String.valueOf(arg0)+" "+String.valueOf(arg1));
		
	}

	@Override
	public void error(String arg0, Object... arg1) {
		nodeLogger.error(String.valueOf(arg0)+" "+String.valueOf(arg1));
		
	}

	@Override
	public void error(String arg0, Throwable arg1) {
		nodeLogger.error(String.valueOf(arg0)+" "+String.valueOf(arg1));
		arg1.printStackTrace(System.err);
		
	}

	@Override
	public void error(String arg0, Object arg1, Object arg2) {
		nodeLogger.error(String.valueOf(arg0)+" "+String.valueOf(arg1)+" "+String.valueOf(arg2));
		
	}

	@Override
	public void info(String arg0) {
		nodeLogger.info(String.valueOf(arg0));
		
	}

	@Override
	public void info(String arg0, Object arg1) {
		nodeLogger.info(String.valueOf(arg0)+" "+String.valueOf(arg1));
		
	}

	@Override
	public void info(String arg0, Object... arg1) {
		nodeLogger.info(String.valueOf(arg0)+" "+String.valueOf(arg1));
		
	}

	@Override
	public void info(String arg0, Throwable arg1) {
		nodeLogger.info(String.valueOf(arg0)+" "+String.valueOf(arg1));
		
	}

	@Override
	public void info(String arg0, Object arg1, Object arg2) {
		nodeLogger.info(String.valueOf(arg0)+" "+String.valueOf(arg1)+" "+String.valueOf(arg2));
		
	}

	@Override
	public boolean isDebugEnabled() {
		return true;
	}

	@Override
	public boolean isErrorEnabled() {
		return true;
	}

	@Override
	public boolean isInfoEnabled() {
		return true;
	}

	@Override
	public boolean isTraceEnabled() {
		return true;
	}

	@Override
	public boolean isWarnEnabled() {
		return true;
	}

	@Override
	public void trace(String arg0) {
		this.debug(arg0);
	}

	@Override
	public void trace(String arg0, Object arg1) {
		this.debug(arg0,arg1);
		
	}

	@Override
	public void trace(String arg0, Object... arg1) {
		this.debug(arg0,arg1);
		
	}

	@Override
	public void trace(String arg0, Throwable arg1) {
		this.debug(arg0,arg1);
		
	}

	@Override
	public void trace(String arg0, Object arg1, Object arg2) {
		this.debug(arg0,arg1,arg2);
		
	}

	@Override
	public void warn(String arg0) {
		this.warn(arg0,arg0);
		
	}

	@Override
	public void warn(String arg0, Object arg1) {
		this.warn(arg0,arg0+" "+arg1);
		
	}

	@Override
	public void warn(String arg0, Object... arg1) {
		this.warn(arg0,arg0+" "+arg1);
		
	}

	@Override
	public void warn(String arg0, Throwable arg1) {
		this.warn(arg0,arg0+" "+arg1);
		arg1.printStackTrace(System.err);
		
	}

	@Override
	public void warn(String arg0, Object arg1, Object arg2) {
		this.warn(arg0,arg0+" "+arg1+" "+arg2);
		
	}

}
