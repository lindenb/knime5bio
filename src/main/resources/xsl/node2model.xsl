<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:import href="mod.node.xsl"/>
<xsl:output method="text" />

<xsl:template match="/">
<xsl:apply-templates select="node"/>
</xsl:template>


<xsl:template match="node">/* <xsl:value-of select="$license"/> */
package <xsl:value-of select="$package"/>;
<xsl:value-of select="$common-imports"/>
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ExecutionContext;


//force javac 
<xsl:if test="$abstract = 'true'">import <xsl:value-of select="$package"/>.<xsl:value-of select="concat($nodeName,'NodeModel')"/>;
</xsl:if>
import <xsl:value-of select="$package"/>.<xsl:value-of select="concat($nodeName,'NodeDialog')"/>;
import <xsl:value-of select="$package"/>.<xsl:value-of select="concat($nodeName,'NodeFactory')"/>;
import <xsl:value-of select="$package"/>.<xsl:value-of select="concat($nodeName,'NodePlugin')"/>;



@Generated("xsl")
public <xsl:if test="$abstract = 'true'">abstract</xsl:if> class <xsl:if test="$abstract = 'true'">Abstract</xsl:if><xsl:value-of select="$modelName"/> extends
	<xsl:choose>
		<xsl:when test="@extends">
			<xsl:value-of select="@extends"/>
		</xsl:when>
		<xsl:otherwise>
		<xsl:text> com.github.lindenb.knime5bio.AbstractNodeModel </xsl:text>
		</xsl:otherwise>
	</xsl:choose>
	{
	protected static final int NUMBER_INPUT_TABLES= <xsl:value-of select="count(ports/inPort)"/> ;
		
	protected static final int NUMBER_OUTPUT_TABLES= <xsl:value-of select="count(ports/outPort)"/>;
	
	
	<xsl:apply-templates select="settings" mode="decl"/>
	
	protected <xsl:if test="$abstract = 'true'">Abstract</xsl:if><xsl:value-of select="$modelName"/>()
		{
		super(NUMBER_INPUT_TABLES,NUMBER_OUTPUT_TABLES);
		}
	
	
    protected abstract BufferedDataTable[] execute(<xsl:for-each select="ports/inPort">
	    final BufferedDataTable inData<xsl:value-of select="position() - 1"/>, </xsl:for-each>
	    final ExecutionContext exec) throws Exception;
	
	
	@Override
    protected final BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception
		{
		return this.execute(<xsl:for-each select="ports/inPort">inData[<xsl:value-of select="position() - 1"/>],</xsl:for-each>exec);
		}
	
    protected DataTableSpec createOutDataTableSpec(final int index,final DataTableSpec[] inSpec)
    	{
		
    	switch(index)
    		{
    		<xsl:for-each select="ports/outPort">
    		case <xsl:value-of select="position() -1"/> : return createOutTableSpec<xsl:value-of select="position() -1"/>(inSpec);
    		</xsl:for-each>
    		default: getLogger().info("Bad index");throw new IllegalStateException("Bad Index");
    		}
    	}
	
	<xsl:for-each select="ports/outPort">
	
	
	/** create DataTableSpec for outport '<xsl:value-of select="position() -1"/>' output */
	protected final DataTableSpec createOutTableSpec<xsl:value-of select="position() -1"/>()
		{
		return createOutTableSpec<xsl:value-of select="position() -1"/>(new BufferedDataTable[0]);
		}
		
	/** create DataTableSpec for outport  '<xsl:value-of select="position() -1"/>' output for known DataTableSpec */
	protected DataTableSpec configureOutTableSpec<xsl:value-of select="position() -1"/>(final DataTableSpec[] inSpecs) throws InvalidSettingsException
		{
		getLogger().info("configureOutTableSpec<xsl:value-of select="position() -1"/>");
		return this.createOutTableSpec<xsl:value-of select="position() -1"/>(inSpecs);
		}	
	
	/** create DataTableSpec for outport '<xsl:value-of select="position() -1"/>' output */
	protected final DataTableSpec createOutTableSpec<xsl:value-of select="position() -1"/>(final BufferedDataTable[] tables)
		{
	
		if( tables==null || tables.length == 0)
		  {
		  return this.createOutTableSpec<xsl:value-of select="position() -1"/>(new DataTableSpec[0]);
		  }
		else
		  {
		  final DataTableSpec array[]= new DataTableSpec[tables.length];
		  for(int i=0;i&lt; array.length;++i) array[i]=tables[i].getDataTableSpec();
		  return this.createOutTableSpec<xsl:value-of select="position() -1"/>(array);
		  }
		}

	
	/** create DataTableSpec for outport '<xsl:value-of select="position() -1"/>' output */
	protected DataTableSpec createOutTableSpec<xsl:value-of select="position() -1"/>(final DataTableSpec[] inSpecs)
		{
		<xsl:choose>
		<xsl:when test="@at-runtime='true' or count(column) = 0">
		/* defined at runtime */
		return null;
		</xsl:when>
		<xsl:otherwise>
		getLogger().info("createOutTableSpec<xsl:value-of select="position() -1"/>");
		DataColumnSpec colspecs[]=new DataColumnSpec[ <xsl:value-of select="count(column)"/> ];
		<xsl:for-each select="column">
		colspecs[ <xsl:value-of select="position() -1"/> ] = new org.knime.core.data.DataColumnSpecCreator(
			"<xsl:value-of select="@name"/>" , <xsl:choose>
			<xsl:when test="@type='string'">org.knime.core.data.DataType.getType(org.knime.core.data.def.StringCell.class</xsl:when>
			<xsl:when test="@type='int'">org.knime.core.data.DataType.getType(org.knime.core.data.def.IntCell.class</xsl:when>
			<xsl:when test="@type='double'">org.knime.core.data.DataType.getType(org.knime.core.data.def.DoubleCell.class</xsl:when>
			<xsl:otherwise><xsl:message terminate="yes">createDataCellsForOutTableSpec:unknown column type</xsl:message></xsl:otherwise>
			</xsl:choose>)).createSpec();
		</xsl:for-each>
		return new DataTableSpec(colspecs);		
		</xsl:otherwise>
		</xsl:choose>
		}
	
	protected DataCell[] createDataCellsForOutTableSpec<xsl:value-of select="position() -1"/>(
		<xsl:for-each select="column">
			
			<xsl:if test="position()&gt;1"> , </xsl:if>
			<xsl:text>final </xsl:text>
			<xsl:choose>
				<xsl:when test="@type='string'">CharSequence</xsl:when>
				<xsl:when test="@type='int'">Integer</xsl:when>
				<xsl:when test="@type='double'">Double</xsl:when>
				<xsl:otherwise><xsl:message terminate="yes">createDataCellsForOutTableSpec:unknown column type</xsl:message></xsl:otherwise>
			</xsl:choose>
			<xsl:text> </xsl:text>
			<xsl:value-of select="@name"/>
		</xsl:for-each>
		)
		{
		final DataCell __cells[]=new DataCell[<xsl:value-of select="count(column)"/>];
		<xsl:for-each select="column">
		__cells[<xsl:value-of select="position() -1"/>] = ( <xsl:value-of select="@name"/> !=null? 
			<xsl:choose>
				<xsl:when test="@type='string'">new StringCell( <xsl:value-of select="@name"/>.toString() ) </xsl:when>
				<xsl:when test="@type='int'">new IntCell( <xsl:value-of select="@name"/> ) </xsl:when>
				<xsl:when test="@type='double'">new DoubleCell( <xsl:value-of select="@name"/> ) </xsl:when>
				<xsl:otherwise><xsl:message terminate="yes">createDataCellsForOutTableSpec:unknown column type</xsl:message></xsl:otherwise>
			</xsl:choose>
			:  DataType.getMissingCell() ) ;
		</xsl:for-each>
		return __cells;
		}
	
	
	</xsl:for-each>
	
	@Override 
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException
		{
		getLogger().info("configure");

    	if(inSpecs==null || inSpecs.length!= <xsl:value-of select="count(ports/inPort)"/>)
			{
			throw new InvalidSettingsException("Expected  <xsl:value-of select="count(ports/inPort)"/> tables");
			}
    	final DataTableSpec datatablespecs[] = new DataTableSpec[<xsl:value-of select="count(ports/outPort)"/>];
    	<xsl:for-each select="ports/outPort">
    	datatablespecs[<xsl:value-of select="position() -1"/>] =  configureOutDataTableSpec(<xsl:value-of select="position() -1"/>,inSpecs);
    	</xsl:for-each>
    	return datatablespecs;
    	}
	
	
	/** configure output port for known-index for known inSpecs */	
	protected DataTableSpec configureOutDataTableSpec(int index,final DataTableSpec[] inSpecs) throws InvalidSettingsException
		{
		getLogger().info("configureOutDataTableSpec");
		switch(index)
    		{
    		<xsl:for-each select="ports/outPort">
    		case <xsl:value-of select="position() - 1"/> : return configureOutTableSpec<xsl:value-of select="position() - 1"/>(inSpecs);
    		</xsl:for-each>
    		default: throw new IllegalStateException();
    		}
		}

	
	
	
	@Override
	protected void fillSettingsModels(final List&lt;SettingsModel&gt; L) {
		<xsl:for-each select="settings/setting">L.add(this.<xsl:apply-templates select="." mode="fieldName"/>);
		</xsl:for-each>
		super.fillSettingsModels(L);
	}
	
	<xsl:if test='count(settings/setting[@gatk="true"]) &gt; 0'>
	protected List&lt;String&gt; fillGatkArgs(final List&lt;String&gt; args) {
	<xsl:apply-templates select="settings/setting[@gatk='true']" mode="gatk"/>
	return args;
	}
	</xsl:if>
	
	}

</xsl:template>


<xsl:template match="settings" mode="decl">
/** BEGIN SETTINGS */

<xsl:apply-templates select="setting" mode="decl"/>


/** END SETTINGS */
</xsl:template>

<xsl:template match="setting" mode="decl">

<xsl:variable name="javaName">
	<xsl:call-template name="titleize">
		<xsl:with-param name="name" select="@name"/>
	</xsl:call-template>
</xsl:variable>


/** BEGIN SETTING <xsl:value-of select="@name"/> */

		final static String <xsl:apply-templates select="." mode="config-name"/> = <xsl:apply-templates select="." mode="config-value"/>;

<xsl:choose>
	<xsl:when test="@type='string' or @type='input-file' or @type='output-file'">
		final static String <xsl:apply-templates select="." mode="config-default"/> =	"<xsl:call-template name="escape">
						<xsl:with-param name="string">
							<xsl:choose>
								<xsl:when test="default"><xsl:apply-templates select="default" /></xsl:when>
								<xsl:when test="@default"><xsl:value-of select="@default"/></xsl:when>
								<xsl:otherwise></xsl:otherwise>
							</xsl:choose>
						</xsl:with-param>
					</xsl:call-template>";
		
		protected final SettingsModelString <xsl:apply-templates select="." mode="fieldName"/> = new SettingsModelString(
			<xsl:apply-templates select="." mode="config-name"/>,
			<xsl:choose>
				<xsl:when test="@type='input-file' and @required='false'">"" /* empty because no required */</xsl:when>
				<xsl:otherwise><xsl:apply-templates select="." mode="config-default"/></xsl:otherwise>
			</xsl:choose>
			
			);
		/** getter for <xsl:value-of select="@name"/> */
		protected SettingsModelString getSettingsModel<xsl:value-of select="$javaName"/>() {
			return this.<xsl:apply-templates select="." mode="fieldName"/>;
			}
			
		/** value getter for <xsl:value-of select="@name"/> */
		
		<xsl:choose>
			<xsl:when test="@type='input-file' or @type='output-file'">
				 <xsl:choose>
					<xsl:when test="@required='false'">
					protected java.util.Optional&lt;java.io.File&gt; getSettingsModel<xsl:value-of select="$javaName"/>File()
						{
						final String s = this.<xsl:apply-templates select="." mode="fieldName"/>.getStringValue();
						if(s==null || s.trim().isEmpty()) return java.util.Optional.empty();
						return java.util.Optional.of(new java.io.File(s));
						}
					</xsl:when>
					<xsl:when test="@required='true'">
					protected java.io.File getSettingsModel<xsl:value-of select="$javaName"/>File()
						{
						final String s = this.<xsl:apply-templates select="." mode="fieldName"/>.getStringValue();
						return new java.io.File(s);
						}
					</xsl:when>
					<xsl:otherwise>
							<xsl:message terminate="yes">required missing: <xsl:value-of select="$nodeName"/> <xsl:value-of select="@name"/>:<xsl:value-of select="@type"/>.</xsl:message>
					</xsl:otherwise>
				
				</xsl:choose>   
			
			</xsl:when>
			<xsl:when test="@type='string'">
			protected	String getSettingsModel<xsl:value-of select="$javaName"/>String() {
				return this.<xsl:apply-templates select="." mode="fieldName"/>.getStringValue();
				}
			
			</xsl:when>
			
			
			<xsl:otherwise>
				<!--  todo -->
			</xsl:otherwise>
		</xsl:choose>	
			
	</xsl:when>
	<xsl:when test="@type='column'">
		final static String <xsl:apply-templates select="." mode="config-default"/> = <xsl:choose>
			<xsl:when test="@default">"<xsl:value-of select="@default"/>"</xsl:when>
			<xsl:otherwise>"<xsl:value-of select="@name"/>"</xsl:otherwise>
			</xsl:choose>;
		protected final SettingsModelColumnName <xsl:apply-templates select="." mode="fieldName"/> = new SettingsModelColumnName(
			<xsl:apply-templates select="." mode="config-name"/>,
			<xsl:apply-templates select="." mode="config-default"/> 
			);
			
		/** getter for <xsl:value-of select="@name"/> */
		protected SettingsModelColumnName getSettingsModel<xsl:value-of select="$javaName"/>() {
			return this.<xsl:apply-templates select="." mode="fieldName"/>;
			}
			
			
	</xsl:when>
	
	<xsl:when test="(@type='int' or @type='double') and not(@required)">
		<xsl:message terminate="true">@required missing in <xsl:value-of select="concat($className,':',@name)"/></xsl:message>
	</xsl:when>
	
	<xsl:when test="(@type='int' or @type='double') and @required='false'">
		
		
		final static String <xsl:apply-templates select="." mode="config-default"/> =	"<xsl:call-template name="escape">
				<xsl:with-param name="string">
					<xsl:choose>
						<xsl:when test="default"><xsl:apply-templates select="default" /></xsl:when>
						<xsl:when test="@default"><xsl:value-of select="@default"/></xsl:when>
						<xsl:otherwise></xsl:otherwise>
					</xsl:choose>
				</xsl:with-param>
			</xsl:call-template>";
		
		protected final SettingsModelString <xsl:apply-templates select="." mode="fieldName"/> = new SettingsModelString(
			<xsl:apply-templates select="." mode="config-name"/>,
			<xsl:choose>
				<xsl:when test="@type='input-file' and @required='false'">"" /* empty because no required */</xsl:when>
				<xsl:otherwise><xsl:apply-templates select="." mode="config-default"/></xsl:otherwise>
			</xsl:choose>
			
			);
		/** getter for <xsl:value-of select="@name"/> */
		protected SettingsModelString getSettingsModel<xsl:value-of select="$javaName"/>() {
			return this.<xsl:apply-templates select="." mode="fieldName"/>;
			}
		
		<xsl:choose>
			<xsl:when test="@type = 'int'">
			protected java.util.Optional&lt;Integer&gt; getSettingsModel<xsl:value-of select="$javaName"/>Integer() throws org.knime.core.node.InvalidSettingsException
		 	{
			final String s=getSettingsModel<xsl:value-of select="$javaName"/>().getStringValue().trim();
			if( s.isEmpty()) return  java.util.Optional.empty();
			try
				{
				return java.util.Optional.of(Integer.parseInt(s));
				}
			catch(Exception err )
				{
				throw new org.knime.core.node.InvalidSettingsException("Bad number for <xsl:value-of select="$javaName"/>:\"" + s + "\"",err);
				}
			}
			</xsl:when>
			<xsl:when test="@type = 'double'">
			
			</xsl:when>
		
		</xsl:choose>
		
		
	</xsl:when>
	
	
	
	<xsl:when test="@type='int'">
		final static int <xsl:apply-templates select="." mode="config-default"/> = <xsl:choose>
			<xsl:when test="@default"><xsl:value-of select="@default"/></xsl:when>
			<xsl:otherwise>0</xsl:otherwise>
			</xsl:choose>;
		protected final SettingsModelInteger <xsl:apply-templates select="." mode="fieldName"/> = new SettingsModelInteger(
			<xsl:apply-templates select="." mode="config-name"/>,
			<xsl:apply-templates select="." mode="config-default"/> 
			);
	</xsl:when>
	
	<xsl:when test="@type='double'">
		final static double <xsl:apply-templates select="." mode="config-default"/> = <xsl:choose>
			<xsl:when test="@default"><xsl:value-of select="@default"/></xsl:when>
			<xsl:otherwise>0"</xsl:otherwise>
			</xsl:choose>;
		protected final SettingsModelDouble <xsl:apply-templates select="." mode="fieldName"/> = new SettingsModelDouble(
			<xsl:apply-templates select="." mode="config-name"/>,
			<xsl:apply-templates select="." mode="config-default"/> 
			);
		/** getter for settings of <xsl:value-of select="@name"/> */
		protected SettingsModelDouble getSettingsModel<xsl:value-of select="$javaName"/>() {
			return this.<xsl:apply-templates select="." mode="fieldName"/>;
			}
		protected double geSettingsModel<xsl:value-of select="$javaName"/>Double() {
			return this.getSettingsModel<xsl:value-of select="$javaName"/>().getDoubleValue();
			}	
			
	</xsl:when>
	
	
	
	<xsl:when test="@type='boolean'">
		final static boolean <xsl:apply-templates select="." mode="config-default"/> = <xsl:choose>
			<xsl:when test="@default"><xsl:value-of select="@default"/></xsl:when>
			<xsl:otherwise>false</xsl:otherwise>
			</xsl:choose>;
		protected final SettingsModelBoolean <xsl:apply-templates select="." mode="fieldName"/> = new SettingsModelBoolean(
			<xsl:apply-templates select="." mode="config-name"/>,
			<xsl:apply-templates select="." mode="config-default"/> 
			);
			
		/** getter for settings of <xsl:value-of select="@name"/> */
		protected SettingsModelBoolean getSettingsModel<xsl:value-of select="$javaName"/>() {
			return this.<xsl:apply-templates select="." mode="fieldName"/>;
			}
		protected boolean isSettingsModel<xsl:value-of select="$javaName"/>Boolean() {
			return this.getSettingsModel<xsl:value-of select="$javaName"/>().getBooleanValue();
			}
			
			
	</xsl:when>
	
		<xsl:when test="@type='string-list' or @type='strings'">
		
		final static String <xsl:apply-templates select="." mode="config-default"/>[] = new String[]{
			<xsl:for-each select="enum/item[@default='true']">
			<xsl:if test="position() &gt; 1">,</xsl:if>
			<xsl:text>"</xsl:text>
			<xsl:value-of select="text()"/>
			<xsl:text>"</xsl:text>
			</xsl:for-each>
			};
		
		protected org.knime.core.node.defaultnodesettings.SettingsModelStringArray  <xsl:apply-templates select="." mode="fieldName"/> = new  org.knime.core.node.defaultnodesettings.SettingsModelStringArray(
			<xsl:apply-templates select="." mode="config-name"/>,
			<xsl:apply-templates select="." mode="config-default"/> 
			);
		
		/** getter for settings of <xsl:value-of select="@name"/> */
		protected org.knime.core.node.defaultnodesettings.SettingsModelStringArray getSettingsModel<xsl:value-of select="$javaName"/>() {
			return this.<xsl:apply-templates select="." mode="fieldName"/>;
			}
		
		
		</xsl:when>
	
	<xsl:otherwise>
		<xsl:message terminate="yes">node2model : unknown setting type: <xsl:value-of select="@name"/> : <xsl:value-of select="@type"/></xsl:message>
	</xsl:otherwise>
</xsl:choose>
/** END SETTING <xsl:value-of select="@name"/> */


</xsl:template>


<xsl:template match="setting" mode="fieldName">
<xsl:value-of select="concat('__',@name)"/>
</xsl:template>

<xsl:template match="setting" mode="config-value">
<xsl:text>"</xsl:text>
<xsl:value-of select="concat('config.',$nodeName,'.',@name)"/>
<xsl:text>"</xsl:text>
</xsl:template>

<xsl:template match="setting" mode="gatk">
<xsl:choose>

	<xsl:when test="@type='string' and @gatk-split='true'">
	
		for(final String s: this.<xsl:apply-templates select="." mode="fieldName"/>.getStringValue().split("[\n\r]") )
    		{
    		if(s.trim().isEmpty()) continue;
    		args.add("--<xsl:value-of select="@name"/>");
    		args.add(s.trim());
    		}
	</xsl:when>


	<xsl:when test="@type='strings' or @type='string-list'">
	
		for(final String s: this.<xsl:apply-templates select="." mode="fieldName"/>.getStringArrayValue())
    		{
    		if(s.isEmpty()) continue;
    		args.add("--<xsl:value-of select="@name"/>");
    		args.add(s);
    		}
	</xsl:when>
	
	<xsl:when test="@type='input-file' or @type='string' or ((@type='double' or @type='int') and @required='false')">
	
		if(!this.<xsl:apply-templates select="." mode="fieldName"/>.getStringValue().trim().isEmpty())
    		{
    		args.add("--<xsl:value-of select="@name"/>");
    		args.add(this.<xsl:apply-templates select="." mode="fieldName"/>.getStringValue());
    		}
	</xsl:when>
	
	<xsl:when test="@type='boolean'">
	
		if(this.<xsl:apply-templates select="." mode="fieldName"/>.getBooleanValue())
    		{
    		args.add("--<xsl:value-of select="@name"/>");
    		}
	</xsl:when>
	
	
	<xsl:when test="@type='double' and @required='true'">
	
		
    		args.add("--<xsl:value-of select="@name"/>");
    		args.add(String.valueOf(this.<xsl:apply-templates select="." mode="fieldName"/>.getDoubleValue()));
    		
	</xsl:when>
	
	<xsl:otherwise>
		<xsl:message terminate="yes">setting[gatk]unknown setting type  <xsl:value-of select="@name"/>:<xsl:value-of select="@type"/>.</xsl:message>
	</xsl:otherwise>
	

</xsl:choose>
</xsl:template>


</xsl:stylesheet>
