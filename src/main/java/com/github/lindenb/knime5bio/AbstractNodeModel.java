package com.github.lindenb.knime5bio;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.BufferedDataTableHolder;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.port.PortType;
import org.knime.core.util.FileUtil;

import htsjdk.samtools.util.CloserUtil;


public abstract class AbstractNodeModel
	extends NodeModel
	implements BufferedDataTableHolder
	{
	private final static String UNIQ_ID_FILE="nodeid.txt";
	
	protected final static BufferedDataTable NO_BUFFERED_TABLE[] = new BufferedDataTable[0];
	
	/** uniq ID for this node */
	private String nodeUniqId=null;
	/** implementation of BufferedDataTableHolder */
	private BufferedDataTable m_dataHolder[]=null;

	// the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(AbstractNodeModel.class);
    

	
	protected AbstractNodeModel(final int nrInDataPorts, final int nrOutDataPorts) {
		super(nrInDataPorts, nrOutDataPorts);
	}

	protected AbstractNodeModel(PortType[] inPortTypes, PortType[] outPortTypes)
		{
		super(inPortTypes, outPortTypes);
		}


	/** array of BDTs which are held and used internally. */
	@Override
	public BufferedDataTable[] getInternalTables()
		{
		if(m_dataHolder==null) m_dataHolder=new BufferedDataTable[0];
		return m_dataHolder;
		}
	
	/** Allows the WorkflowManager to set information about new BDTs, for instance after load. */
	public void setInternalTables(final BufferedDataTable[] tables)
		{
		this.m_dataHolder = tables;
		};

	
	protected BufferedDataTable[] internalTables(BufferedDataTable[] tables)
		{
		setInternalTables(tables);
		return tables;
		}
	
	
	
	
	protected void fillSettingsModels(final List<SettingsModel> L) {
	}
	
    private List<SettingsModel> _getSettingsModels() {
    	final List<SettingsModel> L = new ArrayList<>();
    	fillSettingsModels(L);
    	return L;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	for(SettingsModel sm: _getSettingsModels())
    		sm.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	for(SettingsModel sm: _getSettingsModels())
    		sm.loadSettingsFrom(settings);

    }

    
    protected DataTableSpec createOutDataTableSpec(int index) {
    	throw new IllegalStateException("createOutDataTableSpec");
    }
   
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	for(SettingsModel sm: _getSettingsModels())
    		sm.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException
    	{
		getLogger().info("loadInternals "+getClass().getName()+" from "+internDir);
		final File f=new File(internDir,UNIQ_ID_FILE);
		if(f.exists() && f.isFile())
			{
			final FileReader r=new FileReader(f);
			final StringWriter sw=new StringWriter();
			FileUtil.copy(r, sw);
			r.close();
			final String id=sw.toString();
			if(this.nodeUniqId!=null && !id.equals(this.nodeUniqId))
				{
				throw new IOException("No the same node  id?? "+id+ " "+this.nodeUniqId);
				}
			this.nodeUniqId=id;
			}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

    	}
    
	/* @inheritDoc */
	@Override
	protected void reset()
		{
		getLogger().info("reset "+getClass().getName());
		this.removeTmpNodeFiles();
		this.setInternalTables(null);
		}
	
	/* @inheritDoc */
	@Override
	protected void 	onDispose()
		{
		getLogger().info("dispose "+getClass().getName());
		//this.removeTmpNodeFiles();
		this.setInternalTables(null);
		}
	
	/** return wether the node ID for this node has been defined */
	protected synchronized boolean hasNodeUniqId()
		{
		return nodeUniqId!=null;
		}
	
	/** return a name describing this type of node */
	protected String getNodeName()
		{
		return this.getClass().getSimpleName();
		}	
	/* get MD5 for a string */
	protected static String md5(final CharSequence sequence)
		{
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
	        for(int i=0;i < sequence.length();++i)
	          md.update((byte)sequence.charAt(i));
	        byte[] mdbytes = md.digest();
	        StringBuffer hexString = new StringBuffer(50);
	    	for (int i=0;i<mdbytes.length;i++) {
	    		String hex=Integer.toHexString(0xff & mdbytes[i]);
	   	     	if(hex.length()==1) hexString.append('0');
	   	     	hexString.append(hex);
	    		}
	    	return hexString.toString();
			}
		catch(Exception err)
			{
			throw new RuntimeException(err);
			}
		}

	/** get uniq node id for this node https://tech.knime.org/node/20789 */
	protected synchronized String getNodeUniqId()
		{
		if(this.nodeUniqId==null)
			{
			this.nodeUniqId= AbstractNodeModel.md5(
				String.valueOf(getClass().getName())+":"+
				System.currentTimeMillis()+":"+
				String.valueOf(System.getProperty("user.name"))+":"+
				String.valueOf(Math.random())
				);
			}
		return this.nodeUniqId;
		}
	
	/** get variable name defining path to data nodes. Default no variable defined  */
	protected String getPluginBaseDirectoryVariable()
		{
		return "com.github.lindenb.knime5bio.working.directory";
		}

	/** get root directory where we should store data for nodes */
	protected File getPluginBaseDirectory()
		{
		final String variableName = this.getPluginBaseDirectoryVariable();
		if(variableName == null) return null;/* no base dir defined */
		String s= this.peekFlowVariableString(variableName);
		if(s==null) throw new IllegalStateException(
				"Flow Variable "+variableName+" undefined. "+
				"Add this variable in the KNIME workspace (right click in the workspace icon) and set its value to an existing directory."
				);
		final File dir=new File(s);
		if(!dir.exists())
			{
			throw new IllegalStateException(variableName+" defined as  "+s+" but doesn't exists");
			}
		if(!dir.isDirectory())
			{
			throw new IllegalStateException(variableName+" defined as  "+s+" but it's not a directory");
			}
		return dir;
		}

	
	/** get this Node working directory. Return null if no plugin directory defined */
	protected File getNodeWorkingDirectory()
		{
		File parent= getPluginBaseDirectory();
		if(parent==null) return null;
		File me = new File(parent, getNodeUniqId()+"."+getNodeName());
		return me;
		}

	/* remove tmp files in getNodeWorkingDirectory */
	protected void removeTmpNodeFiles()
		{
		if( !hasNodeUniqId() ) return;
		File dir = this.getNodeWorkingDirectory();
		if(dir==null || !dir.exists()) return;
		getLogger().warn("Cleaning up "+dir);
		for(final File f : dir.listFiles())
			{
			removeTmpNodeFile(f);
			}
		}
	/* remove tmp file in getNodeWorkingDirectory */
	protected void removeTmpNodeFile(final File f)
		{
		getLogger().warn("Remove "+f);
		f.delete();
		}
	
	/** check node working exists, if not create one */
	protected void assureNodeWorkingDirectoryExists()
		throws IOException
		{
		final File dir = getNodeWorkingDirectory();
		if(dir.exists())
			{
			if(!dir.isDirectory())
				{
				throw new IOException("Node directory exists but it's not a directory: "+dir);
				}
			}
		else
			{
			final File parent=dir.getParentFile();
			if(!parent.exists()) {
				throw new IOException("Working directory doesnt exists "+parent);
				}
			if(!dir.mkdir()) {
				throw new IOException("Cannot create node directory "+dir);
				}
			}
		}
	
	/** create File for writing in this node directory 
	 * @param  base base filename
	 * @param extension suffix  
	 * 
	 * */
	protected File createFileForWriting(
			final Optional<String> base,
			String extension
			)
		{
		String rememberOriginal=null;
		File baseFile=null;
		if(base.isPresent())
			{
			try
				{
				baseFile=new File(base.get());
				}
			catch(Exception err)
				{
				baseFile=null;
				}
			}
		
		if(baseFile!=null)
			{
			rememberOriginal=baseFile.getName();
			int dot = rememberOriginal.indexOf('.');
			//start look likes a md5 sum.
			if(dot==32 && rememberOriginal.substring(0,32).matches("[a-z0-9]+"))
				{
				rememberOriginal = rememberOriginal.substring(33);
				}
			String common_extensions[]=new String[]{
				".",".text",".txt",".gz",".tsv",".gz",".zip",".bam",".vcf",".data",
				".sam",".fastq",".fa",".fasta",".xls",".tmp",".bcf"
				};
			boolean done=false;
			while(!done)
				{
				done=true;
				for(String x: common_extensions)
					{
					if(rememberOriginal.toLowerCase().endsWith(x))
						{
						done=false;
						rememberOriginal=
								rememberOriginal.substring(0,
								rememberOriginal.length()-x.length()
							);
						}
					}
				}
			if(rememberOriginal.isEmpty()) rememberOriginal=null;
			}
		if(extension!=null && !extension.startsWith("."))
			{
			extension="."+extension;
			}
		final java.io.File fileout = new java.io.File(
	    		this.getNodeWorkingDirectory(),
	    		md5(!base.isPresent()?"":base.get())+
	    		(rememberOriginal==null?"":"."+rememberOriginal)+
	    		(extension==null?"":extension)
	    		);
		return fileout;
		}


	
	/** read input stream, decompress gzip if needed */
	protected java.io. InputStream  openUriForInputStream(String uri) throws java.io.IOException
		{
		java.io.InputStream  input = null;
		try
			{
			input= org.knime.core.util.FileUtil.openInputStream(uri);
			}
		catch(Exception err)
			{
			throw new java.io.IOException(err);
			}
		/* http://stackoverflow.com/questions/4818468 */
		java.io.PushbackInputStream pb = new java.io.PushbackInputStream( input, 2 ); //we need a pushbackstream to look ahead
     	byte [] signature = new byte[2];
    	pb.read( signature ); //read the signature
    	pb.unread( signature ); //push back the signature to the stream
     	if( signature[ 0 ] == (byte) 0x1f && signature[ 1 ] == (byte) 0x8b ) //check if matches standard gzip magic number
       		return new java.util.zip.GZIPInputStream( pb );
     	else 
       		return pb;
		}	
	
	/** read input reader, decompress gzip if needed */
	protected java.io.BufferedReader openUriForBufferedReader(String uri) throws java.io.IOException
		{
		return new java.io.BufferedReader(
			new java.io.InputStreamReader(
			this.openUriForInputStream(uri)
			));
		}
	
	/* generic way to open a file for writing, gzip if extension=.gz */
	protected java.io.OutputStream openFileForWriting(java.io.File fout)  throws java.io.IOException
		{
		java.io.OutputStream out= new java.io.FileOutputStream(fout);
		if(fout.getName().endsWith(".gz"))
			{
			out= new java.util.zip.GZIPOutputStream(out);
			}
		return out;
		}
	protected int findColumnIndexByName(final BufferedDataTable table,SettingsModelColumnName columnName) throws InvalidSettingsException
		{
		return findColumnIndexByName(table.getDataTableSpec(),columnName);
		}

	protected int findColumnIndexByName(final DataTableSpec spec,SettingsModelColumnName columnName) throws InvalidSettingsException
		{
		int colIndex = spec.findColumnIndex(columnName.getColumnName());
		if(colIndex==-1) throw new InvalidSettingsException("Cannot find column "+columnName.getColumnName()+" in table");
		return colIndex;
		}
	
	/* utility: harvest all the Strings of one column , ignore empty strings and NIL*/
	protected <T extends Collection<String>> T collectOneColumn(
			final T list,
			final BufferedDataTable inputTable,
			final SettingsModelColumnName columnName,
			final Predicate<String> accept
			) throws Exception
		{
		if( columnName.getColumnName().isEmpty()) throw new IllegalArgumentException("empty column name");
		if(inputTable==null) throw new IllegalArgumentException("null table");
		if(list==null) throw new IllegalArgumentException("null list");
		final DataTableSpec spec = inputTable.getSpec();
		int colIndex = spec.findColumnIndex(columnName.getColumnName());
		if(colIndex==-1) throw new InvalidSettingsException("Cannot find column "+columnName.getColumnName()+" in table");
		/*if(!( spec.getColumnSpec(colIndex).getType()!=StringValue.class )) {
			throw new InvalidSettingsException(" column "+columnName.getColumnName()+" is not a string");
			}*/
		CloseableRowIterator iter=null;
		try {
			iter = inputTable.iterator();
			while(iter.hasNext())
				{
				final DataRow row = iter.next();
				final DataCell cell = row.getCell(colIndex);
				if(cell.isMissing()) continue;
				final String value = StringCell.class.cast(cell).getStringValue();
				if(value.isEmpty()) continue;
				if(accept!=null && !accept.test(value)) continue;
				list.add(value);
				}
			iter.close();
			iter=null;
			} 
		catch (Exception e) {
			getLogger().error("harvestOneColumn failed",e);
			}
		finally
			{
			CloserUtil.close(iter);
			}
		return list;
		}
	
	/* utility: collect existing files in column*/
	protected Set<File> collectFilesInOneColumn(
			final BufferedDataTable inputTable,
			final SettingsModelColumnName columnName
			) throws Exception
		{
		final Set<String> set= new HashSet<>();
		this.collectOneColumn(set, inputTable, columnName,null);
		final  Set<File> output=new HashSet<>(set.size());
		for(final String s:set){
			final File f= new File(s);
			if(!f.exists()) throw new FileNotFoundException("Cannot find file \""+f+"\"");
			if(!f.isFile()) throw new FileNotFoundException("not a file \""+f+"\"");
			output.add(f);
		}
		return output;
		}

	/** jvarkit utility , throw exception if problem */
	protected void checkEmptyListOfThrowables(final  Collection<Throwable> throwables) throws InvalidSettingsException {
		if(throwables==null || throwables.isEmpty()) return;
		for(final Throwable t:throwables)
			{
			getLogger().warn("error in "+getNodeName(), t);
			}
		throw new InvalidSettingsException(throwables.iterator().next());
		}
	/** utility : split String into lines */
	protected List<String> split(final CharSequence seq) {
		return Arrays.asList(Pattern.compile("[\n]").split(seq));
	}
	
	
	}
