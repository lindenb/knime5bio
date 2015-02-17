package com.github.knime5bio.vcf;
import org.knime.core.node.*;
import org.knime.core.data.*;
import org.knime.core.data.def.*;

import htsjdk.variant.vcf.VCFFileReader;

public abstract class AbstractVcfFilterNodeModel
	extends com.github.lindenb.knime5bio.AbstractNodeModel
	{
	protected AbstractVcfFilterNodeModel(int inport,int outport)
		{
		/* super(inport,outport) */
		super(inport,outport);
		}
	
	protected VCFFileReader openxx()
		{
		return null;
		}
    
    
   

	}
