
# Compilation

Requirements:

   * GNU make >= 3.81
   * java JDK 1.8
   * xsltproc

Tested with **GATK 3.5** .

Create a new file `local.mk` (in the `Makefile` directory) containing a property **gatk.jar** defining the path to the GATK jar.

```make
gatk.jar=/dir1/dir2/GenomeAnalysisTK.jar
```

Then run 

```bash
$ make
```

# Procedure to add a new node

* in **Makefile** add code generation

```make
$(eval $(call generatecode,com/github/lindenb/knime5bio/gatk/selectvariants/SelectVariants,1,,,,))
```

* in **Makefile** add dependency to ${dist.dir}/com_github_lindenb_knime5bio.jar

```make
${dist.dir}/com_github_lindenb_knime5bio.jar : $(sort ${knime.jars} ${extra.jars} ) \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/vcf/vcf2table/VcfToTableNodeModel.java \
	${this.dir}src/main/java/com/github/lindenb/knime5bio/gatk/selectvariants/SelectVariantsNodeModel.java \
	(...)
```
* in **src/main/resources/xml/plugin.xml** add node

```xml
<node category-path="/community/bio/gatk"
		    	  factory-class="com.github.lindenb.knime5bio.gatk.selectvariants.SelectVariantsNodeFactory"
		    	  id="com.github.lindenb.knime5bio.gatk.selectvariants.SelectVariantsNodeFactory"
				/>
```


