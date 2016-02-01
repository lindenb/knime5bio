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
import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;

import org.knime.core.node.workflow.FlowVariable.Type;

@Generated("xsl")
public <xsl:if test="$abstract = 'true'">abstract</xsl:if> class <xsl:if test="$abstract = 'true'">Abstract</xsl:if><xsl:value-of select="concat($nodeName,'NodeDialog')"/> extends
	 com.github.lindenb.knime5bio.AbstractNodeDialog
	{
    
    public  <xsl:if test="$abstract = 'true'">Abstract</xsl:if><xsl:value-of select="concat($nodeName,'NodeDialog')"/> () {
   	<xsl:apply-templates select="settings"/>
   	
   	}
		
	}
</xsl:template>

<xsl:template match="settings">
   	<xsl:apply-templates select="setting"/>
</xsl:template>

<xsl:template match="setting">
/** BEGIN SETTING <xsl:value-of select="@name"/> */

	<xsl:choose>
		<xsl:when test="@type='string-list' or @type='strings'">
		final  org.knime.core.node.defaultnodesettings.SettingsModelStringArray  <xsl:value-of select="generate-id(.)"/> = new   org.knime.core.node.defaultnodesettings.SettingsModelStringArray (
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-name"/>,
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-default"/> 		
				);		
		final  org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection  <xsl:value-of select="concat(generate-id(.),'d')"/> =  new org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection (
					<xsl:value-of select="generate-id(.)"/>,
					"<xsl:call-template name="escape">
						<xsl:with-param name="string">
							<xsl:apply-templates select="." mode="label"/>
						</xsl:with-param>
					</xsl:call-template>",//label
					java.util.Arrays.asList(new String[]{<xsl:for-each select="enum/item">
						<xsl:if test="position()&gt;1"> , </xsl:if>
						<xsl:text>"</xsl:text>
						<xsl:value-of select="text()"/>
						<xsl:text>"</xsl:text>
					</xsl:for-each>}),//items
					<xsl:choose>
						<xsl:when test="@multiple='false'">javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION</xsl:when>
						<xsl:when test="@multiple='true'">javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION</xsl:when>
						<xsl:otherwise><xsl:message terminate="yes">@multiple  missing in "<xsl:value-of select="@name"/>".</xsl:message></xsl:otherwise>
					</xsl:choose>,//selection type
					<xsl:choose>
						<xsl:when test="@required"><xsl:value-of select="@required"/></xsl:when>
						<xsl:otherwise><xsl:message terminate="yes">column/@required missing in "<xsl:value-of select="@name"/>".</xsl:message></xsl:otherwise>
					</xsl:choose>,//required
					<xsl:choose>
						<xsl:when test="@visibleRowCount"><xsl:value-of select="@visibleRowCount"/></xsl:when>
						<xsl:otherwise>5</xsl:otherwise>
					</xsl:choose>//visibleRowCount
					);
		</xsl:when>
		
	
	
		<xsl:when test="@type='output-file'">
		final SettingsModelString <xsl:value-of select="generate-id(.)"/> = new SettingsModelString(
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-name"/>,
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-default"/> 		
				);		
		final DialogComponentFileChooser <xsl:value-of select="concat(generate-id(.),'d')"/> =  new DialogComponentFileChooser(
			  <xsl:value-of select="generate-id(.)"/>,
			  <xsl:choose>
				  <xsl:when test="@history-id"><xsl:value-of select="@history-id"/></xsl:when>
				  <xsl:otherwise>"<xsl:value-of select="concat($nodeName,'.',@name)"/>"</xsl:otherwise>
			  </xsl:choose>,//historyID 
			  javax.swing.JFileChooser.SAVE_DIALOG,
			  false//dir only
			  <xsl:for-each select="suffix">
				  <xsl:text>,"</xsl:text>
				  <xsl:value-of select="text()"/>
				  <xsl:text>"</xsl:text>
			  </xsl:for-each>
			  );
		
		
		<xsl:value-of select="concat(generate-id(.),'d')"/>.setBorderTitle("<xsl:call-template name="escape">
						<xsl:with-param name="string">
							<xsl:apply-templates select="." mode="label"/>
						</xsl:with-param>
					</xsl:call-template>");
		//<xsl:value-of select="concat(generate-id(.),'d')"/>.setToolTipText("fastaIn");


		</xsl:when>

	
		<xsl:when test="@type='input-file'">
		final SettingsModelString <xsl:value-of select="generate-id(.)"/> = new SettingsModelString(
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-name"/>,
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-default"/> 		
				);
		final FlowVariableModel <xsl:value-of select="concat(generate-id(.),'f')"/> = createFlowVariableModel(<xsl:value-of select="generate-id(.)"/>.getKey(), Type.STRING);
		
		final DialogComponentFileChooser <xsl:value-of select="concat(generate-id(.),'d')"/> =  new DialogComponentFileChooser(
					<xsl:value-of select="generate-id(.)"/>,
					<xsl:choose>
						<xsl:when test="@history-id"><xsl:value-of select="@history-id"/></xsl:when>
						<xsl:otherwise>"<xsl:value-of select="concat($nodeName,'.',@name)"/>"</xsl:otherwise>
					</xsl:choose>,//historyID 
					javax.swing.JFileChooser.OPEN_DIALOG,
					false,//dir only
					 <xsl:value-of select="concat(generate-id(.),'f')"/>//flow
					<xsl:for-each select="suffix">
						<xsl:text>,"</xsl:text>
						<xsl:value-of select="text()"/>
						<xsl:text>"</xsl:text>
					</xsl:for-each>
					);
				
		<xsl:value-of select="concat(generate-id(.),'d')"/>.setAllowRemoteURLs(true);
		//<xsl:value-of select="concat(generate-id(.),'d')"/>.setToolTipText("fastaIn");

		<xsl:value-of select="concat(generate-id(.),'d')"/>.setBorderTitle("<xsl:call-template name="escape">
						<xsl:with-param name="string">
							<xsl:apply-templates select="." mode="label"/>
						</xsl:with-param>
					</xsl:call-template>");


		</xsl:when>
		
		<xsl:when test="@type='column'">
		final SettingsModelColumnName <xsl:value-of select="generate-id(.)"/> =	new SettingsModelColumnName(
						<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-name"/>,
						<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-default"/> 		
						);
		@SuppressWarnings("unchecked")
		final DialogComponentColumnNameSelection <xsl:value-of select="concat(generate-id(.),'d')"/> =  new DialogComponentColumnNameSelection(
					<xsl:value-of select="generate-id(.)"/>,
					"<xsl:call-template name="escape">
						<xsl:with-param name="string">
							<xsl:apply-templates select="." mode="label"/>
						</xsl:with-param>
					</xsl:call-template>",//label
					<xsl:choose>
						<xsl:when test="@index"><xsl:value-of select="@index"/></xsl:when>
						<xsl:otherwise><xsl:message terminate="yes">column/@index missing</xsl:message></xsl:otherwise>
					</xsl:choose>,//port index
					<xsl:choose>
						<xsl:when test="@required"><xsl:value-of select="@required"/></xsl:when>
						<xsl:otherwise><xsl:message terminate="yes">column/@@required missing</xsl:message></xsl:otherwise>
					</xsl:choose>,//required
					<xsl:choose>
						<xsl:when test="@addNoneCol"><xsl:value-of select="@addNoneCol"/></xsl:when>
						<xsl:otherwise>false</xsl:otherwise>
					</xsl:choose>,//addNoneCol
					<xsl:choose>
						<xsl:when test="@data-type='string'">org.knime.core.data.StringValue.class</xsl:when>
						<xsl:when test="@data-type='int'">org.knime.core.data.IntValue.class</xsl:when>
						<xsl:otherwise><xsl:message terminate="yes">column/@data-type missing or unknown</xsl:message></xsl:otherwise>
					</xsl:choose>
					);
		
		
		</xsl:when>
		
		
		<xsl:when test="@type='string'">
		final SettingsModelString <xsl:value-of select="generate-id(.)"/> = new SettingsModelString(
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-name"/>,
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-default"/> 		
				);
		
		
		<xsl:choose>
		<xsl:when test="@multiline='true'">
		final org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString <xsl:value-of select="concat(generate-id(.),'d')"/> = 
			new org.knime.core.node.defaultnodesettings.DialogComponentMultiLineString(
					<xsl:value-of select="generate-id(.)"/>,
					"<xsl:call-template name="escape">
						<xsl:with-param name="string">
							<xsl:apply-templates select="." mode="label"/>
						</xsl:with-param>
					</xsl:call-template>",
					false/* disallowEmptyString - if set true, the component request a non-empty string from the user. */
					,<xsl:choose>
					  <xsl:when test="@columns"><xsl:value-of select="@columns"/></xsl:when>
					  <xsl:when test="@cols"><xsl:value-of select="@cols"/></xsl:when>
					  <xsl:otherwise>50</xsl:otherwise>
					  </xsl:choose> /* cols */,
					  <xsl:choose>
					  <xsl:when test="@rows"><xsl:value-of select="@rows"/></xsl:when>
					  <xsl:otherwise>10</xsl:otherwise>
					  </xsl:choose> /* rows */
					);
		</xsl:when>
		<!--  TODO https://tech.knime.org/docs/api/org/knime/core/node/defaultnodesettings/DialogComponentOptionalString.html  -->
		<xsl:otherwise>
		final FlowVariableModel <xsl:value-of select="concat(generate-id(.),'f')"/> = createFlowVariableModel(<xsl:value-of select="generate-id(.)"/>.getKey(), Type.STRING);
		
		final org.knime.core.node.defaultnodesettings.DialogComponentString <xsl:value-of select="concat(generate-id(.),'d')"/> = 
			new org.knime.core.node.defaultnodesettings.DialogComponentString(
					<xsl:value-of select="generate-id(.)"/>,
					"<xsl:call-template name="escape">
						<xsl:with-param name="string">
							<xsl:apply-templates select="." mode="label"/>
						</xsl:with-param>
					</xsl:call-template>",
					false,/* disallowEmptyString - if set true, the component request a non-empty string from the user. */
					20,/* width */
					<xsl:value-of select="concat(generate-id(.),'f')"/>
					);		
		</xsl:otherwise>
		</xsl:choose>
		
		</xsl:when>
		
		
		
		<xsl:when test="(@type='double' or @type='int') and not(@required='true')">
		final SettingsModelString <xsl:value-of select="generate-id(.)"/> = new SettingsModelString(
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-name"/>,
				String.valueOf(<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-default"/>) 		
				);
		final FlowVariableModel <xsl:value-of select="concat(generate-id(.),'f')"/> = createFlowVariableModel(<xsl:value-of select="generate-id(.)"/>.getKey(), Type.STRING);
		final org.knime.core.node.defaultnodesettings.DialogComponentString <xsl:value-of select="concat(generate-id(.),'d')"/> = 
			new org.knime.core.node.defaultnodesettings.DialogComponentString(
					<xsl:value-of select="generate-id(.)"/>,
					"<xsl:call-template name="escape">
						<xsl:with-param name="string">
							<xsl:apply-templates select="." mode="label"/>
						</xsl:with-param>
					</xsl:call-template>",
					false,/* disallowEmptyString - if set true, the component request a non-empty string from the user. */
					20,/* width */
					<xsl:value-of select="concat(generate-id(.),'f')"/>
					);		
		
		
		
		</xsl:when>
		
		<xsl:when test="@type='int'">
		final SettingsModelInteger <xsl:value-of select="generate-id(.)"/> = new SettingsModelInteger(
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-name"/>,
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-default"/> 
				);
		final FlowVariableModel <xsl:value-of select="concat(generate-id(.),'f')"/> = createFlowVariableModel(<xsl:value-of select="generate-id(.)"/>.getKey(), Type.INTEGER);
		final DialogComponentNumber <xsl:value-of select="concat(generate-id(.),'d')"/> = new DialogComponentNumber(
			<xsl:value-of select="generate-id(.)"/>,//setting
			"<xsl:call-template name="escape">
				<xsl:with-param name="string">
					<xsl:apply-templates select="." mode="label"/>
				</xsl:with-param>
			</xsl:call-template>",//label
			<xsl:choose>
				<xsl:when test="@step"><xsl:value-of select="@step"/></xsl:when>
				<xsl:otherwise>1</xsl:otherwise>
			</xsl:choose>,//step
			<xsl:choose>
				<xsl:when test="@width"><xsl:value-of select="@width"/></xsl:when>
				<xsl:otherwise>20</xsl:otherwise>
			</xsl:choose>,//compWidth
			<xsl:value-of select="concat(generate-id(.),'f')"/>//flow
			
			);
		
		
		</xsl:when>
		<xsl:when test="@type='boolean'">
		final SettingsModelBoolean <xsl:value-of select="generate-id(.)"/> = new SettingsModelBoolean(
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-name"/>,
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-default"/> 
				);
		
		final DialogComponentBoolean <xsl:value-of select="concat(generate-id(.),'d')"/> = new DialogComponentBoolean(
			<xsl:value-of select="generate-id(.)"/>,
			"<xsl:call-template name="escape">
				<xsl:with-param name="string">
					<xsl:apply-templates select="." mode="label"/>
				</xsl:with-param>
			</xsl:call-template>"//label
			);
		
		</xsl:when>
		

		<xsl:when test="@type='double'">
		final       org.knime.core.node.defaultnodesettings.SettingsModelDouble  <xsl:value-of select="generate-id(.)"/> = new       org.knime.core.node.defaultnodesettings.SettingsModelDouble (
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-name"/>,
				<xsl:value-of select="concat($nodeName,'NodeModel.')"/><xsl:apply-templates select="." mode="config-default"/> 
				);
		final FlowVariableModel <xsl:value-of select="concat(generate-id(.),'f')"/> = createFlowVariableModel(<xsl:value-of select="generate-id(.)"/>.getKey(), Type.DOUBLE);
		
		final DialogComponentNumber <xsl:value-of select="concat(generate-id(.),'d')"/> = new DialogComponentNumber(
			<xsl:value-of select="generate-id(.)"/>,
			"<xsl:call-template name="escape">
				<xsl:with-param name="string">
					<xsl:apply-templates select="." mode="label"/>
				</xsl:with-param>
			</xsl:call-template>",//label
			<xsl:choose>
				<xsl:when test="@step-size">
					<xsl:value-of select="@step-size"/>
				</xsl:when>
				<xsl:otherwise>0.0001</xsl:otherwise>
			</xsl:choose>,//stepSize
			<xsl:choose>
				<xsl:when test="@width">
					<xsl:value-of select="@width"/>
				</xsl:when>
				<xsl:otherwise>10</xsl:otherwise>
			</xsl:choose>,//compWidth
			<xsl:value-of select="concat(generate-id(.),'f')"/>//flow
			);
		
		</xsl:when>

		
		<xsl:otherwise>
			<xsl:message terminate="yes">node2dialog: unknown setting type: <xsl:value-of select="@name"/> : <xsl:value-of select="@type"/></xsl:message>
		</xsl:otherwise>
	</xsl:choose>
	
	<xsl:value-of select="concat(generate-id(.),'d')"/>.setToolTipText("<xsl:call-template name="escape">
		<xsl:with-param name="string">
			<xsl:apply-templates select="." mode="description"/>
		</xsl:with-param>
	</xsl:call-template>");
	
		
	
	this.addDialogComponent( <xsl:value-of select="concat(generate-id(.),'d')"/>);
	
/** END SETTING <xsl:value-of select="@name"/> */
</xsl:template>



</xsl:stylesheet>
