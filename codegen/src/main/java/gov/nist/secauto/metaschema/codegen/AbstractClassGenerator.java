package gov.nist.secauto.metaschema.codegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.codegen.context.FlagInstanceContext;
import gov.nist.secauto.metaschema.codegen.context.InstanceContext;
import gov.nist.secauto.metaschema.model.FlagInstance;
import gov.nist.secauto.metaschema.model.ManagedObject;

public abstract class AbstractClassGenerator<DEFINITION extends ManagedObject> {
	private static final Logger logger = LogManager.getLogger(AbstractClassGenerator.class);

	private static final Set<String> DEFAULT_IMPORTS;
	
	static {
		Set<String> imports = new HashSet<>();
		imports.add("com.fasterxml.jackson.annotation.*");
		imports.add("javax.xml.bind.annotation.*");
		DEFAULT_IMPORTS = Collections.unmodifiableSet(imports);
	}

	private final DEFINITION definition;
	private final Map<String, InstanceContext> instanceNameToContextMap = new LinkedHashMap<>();
	private String className;
	private String packageName;
	private String qualifiedClassName;

	public AbstractClassGenerator(DEFINITION definition) {
		this.definition = definition;
		for (FlagInstance instance : definition.getFlagInstances().values()) {
			newFlagInstance(instance);
		}
	}

	public DEFINITION getDefinition() {
		return definition;
	}

	public String getPackageName() {
		if (packageName == null) {
			packageName = ClassUtils.toPackageName(getDefinition());
		}
		return packageName;
	}

	public String getClassName() {
		if (className == null) {
			className = ClassUtils.toClassName(getDefinition());
		}
		return className;
	}

	public String getQualifiedClassName() {
		if (qualifiedClassName == null) {
			qualifiedClassName = getPackageName()+"."+getClassName();
		}
		return qualifiedClassName;
	}

	public String generateClass(File outputDir)
			throws IOException {
		String packageName = getPackageName();
		String className = getClassName();
		String qualifiedClassName = getQualifiedClassName();
		File packageDir = new File(outputDir, packageName.replace(".", "/"));

		if (packageDir.mkdirs()) {
			logger.info("Created package directory '{}'", packageDir.getAbsolutePath());
		}

		File classFile = new File(packageDir, className + ".java");

		try (BufferedWriter bufferWriter = new BufferedWriter(new FileWriter(classFile))) {
			logger.info("Generating class: {}", qualifiedClassName);

			PrintWriter writer = new PrintWriter(bufferWriter);
			writer.printf("package %s;%n", packageName);
			writer.println();

			writeClass(writer);
		}

		return qualifiedClassName;
	}

	protected void addInstance(InstanceContext context) {
		String name = context.getPropertyName();
		InstanceContext oldContext = instanceNameToContextMap.put(name, context);
		if (oldContext != null) {
			logger.error("Unexpected duplicate instance property name '{}'", name);
			throw new RuntimeException(String.format("Unexpected duplicate instance property name '%s'", name));
		}
	}

	public FlagInstanceContext newFlagInstance(FlagInstance instance) {
		FlagInstanceContext context = new FlagInstanceContext(instance, this);
		addInstance(context);
		return context;
	}

	public boolean hasInstanceWithName(String newName) {
		return instanceNameToContextMap.containsKey(newName);
	}

	public Collection<InstanceContext> getInstanceContexts() {
		return instanceNameToContextMap.values();
	}

	public void writeClass(PrintWriter writer) {
	
		// Handle Imports
		Set<String> imports = new HashSet<>();
		for (InstanceContext instance : getInstanceContexts()) {
			imports.addAll(instance.getImports());
		}
		imports.addAll(getAdditionalImports());
	
		if (!imports.isEmpty()) {
			// sort
			imports = imports.stream().sorted((String s1,String s2)->{       
			    return s1.compareTo(s2);
			}).collect(Collectors.toCollection(LinkedHashSet::new));;
	
			for (String importEntry : imports) {
				if (!importEntry.startsWith("java.lang.")) {
					writer.printf("import %s;%n", importEntry);
				}
			}
			writer.println();
		}

		writeClassJava(writer);
	}

	protected void writeClassJava(PrintWriter writer) {
		writer.println("@XmlAccessorType(XmlAccessType.FIELD)");
		writer.printf("public class %s {%n", getClassName());
	
		// handle instance variables
		for (InstanceContext instance : getInstanceContexts()) {
			instance.writeVariable(writer);
		}
		
		writeOtherVariables(writer);
	
		writer.println();

		writeConstructors(writer);

		// handle flag getters/setters
		for (InstanceContext instance : getInstanceContexts()) {
			instance.writeGetter(writer);
			instance.writeSetter(writer);
		}
	
		writeOtherAccessors(writer);
	
		writeOtherMethods(writer);
		
		writer.println("}");
	}

	protected void writeConstructors(PrintWriter writer) {
		// no-arg constructor
		writer.printf("\tpublic %s() {%n", getClassName());
		writer.println("\t}");
		writer.println();
	}

	protected Set<String> getAdditionalImports() {
		return DEFAULT_IMPORTS;
	}

	protected void writeOtherVariables(@SuppressWarnings("unused") PrintWriter writer) { }

	protected void writeOtherAccessors(@SuppressWarnings("unused") PrintWriter writer) { }

	protected void writeOtherMethods(@SuppressWarnings("unused") PrintWriter writer) { }
}
