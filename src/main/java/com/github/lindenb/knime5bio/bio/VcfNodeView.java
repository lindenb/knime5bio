/*
The MIT License (MIT)

Copyright (c) 2014 Pierre Lindenbaum

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


History:
* 2014 creation

*/
package com.github.lindenb.knime5bio.bio;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.BufferedDataTableHolder;
import org.knime.core.node.NodeView;

import com.github.lindenb.jvarkit.io.IOUtils;
import com.github.lindenb.jvarkit.util.igv.IgvSocket;
import com.github.lindenb.jvarkit.util.swing.AbstractGenericTable;
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
		
		private JTable genotypeTable;
		private GenotypeTableModel genotypeTableModel;
		
		private JTable infoTable;
		private InfoTableModel infoTableModel;
		
		private AbstractAction openIgvAction=null;
		private IgvSocket igvSocket=new IgvSocket();
		VcfNodeViewComponent(T nodeModel)
			{
			super(new BorderLayout(5, 5));
			this.setBorder(new EmptyBorder(5, 5, 5, 5));
			JPanel contentPane=new JPanel(new BorderLayout(5, 5));
			this.add(contentPane,BorderLayout.CENTER);
			
			this.openIgvAction=new AbstractAction("View in IGV")
				{
				@Override
				public void actionPerformed(ActionEvent e) {
					viewIgv();
					}
				};
			this.openIgvAction.setEnabled(false);
			this.nodeModel=nodeModel;
			DefaultListModel<String> fileListModel=new DefaultListModel<>();
			fileList =  new JList<String>(fileListModel);
			fileList.setPreferredSize(new Dimension(500,1000));
			JScrollPane scroll=new JScrollPane(fileList);
			contentPane.add(scroll,BorderLayout.WEST);
			
			this.vcfTableModel=new DefaultVcfTable();
			this.vcfTable = new JTable(this.vcfTableModel);
			this.vcfTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			this.vcfTable.getSelectionModel().clearSelection();
			scroll=new JScrollPane(this.vcfTable);
			contentPane.add(scroll,BorderLayout.CENTER);
			
			this.genotypeTableModel = new GenotypeTableModel();
			this.genotypeTable = new JTable(this.genotypeTableModel);
			this.genotypeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			this.genotypeTable.getSelectionModel().clearSelection();
			scroll=new JScrollPane(this.genotypeTable);
			contentPane.add(scroll,BorderLayout.EAST);
			
			
			this.infoTableModel = new InfoTableModel();
			this.infoTable = new JTable(this.infoTableModel);
			this.infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			this.infoTable.getSelectionModel().clearSelection();
			scroll=new JScrollPane(this.infoTable);
			contentPane.add(scroll,BorderLayout.SOUTH);

			
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
			
			this.vcfTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.vcfTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e)
					{
					if(e.getValueIsAdjusting()) return;
					int rowIndex = e.getFirstIndex();
					openIgvAction.setEnabled(rowIndex!=-1);
					if(rowIndex<0 || rowIndex>=VcfNodeViewComponent.this.vcfTableModel.getRowCount()) return ;
					VcfNodeViewComponent.this.genotypeTableModel.reset(
							VcfNodeViewComponent.this.vcfTableModel.getVCFHeader(),
							VcfNodeViewComponent.this.vcfTableModel.getVariantContextAt(rowIndex)
							);
					VcfNodeViewComponent.this.infoTableModel.reset(
							VcfNodeViewComponent.this.vcfTableModel.getVCFHeader(),
							VcfNodeViewComponent.this.vcfTableModel.getVariantContextAt(rowIndex)
							);

					}
				});
			
			JPanel top=new JPanel(new FlowLayout());
			this.add(top,BorderLayout.NORTH);
			JButton button=new JButton(this.openIgvAction);
			top.add(button);
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
					if(nsamples==0 ) nsamples=1;
					
					while(r.hasNext() )
						{
						list.add(r.next());
						if(nsamples*list.size()>=10000) break;
						}
					this.vcfTableModel.reset(cah.header,cah.codec);
					this.vcfTableModel.getRows().clear();
					this.vcfTableModel.getRows().addAll(list);
					this.vcfTableModel.fireTableDataChanged();
					}
				else
					{
					this.vcfTableModel.reset(null, null);
					}	
				this.genotypeTableModel.reset(null, null);
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
		private void viewIgv()
			{
			int rowindex=this.vcfTable.getSelectedRow();
			if(rowindex==-1) return;
			final VariantContext ctx=this.vcfTableModel.getVariantContextAt(rowindex);
			if(ctx==null) return;
			this.igvSocket.show(ctx);
			}
		}
	
	@SuppressWarnings("serial")
	private class GenotypeTableModel extends AbstractTableModel
		{
		private VariantContext ctx;
		private List<String> samples = Collections.emptyList();
		private List<VCFFormatHeaderLine> formats = Collections.emptyList();
				
		@Override
		public String getColumnName(int column) {
			
			if(column==0) return "Sample";
			if(formats==null)
				{
				return "";
				}
			return formats.get(column-1).getID();
			}
		@Override
		public int getRowCount() {
			return this.samples.size();
			}
		
		@Override
		public int getColumnCount() {
			return 1+(formats==null?0:formats.size());
			}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
			{
			if(this.ctx==null || rowIndex<0 || rowIndex>=this.samples.size()) return null;
			String sampleName=  this.samples.get(rowIndex);
			if(columnIndex==0)
				{
				return sampleName ;
				}
			Genotype g = this.ctx.getGenotype(sampleName);
			if( g == null ) return null;
			VCFFormatHeaderLine h = this.formats.get(columnIndex-1);
			Object o = g.getAnyAttribute(h.getID());
			if(o!=null && (o instanceof int[]))
				{
				int d[]=(int[])o;
				StringBuilder sb=new StringBuilder();
				for(int i:d)
					{
					if(sb.length()>0) sb.append(",");
					sb.append(i);
					}
				return sb.toString();
				}
			else if(o!=null && o.getClass().isArray())
				{
				return Arrays.toString((Object[])o);
				}
			return String.valueOf(o);
			}
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex)
				{
				default: return String.class;
				}
			}
		
		public void reset(VCFHeader header,VariantContext ctx)
			{
			this.ctx = ctx;
			if(header==null || ctx==null)
				{
				this.samples = Collections.emptyList();
				this.formats = Collections.emptyList();
				}
			else
				{
				this.samples = header.getSampleNamesInOrder();
				this.formats = new ArrayList<>(header.getFormatHeaderLines());
				}
			fireTableStructureChanged();
			}
		}
	private static class IdValueDesc
		{
		String id;
		Object value;
		VCFInfoHeaderLine h;
		}

	@SuppressWarnings("serial")
	private class InfoTableModel extends AbstractGenericTable<IdValueDesc>
		{
		private VCFHeader header=null;
				
		@Override
		public String getColumnName(int column)
			{
			switch(column)
				{
				case 0: return "Key";
				case 1: return "Value";
				case 2: return "Description";
				}
			return null;
			}
		@Override
		public int getColumnCount() {
			return 3;
			}
		
		@Override
		public Object getValueOf(IdValueDesc o, int columnIndex) {
			if(o==null || this.header==null) return null;
			switch(columnIndex)
				{	
				case 0: return o.id;
				case 1: return o.value;
				case 2: return o.h.getDescription();
				}
			return null;
			}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex)
				{
				case 0: case 2: return String.class;
				default: return Object.class;
				}
			}
		
		public void reset(VCFHeader header,VariantContext ctx)
			{
			this.getRows().clear();
			if(header==null || ctx==null)
				{
				this.header=null;
				}
			else
				{
				this.header=header;
				for(VCFInfoHeaderLine h:header.getInfoHeaderLines())
					{
					for(Object o: VCFUtils.attributeAsList(ctx.getAttribute(h.getID())))
						{
						IdValueDesc ivd=new IdValueDesc();
						ivd.id=h.getID();
						ivd.value=o;
						ivd.h=h;
						this.getRows().add(ivd);
						}
					}
				}
			fireTableDataChanged();
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
