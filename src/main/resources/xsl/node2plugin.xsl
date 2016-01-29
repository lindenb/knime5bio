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

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

@Generated("xsl")
public <xsl:if test="$abstract = 'true'">abstract </xsl:if> class <xsl:if test="$abstract = 'true'">Abstract</xsl:if><xsl:value-of select="concat($nodeName,'NodePlugin')"/>
	extends com.github.lindenb.knime5bio.AbstractNodePlugin {
   
   
   	<xsl:choose>
   	<xsl:when test="$abstract = 'true'">
    /**
     * The constructor.
     */
    protected Abstract<xsl:value-of select="concat($nodeName,'NodePlugin')"/>() {
        super();
    }


    /**
     * This method is called when the plug-in is stopped.
     * 
     * @param context The OSGI bundle context
     * @throws Exception If this plugin could not be stopped
     */
    @Override
    public void stop(final BundleContext context) throws Exception
    	{
    	super.stop(context);
    	}


   	</xsl:when>
   	<xsl:otherwise>
   	
    // The shared instance.
    private static <xsl:value-of select="concat($nodeName,'NodePlugin')"/> plugin;

    /**
     * The constructor.
     */
    public <xsl:if test="$abstract = 'true'">abstract</xsl:if><xsl:value-of select="concat($nodeName,'NodePlugin')"/>() {
        super();
        plugin = this;
    }


    /**
     * This method is called when the plug-in is stopped.
     * 
     * @param context The OSGI bundle context
     * @throws Exception If this plugin could not be stopped
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
    }

    /**
     * Returns the shared instance.
     * 
     * @return Singleton instance of the Plugin
     */
    public static <xsl:value-of select="concat($nodeName,'NodePlugin')"/> getDefault() {
        return plugin;
    }

   	
   	
   	</xsl:otherwise>
   	
   	</xsl:choose>
    /**
     * This method is called upon plug-in activation.
     * 
     * @param context The OSGI bundle context
     * @throws Exception If this plugin could not be started
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);

    }
}

</xsl:template>


</xsl:stylesheet>