package gov.nist.secauto.metaschema.codegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.binding.annotations.MetaschemaModel;
import gov.nist.secauto.metaschema.codegen.builder.ClassBuilder;
import gov.nist.secauto.metaschema.codegen.builder.MethodBuilder;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.model.FlagInstance;
import gov.nist.secauto.metaschema.model.ManagedObject;

public abstract class AbstractClassGenerator<DEFINITION extends ManagedObject> implements ClassGenerator {
	private static final Logger logger = LogManager.getLogger(AbstractClassGenerator.class);

	private final DEFINITION definition;
	private final Map<String, InstanceGenerator> instanceNameToContextMap = new LinkedHashMap<>();
	private final JavaType javaType;

	public AbstractClassGenerator(DEFINITION definition) {
		this.definition = definition;
		this.javaType = JavaType.create(ClassUtils.toPackageName(getDefinition()), ClassUtils.toClassName(getDefinition()));
		for (FlagInstance instance : definition.getFlagInstances().values()) {
			newFlagInstance(instance);
		}
	}

	public DEFINITION getDefinition() {
		return definition;
	}

	@Override
	public JavaType getJavaType() {
		return javaType;
	}

	@Override
	public URI getXmlNamespace() {
		return getDefinition().getContainingMetaschema().getXmlNamespace();
	}

	@Override
	public String getPackageName() {
		return getJavaType().getPackageName();
	}

	@Override
	public JavaGenerator.GeneratedClass generateClass(File outputDir)
			throws IOException {
		String className = getJavaType().getClassName();
		String qualifiedClassName = getJavaType().getQualifiedClassName();
		String packagePath = getPackageName().replace(".", "/");
		File packageDir = new File(outputDir, packagePath);

		if (packageDir.mkdirs()) {
			logger.info("Created package directory '{}'", packageDir.getAbsolutePath());
		}

		File classFile = new File(packageDir, className + ".java");

		try (BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(classFile))) {
			logger.info("Generating class: {}", qualifiedClassName);

			PrintWriter writer = new PrintWriter(bufferWriter);
			ClassBuilder builder = new ClassBuilder(getJavaType());
			
			buildClass(builder);

			builder.build(writer);
		}

		return new JavaGenerator.GeneratedClass(classFile, qualifiedClassName, isRootClass());
	}

	protected boolean isRootClass() {
		return false;
	}

	protected void addInstance(InstanceGenerator context) {
		String name = context.getPropertyName();
		InstanceGenerator oldContext = instanceNameToContextMap.put(name, context);
		if (oldContext != null) {
			logger.error("Unexpected duplicate instance property name '{}'", name);
			throw new RuntimeException(String.format("Unexpected duplicate instance property name '%s'", name));
		}
	}

	public FlagInstanceGenerator newFlagInstance(FlagInstance instance) {
		FlagInstanceGenerator context = new FlagInstanceGenerator(instance, this);
		addInstance(context);
		return context;
	}

	@Override
	public boolean hasInstanceWithName(String newName) {
		return instanceNameToContextMap.containsKey(newName);
	}

	public Collection<InstanceGenerator> getInstanceContexts() {
		return instanceNameToContextMap.values();
	}

	protected void buildClass(ClassBuilder builder) {
		builder.annotation(MetaschemaModel.class);

		// no-arg constructor
		builder.newConstructorBuilder();

		for (InstanceGenerator instance : getInstanceContexts()) {
			instance.buildInstance(builder);
		}

		MethodBuilder toStringMethod = builder.newMethodBuilder("toString").returnType(String.class);
		toStringMethod.annotation(Override.class);
		toStringMethod.getBodyWriter().println("return new org.apache.commons.lang3.builder.ReflectionToStringBuilder(this, org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE).toString();");
	}
}
