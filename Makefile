ifneq ($(realpath local.mk),)
include local.mk
endif
.PHONY=all build build clean:

xsl2java?= ../xslt-sandbox/stylesheets/knime/knime2java.xsl
knime.dir?= ${HOME}/tmp/KNIME/knime_2.11.2
htsjdk.dist?= $(realpath ../jvarkit-git/htsjdk/dist)
jvarkit.dist?=$(realpath ../jvarkit-git/dist)

generated.dir=generated
extra.jars=${htsjdk.dist}/htsjdk-1.129.jar:${htsjdk.dist}/commons-jexl-2.1.1.jar:${htsjdk.dist}/commons-logging-1.1.1.jar:${htsjdk.dist}/snappy-java-1.0.3-rc3.jar:${jvarkit.dist}/jvarkit-1.129.jar



all: build

run : install
	${knime.dir}/knime -clean

install: build
	rm -f ${knime.dir}/plugins/com.github.lindenb.jvarkit.knime*.jar
	cp ${generated.dir}/dist/com.github.lindenb.knime5bio_*.jar ${knime.dir}/plugins

build: ${xsl2java} model/knime5bio.xml
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
		$^
	$(MAKE) -B -C ${generated.dir} knime.root=${knime.dir}

doc: build
	$(MAKE) -C ${generated.dir} $@ knime.root=${knime.dir}
	
clean:
	rm -rf ${generated.dir}


