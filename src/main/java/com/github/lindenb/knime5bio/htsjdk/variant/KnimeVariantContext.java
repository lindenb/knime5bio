package com.github.lindenb.knime5bio.htsjdk.variant;

import org.knime.core.data.DataRow;
import htsjdk.variant.variantcontext.VariantContext;

public class KnimeVariantContext extends VariantContext {
	private final DataRow dataRow;
	private static final long serialVersionUID = 1L;

	public KnimeVariantContext(final VariantContext other,final DataRow dataRow) {
		super(other);
		this.dataRow = dataRow;
	}
	
	public DataRow getDataRow() {
		return dataRow;
	}


}
