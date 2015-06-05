ifneq ($(realpath local.mk),)
include local.mk
endif
.PHONY=all build build clean lib:

xsl2java?= ../xslt-sandbox/stylesheets/knime/knime2java.xsl
knime.dir?= ${HOME}/tmp/KNIME/knime_2.11.2
jvarkit.dir?=$(realpath ../jvarkit-git)
htsjdk.dist?= $(realpath ${jvarkit.dir}/htsjdk/dist)
jvarkit.dist?=$(realpath ${jvarkit.dir}/dist)

generated.dir=generated
extra.jars=${htsjdk.dist}/htsjdk-1.133.jar:${htsjdk.dist}/commons-jexl-2.1.1.jar:${htsjdk.dist}/commons-logging-1.1.1.jar:${htsjdk.dist}/snappy-java-1.0.3-rc3.jar:${jvarkit.dist}/jvarkit-1.133.jar:${jvarkit.dir}/lib/BigWig.jar:${jvarkit.dir}/lib/log4j-1.2.15.jar



all: build

run : install
	${knime.dir}/knime -clean

install: build
	rm -f ${knime.dir}/plugins/com.github.lindenb.knime5bio_*.jar
	cp ${generated.dir}/dist/com.github.lindenb.knime5bio_*.jar ${knime.dir}/plugins

build: ${xsl2java} model/knime5bio.xml lib
	rm -rf ${generated.dir}/src ${generated.dir}/tmp
	mkdir -p ${generated.dir}/model
	xsltproc --xinclude \
		-stringparam base.dir ${generated.dir}/model \
		stylesheets/generate-code.xsl \
		model/model.code.xml
	xsltproc --xinclude \
		--path ${generated.dir}/model \
		--stringparam base.dir ${generated.dir} \
		--stringparam extra.source.dir $(realpath src/main/java) \
		--stringparam extra.jars ${extra.jars} \
		${xsl2java} model/knime5bio.xml
	rm -f ${generated.dir}/dist/com.github.lindenb.knime5bio_*.jar
	$(MAKE) -B -C ${generated.dir} knime.root=${knime.dir}

doc: build
	$(MAKE) -C ${generated.dir} $@ knime.root=${knime.dir}

lib:
	(cd ../jvarkit-git && make library)

clean:
	rm -rf ${generated.dir}


