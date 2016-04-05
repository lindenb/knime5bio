package com.github.lindenb.knime5bio.vcf.burdensplitter;
import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;

import com.github.lindenb.jvarkit.tools.burden.VcfBurdenSplitter;
import com.github.lindenb.jvarkit.util.vcf.VCFUtils;
import com.github.lindenb.jvarkit.util.vcf.VcfIterator;

import htsjdk.samtools.util.CloserUtil;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.writer.VariantContextWriter;
import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFHeaderLine;


public class BurdenSplitterNodeModel extends AbstractBurdenSplitterNodeModel {
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable inputTable, final ExecutionContext exec) throws Exception {
		final VcfBurdenSplitter application = new VcfBurdenSplitter();
		final Set<File> infiles= super.collectFilesInOneColumn(inputTable, super.__vcf);
		if(infiles.size()!=1) {
     		throw new RuntimeException("Expected ONE VCF file in table but got "+infiles.size());
		}
		final File vcfFile = infiles.iterator().next();
		super.assureNodeWorkingDirectoryExists();
		final File zipFile = createFileForWriting(Optional.empty(),".zip");
		zipFile.deleteOnExit();
		ZipInputStream zin = null;
		VcfIterator in = null;
		VariantContextWriter w=null;
		final DataTableSpec outspec = super.createOutTableSpec0();
		BufferedDataContainer container = null;
		try {
			
			application.setOutputFile(zipFile);
			application.setCasesFile(super.getSettingsModelCasesFileFile());
			application.setControlsFile(super.getSettingsModelControlsFileFile());
			application.setMaxRecordsInRam(super.__maxRecordsInRam.getIntValue());
			application.setAcceptFiltered(false);
			
			application.setIgnoreVepFeature(true);
			application.setIgnoreVepHgnc(true);
			application.setIgnoreVepEnsg(true);
			application.setIgnoreVepEnst(true);
			application.setIgnoreVepEnsp(true);
			application.setIgnoreVepSymbol(true);
			application.setIgnoreVepRefSeq(true);
			
			application.setIgnoreAllNM(false);
			application.setIgnoreAllRefSeq(false);
			application.setIgnoreAllEnst(false);
			application.setIgnoreAllTranscript(false);
			application.setSplitterName(super.__splittype.getStringValue());

			application.setTmpdir(getNodeWorkingDirectory());
			checkEmptyListOfThrowables(application.call());
			super.checkEmptyListOfThrowables(application.initializeKnime());
			super.checkEmptyListOfThrowables(application.doVcfToVcf(vcfFile.getPath()));
			application.disposeKnime();
			
			
			container = exec.createDataContainer(outspec);
			long rowIndex=0L;
			zin  = new ZipInputStream(new FileInputStream(zipFile));
			for(;;) {
				final ZipEntry entry = zin.getNextEntry();
				if(entry==null) break;
				if(!entry.isDirectory() && entry.getName().endsWith(".vcf")) {
					getLogger().info("unpacking "+entry.getName());
					in  = VCFUtils.createVcfIteratorFromStream(zin);
					final File vcfFileOut = super.createFileForWriting(Optional.of(entry.getName()), ".vcf.gz");
					final VCFHeader header= in.getHeader();
					
					String chrom = null;
					Integer chromStart = null;
					Integer chromEnd = null;
					Double fisher = null;
					VCFHeaderLine ohl = header.getOtherHeaderLine(VcfBurdenSplitter.VCF_HEADER_FISHER_VALUE);
					if(ohl!=null) {
						fisher = Double.parseDouble(ohl.getValue());
					}
					String splitkey=entry.getName();
					header.getOtherHeaderLine(VcfBurdenSplitter.VCF_HEADER_SPLITKEY);
					if(ohl!=null) {
						splitkey = ohl.getValue();
					}
					
					int totalVariants =0;
					int unfilteredVariants =0;
					w = VCFUtils.createVariantContextWriter(vcfFileOut);
					while(in.hasNext()) {
						final VariantContext ctx = in.next();
						chrom = ctx.getContig();
						if(chromStart==null || chromStart+1> ctx.getStart()) chromStart = ctx.getStart();
						if(chromEnd==null || chromEnd< ctx.getEnd()) chromEnd = ctx.getEnd();
						totalVariants++;
						if(ctx.isNotFiltered()) unfilteredVariants++;
						w.add(ctx);
						}
					w.close(); w=null;
					in.close();in=null;
					rowIndex++;
					final DataRow row = new DefaultRow(RowKey.createRowKey(rowIndex),
							super.createDataCellsForOutTableSpec0(chrom,chromStart, chromEnd,
							splitkey,		
							totalVariants,unfilteredVariants,fisher,vcfFileOut.getPath()));
					container.addRowToTable(row);
					}
				}
			zin.close();zin=null;
			zipFile.delete();
			final BufferedDataTable table = container.getTable();
			container=null;
			return new BufferedDataTable[]{table};
		} catch (Exception e) {
			CloserUtil.close(zin);
			CloserUtil.close(w);
			getLogger().error("VcfBurdenSplitter", e);
			this.removeTmpNodeFiles();
			throw e;
			}
		finally {
			CloserUtil.close(zin);
			CloserUtil.close(w);
			zipFile.delete();
		}
		
		}
	
}
