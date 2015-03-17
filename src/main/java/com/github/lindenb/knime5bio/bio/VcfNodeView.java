package com.github.lindenb.knime5bio.bio;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.tribble.readers.LineIterator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.BufferedDataTableHolder;
import org.knime.core.node.NodeView;

import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.swing.DefaultVcfTable;
import com.github.lindenb.knime5bio.AbstractNodeModel;

public class VcfNodeView<T extends AbstractNodeModel>
	extends NodeView<T>
	{
	@SuppressWarnings("serial")
	private class VcfNodeViewComponent extends JPanel
		{	
		private T nodeModel=null;
		private JList<String> fileList;
		private JTable vcfTable;
		private DefaultVcfTable vcfTableModel;
		
		VcfNodeViewComponent(T nodeModel)
			{
			super(new BorderLayout(5, 5));
			this.nodeModel=nodeModel;
			DefaultListModel<String> fileListModel=new DefaultListModel<>();
			fileList =  new JList<String>(fileListModel);
			fileList.setPreferredSize(new Dimension(500,1000));
			JScrollPane scroll=new JScrollPane(fileList);
			this.add(scroll,BorderLayout.WEST);
			
			this.vcfTableModel=new DefaultVcfTable();
			this.vcfTable = new JTable(this.vcfTableModel);
			this.vcfTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			this.vcfTable.getSelectionModel().clearSelection();
			scroll=new JScrollPane(this.vcfTable);
			this.add(scroll,BorderLayout.CENTER);
			
			this.fileList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.fileList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
				{
				@Override
				public void valueChanged(ListSelectionEvent e)
					{
					if(e.getValueIsAdjusting()) return;
					int rowIndex = e.getFirstIndex();
					if(rowIndex<0) return ;
					@SuppressWarnings("unchecked")
					DefaultListModel<String> model=DefaultListModel.class.cast(fileList.getModel());
					if(rowIndex>=model.getSize()) return;
					String f = model.elementAt(rowIndex);
					reloadVcfFile(f);
					}
				});
			reloadFileList();
			}
		
		private void reloadVcfFile(String uri)
			{
			LineIterator r=null;
			List<String> list=new ArrayList<String>();
			try
				{
				if(uri!=null && !uri.trim().isEmpty())
					{
					r= IOUtils.openURIForLineIterator(uri);
					VCFUtils.CodecAndHeader cah=VCFUtils.parseHeader(r);
					int nsamples=cah.header.getNGenotypeSamples();
					while(r.hasNext() )
						{
						list.add(r.next());
						if(nsamples*list.size()>=10000) break;
						}
					this.vcfTableModel.reset(cah.header,cah.codec);
					this.vcfTableModel.getRows().addAll(list);
					this.vcfTableModel.fireTableDataChanged();
					}
				else
					{
					this.vcfTableModel.reset(null, null);
					}	
				}
			catch(Exception err)
				{
				err.printStackTrace();
				this.vcfTableModel.reset(null,null);
				this.vcfTableModel.fireTableDataChanged();
				}
			finally
				{
				CloserUtil.close(r);
				}
			}
		
		private void reloadFileList()
			{
			@SuppressWarnings("unchecked")
			DefaultListModel<String> model=DefaultListModel.class.cast(fileList.getModel());
			model.removeAllElements();
			if(!(this.nodeModel instanceof BufferedDataTableHolder)) return;
			BufferedDataTable tables[]=BufferedDataTableHolder.class.cast(nodeModel).getInternalTables();
			if(tables!=null && tables.length>0)
				{
				CloseableRowIterator iter=null;
				try
					{
					iter=tables[0].iterator();
					while(iter.hasNext())
						{
						DataRow row=iter.next();
						if(row.getNumCells()==0) break;
						DataCell cell=row.getCell(0);
						if(cell.isMissing()) continue;
						if(!cell.getType().equals(StringCell.TYPE)) continue;
						String uri = StringCell.class.cast(cell).getStringValue();
						if(uri.isEmpty()) continue;
						if(!(uri.endsWith(".vcf") || uri.endsWith(".vcf.gz"))) continue;
						model.addElement(uri);
						}
					}
				catch(Exception err)
					{
					err.printStackTrace();
					}
				finally
					{
					CloserUtil.close(iter);
					}
				}
			}
		
		}
	
	private VcfNodeViewComponent vcfNodeViewComponent=null;
	
	public VcfNodeView(T nodeModel)
		{
		super(nodeModel);
		super.setViewTitleSuffix("VCF");
		this.vcfNodeViewComponent=new VcfNodeViewComponent(nodeModel);
		this.setComponent(this.vcfNodeViewComponent);
		}		
	
	@Override
	protected void onClose()
		{
		}

	@Override
	protected void onOpen()
		{

		}

	@Override
	protected void modelChanged()
		{
		this.vcfNodeViewComponent.reloadFileList();
		}
	
	
	}
