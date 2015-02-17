.PHONY=all build clean:
generated.dir=generated
knime.dir=${HOME}/tmp/KNIME/knime_2.11.2

all: build

run : install
	${knime.dir}/knime -clean

install: build
	rm -f ${knime.dir}/plugins/com.github.lindenb.jvarkit.knime*.jar
	cp ${generated.dir}/dist/com.github.lindenb.jvarkit.knime_*.jar ${knime.dir}/plugins

build: ../xslt-sandbox/stylesheets/knime/knime2java.xsl model/knime5bio.xml
	rm -rf ${generated.dir}
	mkdir -p ${generated.dir}
	xsltproc --xinclude --stringparam base.dir ${generated.dir}  $^
	$(MAKE) -C ${generated.dir} knime.root=${knime.dir}
	
clean:
	rm -rf ${generated.dir}
