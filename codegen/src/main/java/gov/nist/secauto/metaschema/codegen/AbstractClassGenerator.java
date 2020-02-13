/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
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

import gov.nist.secauto.metaschema.binding.model.annotations.MetaschemaModel;
import gov.nist.secauto.metaschema.codegen.builder.ClassBuilder;
import gov.nist.secauto.metaschema.codegen.builder.MethodBuilder;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.model.info.definitions.ManagedObject;
import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;

public abstract class AbstractClassGenerator<DEFINITION extends ManagedObject> implements ClassGenerator {
	private static final Logger logger = LogManager.getLogger(AbstractClassGenerator.class);

	private final DEFINITION definition;
	private final Map<String, InstanceGenerator> instanceNameToContextMap = new LinkedHashMap<>();
	private final JavaType javaType;
	private boolean hasJsonKeyFlag = false;

	public AbstractClassGenerator(DEFINITION definition) {
		this.definition = definition;
		
		this.javaType = JavaType.create(getDefinition());
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
		if (instance.isJsonKeyFlag()) {
			hasJsonKeyFlag = true;
		}
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

	public boolean hasJsonKeyFlag() {
		return hasJsonKeyFlag;
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
