package com.github.lindenb.knime5bio.util.echo;

import java.io.BufferedReader;
import java.io.StringReader;

import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import htsjdk.samtools.util.CloserUtil;


public class EchoNodeModel extends AbstractEchoNodeModel {
@Override
    protected BufferedDataTable[] execute(final ExecutionContext exec) throws Exception
 		{
		BufferedDataContainer bdc = null;
		long count = 0L;
		try {
			String line;
			bdc = exec.createDataContainer(super.createOutTableSpec0());
			BufferedReader in = new BufferedReader(new StringReader(this.__echoString.getStringValue()));
			while ((line = in.readLine()) != null) {
				bdc.addRowToTable(new DefaultRow(
						RowKey.createRowKey(++count),
						createDataCellsForOutTableSpec0(line))
						);
				}
			in.close();
			bdc.close();
			final BufferedDataTable table = bdc.getTable();
			bdc.close();
			bdc = null;
			return new BufferedDataTable[] { table };
		} catch (Exception e) {
			getLogger().error("Echo Error", e);
			throw e;
		} finally {
			CloserUtil.close(bdc);
		}
	}
}
