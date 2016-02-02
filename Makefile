.PHONY:all clean install run
SHELL=/bin/bash
this.makefile=$(lastword $(MAKEFILE_LIST))
this.dir=$(dir $(realpath ${this.makefile}))

#need local settings ? create a file 'local.mk' in this directory
ifneq ($(realpath local.mk),)
include $(realpath local.mk)
endif

# proxy for curl, etc...
curl.proxy=$(if ${http.proxy.host}${http.proxy.port},-x "${http.proxy.host}:${http.proxy.port}",)


EMPTY :=
SPACE := $(EMPTY) $(EMPTY)
COMMA :=,
dist.dir?=${this.dir}dist
src.dir=${this.dir}src/main/java
tmp.dir?=${this.dir}tmp
JAVAC?=javac
JAR?=jar
JAVA?=java


plugin.version=2016.01.21
knime.root?= ${HOME}/package/knime_3.1.0

htsjdk.root?=${HOME}/src/htsjdk
htsjdk.jars=$(realpath $(addsuffix .jar,$(addprefix ${htsjdk.root}/dist/,apache-ant-1.8.2-bzip2 commons-compress-1.4.1 commons-jexl-2.1.1 commons-logging-1.1.1 htsjdk-2.0.1 ngs-java-1.2.2 snappy-java-1.0.3-rc3 xz-1.5)))


ifneq ($(words ${htsjdk.jars}),8)
$(error expected count($$htsjdk.jar)=8 but got '$(words ${htsjdk.jars})' for ${htsjdk.jars})
endif

jvarkit.root?=${HOME}/src/jvarkit
ifeq ($(realpath ${jvarkit.root}),)
$(error cannot find $$jvarit.root = ${jvarkit.root})
endif
jvarkit.jars?=$(shell find $(realpath ${jvarkit.root})/dist* -type f -name "vcfcomparecallers.jar" -o -name "groupbygene.jar" -o -name "vcfpeekvcf.jar" -o -name "vcfmulti2oneinfo.jar" -o -name "vcffilterso.jar" -o -name "vcfmulti2oneallele.jar" -o -name "vcffilterjs.jar" -o -name "bioalcidae.jar"  -o -name "vcfindextabix.jar" ) \
	$(realpath $(addprefix ${jvarkit.root}/,lib/commons-cli/commons-cli/1.3.1/commons-cli-1.3.1.jar lib/org/slf4j/slf4j-api/1.7.13/slf4j-api-1.7.13.jar lib/org/slf4j/slf4j-simple/1.7.13/slf4j-simple-1.7.13.jar ))
	


ifneq ($(words ${jvarkit.jars}),12)
$(error expected count($$jvarkit.jars)=12 but got '$(words ${jvarkit.jars})' for ${jvarkit.jars})
endif


ifeq ($(realpath ${knime.root}),)
$(error cannot find $$knime.root = ${knime.root})
endif

knime.jars=$(shell find ${knime.root}/plugins -type f -name "knime-core.jar" -o -name "xbean*.jar" -o -name "org.knime.core.util_*.jar" -o -name "org.eclipse.osgi_*.jar"  -o -name "org.eclipse.core.runtime_*.jar")

ifneq ($(words ${knime.jars}),6)
$(error expected count($$knime.jars)=6 but got '$(words ${knime.jars})' for ${knime.jars})
endif


extra.jars=${htsjdk.jars} ${jvarkit.jars}



#ifeq ($(gatk.jar),) 
#$(error variable gatk.jar is not defined)
#endif

## $(1) : path to XML without suffix '.xml'
## $(2) : model is abstract
## $(3) : dialog is abstract
## $(4) : plugin is abstract


define generatecode

$${this.dir}src/main/generated-code/java/$(1).preproc.xml  : $${this.dir}src/main/java/$(1).xml
	mkdir -p $$(dir $$@)
	xsltproc $${this.dir}src/main/resources/xsl/node.preproc.xsl $$< |\
	xsltproc $${this.dir}src/main/resources/xsl/node.preproc.xsl - |\
	xsltproc $${this.dir}src/main/resources/xsl/node.preproc.xsl - |\
	xsltproc $${this.dir}src/main/resources/xsl/node.preproc.xsl - |\
	xsltproc --output "$$@" $${this.dir}src/main/resources/xsl/node.preproc.xsl -

$${this.dir}src/main/java/$$(dir $(1))$$(notdir $(1))NodeModel.java : $${this.dir}src/main/generated-code/java/$$(dir $(1))$$(if $(2),Abstract)$$(notdir $(1))NodeModel.java



$${this.dir}src/main/generated-code/java/$$(dir $(1))$$(if $(2),Abstract)$$(notdir $(1))NodeModel.java : $${this.dir}src/main/generated-code/java/$(1).preproc.xml \
		$${this.dir}src/main/generated-code/java/$$(dir $(1))$$(if $(3),Abstract)$$(notdir $(1))NodeDialog.java \
		$${this.dir}src/main/generated-code/java/$$(dir $(1))$$(notdir $(1))NodeFactory.java \
		$${this.dir}src/main/generated-code/java/$$(dir $(1))$$(if $(4),Abstract)$$(notdir $(1))NodePlugin.java \
		$${this.dir}src/main/generated-code/java/$$(dir $(1))$$(notdir $(1))NodeFactory.xml \
		$${this.dir}src/main/generated-code/java/$$(dir $(1))default.png
	mkdir -p $$(dir $$@)
	xsltproc --output "$$@" \
		--stringparam package "$$(subst /,.,$$(subst /$$(notdir $(1)),,$(1)))" \
		--stringparam nodeName "$$(notdir $(1))" \
		--stringparam abstract $$(if $(2),true,false) \
		$${this.dir}src/main/resources/xsl/node2model.xsl "$$<"


$${this.dir}src/main/generated-code/java/$$(dir $(1))$$(if $(3),Abstract)$$(notdir $(1))NodeDialog.java : $${this.dir}src/main/generated-code/java/$(1).preproc.xml
	mkdir -p $$(dir $$@)
	xsltproc --output "$$@" \
		--stringparam package "$$(subst /,.,$$(subst /$$(notdir $(1)),,$(1)))" \
		--stringparam nodeName "$$(notdir $(1))" \
		--stringparam abstract $$(if $(3),true,false) \
		$${this.dir}src/main/resources/xsl/node2dialog.xsl "$$<"

$${this.dir}src/main/generated-code/java/$$(dir $(1))$$(notdir $(1))NodeFactory.java : $${this.dir}src/main/generated-code/java/$(1).preproc.xml
	mkdir -p $$(dir $$@)
	xsltproc --output "$$@" \
		--stringparam package "$$(subst /,.,$$(subst /$$(notdir $(1)),,$(1)))" \
		--stringparam nodeName "$$(notdir $(1))" \
		$${this.dir}src/main/resources/xsl/node2factory.xsl "$$<"

$${this.dir}src/main/generated-code/java/$$(dir $(1))$$(notdir $(1))NodeFactory.xml : $${this.dir}src/main/generated-code/java/$(1).preproc.xml
	mkdir -p $$(dir $$@)
	xsltproc --output "$$@" \
		--stringparam package "$$(subst /,.,$$(subst /$$(notdir $(1)),,$(1)))" \
		--stringparam nodeName "$$(notdir $(1))" \
		$${this.dir}src/main/resources/xsl/node2xml.xsl "$$<"


$${this.dir}src/main/generated-code/java/$$(dir $(1))$$(if $(4),Abstract)$$(notdir $(1))NodePlugin.java : $${this.dir}src/main/generated-code/java/$(1).preproc.xml
	mkdir -p $$(dir $$@)
	xsltproc --output "$$@" \
		--stringparam package "$$(subst /,.,$$(subst /$$(notdir $(1)),,$(1)))" \
		--stringparam abstract $$(if $(4),true,false) \
		--stringparam nodeName "$$(notdir $(1))" \
		$${this.dir}src/main/resources/xsl/node2plugin.xsl "$$<"
		
$${this.dir}src/main/generated-code/java/$$(dir $(1))default.png: $${this.dir}src/main/generated-code/java/$(1).preproc.xml
	mkdir -p $$(dir $$@)
	xsltproc  $${this.dir}src/main/resources/xsl/node2icon.xsl "$$<" | base64 -d  > "$$@"

endef

all: ${dist.dir}/com.github.lindenb.knime5bio_${plugin.version}.jar

$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/veprest/VepRest,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/vcf2table/VcfToTable,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/sortingindex/SortingIndex,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/cmpcallers/CmpCallers,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/util/file2table/FileToTable,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/util/head/Head,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/util/echo/Echo,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/groupbygene/GroupByGene,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/expandvep/ExpandVep,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/extractinfo/ExtractInfo,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/vcfpeekvcf/VcfPeekVcf,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/filterso/FilterSO,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/table2vcf/TableToVcf,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/multi2oneallele/Multi2OneAllele,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/multi2oneinfo/Multi2OneInfo,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/filterjs/VcfFilterJs,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/indexvcf/IndexVcf,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/vcf/bioalcidae/BioAlcidae,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/fasta/readfasta/ReadFasta,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/fasta/writefasta/WriteFasta,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/gatk/selectvariants/SelectVariants,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/gatk/variantfiltration/VariantFiltration,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/gatk/combinevariants/CombineVariants,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/gatk/variants2table/VariantsToTable,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/gatk/leftalign/LeftAlignAndTrimVariants,1,,,,,))
$(eval $(call generatecode,com/github/lindenb/knime5bio/gatk/phasebytransmission/PhaseByTransmission,1,,,,,))

	

run : install
	${knime.root}/knime -clean

install: ${dist.dir}/com.github.lindenb.knime5bio_${plugin.version}.jar
	rm -f ${knime.root}/dropins/com.github.lindenb.knime5bio_*.jar
	cp $< ${knime.root}/dropins/



${dist.dir}/com.github.lindenb.knime5bio_${plugin.version}.jar :${dist.dir}/com_github_lindenb_knime5bio.jar ${extra.jars}
	rm -f $@ 
	mkdir -p ${tmp.dir}/META-INF $(dir $@)
	cat ${this.dir}src/main/resources/manifest/manifest.txt | \
		sed 's%___VERSION___%${plugin.version}%' |\
		sed 's%___EXTRAJARS___%$(subst $(SPACE),$(COMMA),$(notdir ${extra.jars}))%g' |\
		awk -f ${this.dir}src/main/resources/awk/manifest.awk > ${tmp.dir}/META-INF/MANIFEST.MF
	cat  ${tmp.dir}/META-INF/MANIFEST.MF
	cp ${this.dir}src/main/resources/xml/plugin.xml ${tmp.dir}
	echo "##$^##"
	cp $(filter %.jar,$^) ${tmp.dir}
	jar cmvf ${tmp.dir}/META-INF/MANIFEST.MF $@  -C ${tmp.dir} .
	rm -rf ${tmp.dir}

${dist.dir}/com_github_lindenb_knime5bio.jar : $(sort ${knime.jars} ${extra.jars} ) \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/util/file2table/FileToTableNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/util/head/HeadNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/util/echo/EchoNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/cmpcallers/CmpCallersNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/indexvcf/IndexVcfNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/sortingindex/SortingIndexNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/groupbygene/GroupByGeneNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/expandvep/ExpandVepNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/extractinfo/ExtractInfoNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/vcfpeekvcf/VcfPeekVcfNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/filterso/FilterSONodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/vcf2table/VcfToTableNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/table2vcf/TableToVcfNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/multi2oneallele/Multi2OneAlleleNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/multi2oneinfo/Multi2OneInfoNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/filterjs/VcfFilterJsNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/bioalcidae/BioAlcidaeNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/gatk/selectvariants/SelectVariantsNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/gatk/variantfiltration/VariantFiltrationNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/gatk/variants2table/VariantsToTableNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/gatk/combinevariants/CombineVariantsNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/gatk/leftalign/LeftAlignAndTrimVariantsNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/gatk/phasebytransmission/PhaseByTransmissionNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/fasta/writefasta/WriteFastaNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/fasta/readfasta/ReadFastaNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/veprest/VepRestNodeModel.java
	mkdir -p $(dir $@) ${tmp.dir}
	${JAVAC} -Xlint -d ${tmp.dir} -g -classpath "$(subst $(SPACE),:,$(filter %.jar,$^))" -sourcepath ${this.dir}src/main/java:${this.dir}src/main/generated-code/java $(filter %.java,$^)
	(cd ${this.dir}src/main/generated-code/java && find -type f \( -name "*Factory.xml" -o -name "*.png" \) -exec cp --parent '{}' ${tmp.dir} ';')
	jar cf $@ -C ${tmp.dir} .
	rm -rf ${tmp.dir}

${dist.dir}/jvarkit.jar: ${jvarkit.jars}
	mkdir -p $(dir $@) ${tmp.dir}
	$(foreach J,$^,unzip -o -d ${tmp.dir} "${J}";)
	jar cf $@ -C ${tmp.dir} .
	rm -rf ${tmp.dir}


${this.dir}src/main/generated-code/java/com/github/lindenb/gatknime/GATKVersion.java: ${tmp.dir}/GATK_public.key
	mkdir -p $(dir $@)
	grep '^version' "${tmp.dir}/META-INF/maven/org.broadinstitute.gatk/gatk-tools-public/pom.properties" |\
	cut -d'=' -f2 | awk 'BEGIN{V="";} {V=$$1;} END{printf("package com.github.lindenb.gatknime;\npublic class GATKVersion{public static String getVersion() { return \"%s\";}}\n",V);}' > $@


${tmp.dir}/GATK_public.key : ${gatk.jar}
	mkdir -p ${tmp.dir}
	unzip -o ${gatk.jar}  -d ${tmp.dir}
	mv ${tmp.dir}/META-INF/MANIFEST.MF ${tmp.dir}/META-INF/MANIFEST.old
	touch -c $@

clean:
	rm -rf "${tmp.dir}"
