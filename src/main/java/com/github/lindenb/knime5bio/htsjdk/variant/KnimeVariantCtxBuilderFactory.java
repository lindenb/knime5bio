package com.github.lindenb.knime5bio.htsjdk.variant;

import org.knime.core.data.DataRow;

import com.github.lindenb.jvarkit.util.vcf.VariantContextBuilderFactory;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;

public class KnimeVariantCtxBuilderFactory implements VariantContextBuilderFactory {
	
	public KnimeVariantCtxBuilderFactory() {
	}
	
	@Override
	public Builder newVariantContextBuilder() {
		return new Builder();
	}
	@Override
	public Builder newVariantContextBuilder(VariantContext ctx) {
		return new Builder(ctx);
	}
	
	
	public static class Builder  extends VariantContextBuilder 
	{
	private DataRow _dataRow = null;
	public Builder() {
	}

	public Builder(final VariantContext parent) {
		super(parent);
		if( parent instanceof KnimeVariantContext) {
			this._dataRow = KnimeVariantContext.class.cast(parent).getDataRow();
		}
	}
		
	public Builder dataRow(final DataRow row) {
		this._dataRow=row;
		return this;
	}
		
	
	@Override
	public VariantContext make(boolean leaveModifyableAsIs) {
		return new KnimeVariantContext(super.make(leaveModifyableAsIs),this._dataRow);
	}
	}
}
