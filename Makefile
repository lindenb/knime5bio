ifneq ($(realpath local.mk),)
include local.mk
endif
.PHONY=all build clean:

xsl2java?= ../xslt-sandbox/stylesheets/knime/knime2java.xsl
knime.dir?= ${HOME}/tmp/KNIME/knime_2.11.2
htsjdk.dist?= $(realpath ../jvarkit-git/htsjdk/dist)

generated.dir=generated
extra.jars=${htsjdk.dist}/htsjdk-1.128.jar:${htsjdk.dist}/commons-jexl-2.1.1.jar:${htsjdk.dist}/commons-logging-1.1.1.jar:${htsjdk.dist}/snappy-java-1.0.3-rc3.jar



all: build

run : install
	${knime.dir}/knime -clean

install: build
	rm -f ${knime.dir}/plugins/com.github.lindenb.jvarkit.knime*.jar
	cp ${generated.dir}/dist/com.github.lindenb.jvarkit.knime_*.jar ${knime.dir}/plugins

build: ${xsl2java} model/knime5bio.xml
	mkdir -p ${generated.dir}
	xsltproc --xinclude \
		--stringparam base.dir ${generated.dir} \
		--stringparam extra.source.dir $(realpath src/main/java) \
		--stringparam extra.jars ${extra.jars} \
		$^
	$(MAKE) -C ${generated.dir} knime.root=${knime.dir}
	
clean:
	rm -rf ${generated.dir}

