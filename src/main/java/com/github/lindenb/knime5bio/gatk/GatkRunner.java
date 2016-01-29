package com.github.lindenb.knime5bio.gatk;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeLogger;

import htsjdk.samtools.util.CloserUtil;

public class GatkRunner {
	private File gatkJarFile = null;
	private NodeLogger logger = null;

	public void setGatkJarFile(File gatkJarFile) {
		this.gatkJarFile = gatkJarFile;
	}
	
	public File getGatkJarFile() {
		return gatkJarFile;
	}
	
	public NodeLogger getLogger() {
		return logger;
	}
	
	public void setLogger(final NodeLogger logger) {
		this.logger = logger;
	}
	
	public int execute(final List<String> argList) {
		
		if(this.logger == null) throw new IllegalStateException("Logger is null");
		
		final String javaHome = System.getProperty("java.home",null);
		if(javaHome==null)
			{
			logger.warn("java.home is not defined");
			throw new IllegalStateException("java.home is not defined");
			}
		final File javaExe = new File(new File(javaHome),"bin" + File.separator+"java");
		if(!javaExe.exists()) 
		if(!javaExe.exists()) throw new IllegalStateException("where is java ?? " +javaExe);
		if(!javaExe.isFile()) throw new IllegalStateException("not a file " +javaExe);

		
		if(this.gatkJarFile == null) throw new IllegalStateException("gatkJarFile is null");
		if(!this.gatkJarFile.exists()) throw new IllegalStateException("not an existing file " +this.gatkJarFile);
		if(!this.gatkJarFile.isFile()) throw new IllegalStateException("not a file " +this.gatkJarFile);
		if(!this.gatkJarFile.getName().endsWith(".jar")) throw new IllegalStateException("not a jar file " +this.gatkJarFile);
		final List<String> newArgs = new ArrayList<>();
		newArgs.add(javaExe.getPath());
		newArgs.add("-Djava.io.tmpDir="+KNIMEConstants.getKNIMETempDir());
		newArgs.add("-jar");
		newArgs.add(this.gatkJarFile.getPath());
		newArgs.addAll(argList);
		
		
		final String args[]=newArgs.toArray(new String[newArgs.size()]);
		StreamBoozer stderrStream=null;
		StreamBoozer stdouttream=null;
		try {
			final ProcessBuilder procbuilder = new ProcessBuilder(args);
			//procbuilder.directory(KNIMEConstants.get);
			Process proc = procbuilder.start();
			stderrStream = new StreamBoozer(proc.getErrorStream(),logger,NodeLogger.LEVEL.WARN);	
			stdouttream = new StreamBoozer(proc.getInputStream(),logger,NodeLogger.LEVEL.INFO);	
			stderrStream.start();
			stdouttream.start();
			final int returnValue = proc.waitFor();
			
			if(returnValue==0)
				{
				logger.info("GATK: success");
				}
			else
				{
				logger.error("GATK: failure");
				}
			stderrStream.close();
			stdouttream.close();
			return returnValue;
		} catch (Exception e) {
			logger.error("Failure for "+argList+" "+e.getMessage(),e);
			return -1;
		} finally
			{
			CloserUtil.close(stderrStream);
			CloserUtil.close(stdouttream);
			}
		
	
	}
	
	private static class StreamBoozer extends Thread implements Closeable
		{	
	    private InputStream in;
	    private NodeLogger logger = null;
	    private  NodeLogger.LEVEL level = null;
		public StreamBoozer(final InputStream in,NodeLogger logger,final NodeLogger.LEVEL level)
			{
	        this.in = in;
			this.logger = logger;
			this.level = level;
			}
	
		private void write(final CharSequence s)
			{
			switch(level)
				{	
				case INFO: logger.info(s);break;
				case WARN: logger.warn(s);break;
				default : logger.error(s);break;
				}
			}
		
		@Override
		public void close() throws IOException {
			CloserUtil.close(this.in);
			try {
				if(!this.isAlive()) return;
				if(this.isInterrupted()) return;
				this.interrupt();
			} catch (Exception e) {
			}
			}
		
	    @Override
	    public void run()
	    	{
	    	final StringBuilder sb=new StringBuilder();
	    	try {
	    		int c;
	  
	    		while((c=in.read())!=-1)
	    			{
	    			if(c=='\n' || sb.length()>1000) 
	    				{
	    				write(sb);
	    				sb.setLength(0);
	    				}
	    			sb.append((char)c);
	    			}
	    		if(sb.length()>0) write(sb);
	    	 	}
	    	catch(final Exception err)
	    		{
	    		logger.error("StreamBoozer error", err);
	    		}
	    	finally
	    		{
	    		CloserUtil.close(this.in);
	    		}
	    	}
		}

}
