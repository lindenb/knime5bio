package com.github.lindenb.knime5bio;

import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeView;

public abstract class AbstractNodeView<T extends NodeModel> extends NodeView<T> {
protected AbstractNodeView(T t)
	{
	super(t);
	}
@Override
protected void onOpen() {		
	}
@Override
protected void onClose() {
	}
@Override
protected void modelChanged() {
	}
}
