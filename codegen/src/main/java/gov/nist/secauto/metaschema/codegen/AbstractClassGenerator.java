package gov.nist.secauto.metaschema.codegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.codegen.context.ClassContext;

public abstract class AbstractClassGenerator {
	private static final Logger logger = LogManager.getLogger(AbstractClassGenerator.class);

	private final String packageName;
	private final String className;
	private final List<FlagInstanceGenerator> flags;

	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return className;
	}

	public List<FlagInstanceGenerator> getFlags() {
		return flags;
	}

	public static abstract class Builder<C extends AbstractClassGenerator, B extends Builder<C, B>> {
        private String packageName;
        private String className;
        private List<FlagInstanceGenerator> flags = new LinkedList<>();
 
        @SuppressWarnings( "unchecked" )
		public B packageName(String name) {
			this.packageName = name;
			return (B)this;
		}

        @SuppressWarnings( "unchecked" )
		public B className(String name) {
			this.className = name;
			return (B)this;
		}
 
		@SuppressWarnings( "unchecked" )
		public B flag(FlagInstanceGenerator flagGenerator) {
			this.flags.add(flagGenerator);
			return (B)this;
		}

		public abstract C build();
    }
  
    protected AbstractClassGenerator(Builder<?, ?> builder) {
        this.packageName = builder.packageName;
        this.className = builder.className;
        this.flags = builder.flags.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(builder.flags);
    }

	protected void processInstances(ClassContext classContext) {
		for (FlagInstanceGenerator flag : getFlags()) {
			classContext.newFlagInstance(flag);
		}
	}

	public void writeClass(File outputDir)
			throws IOException {
		File packageDir = new File(outputDir, getPackageName().replace(".", "/"));

		if (packageDir.mkdirs()) {
			logger.info("Created package directory '{}'", packageDir.getAbsolutePath());
		}

		File classFile = new File(packageDir, getClassName() + ".java");

		try (BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(classFile))) {
			logger.info("Generating class: {}.{}", getPackageName(), getClassName());

			PrintWriter writer = new PrintWriter(bufferWriter);
			writer.printf("package %s;%n", getPackageName());
			writer.println();

			ClassContext classContext = new ClassContext(this);
			processInstances(classContext);
			classContext.writeClass(writer);
		}
	}
}
