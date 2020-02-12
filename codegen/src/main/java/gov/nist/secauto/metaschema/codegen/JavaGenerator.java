package gov.nist.secauto.metaschema.codegen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.binding.model.annotations.XmlNs;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlNsForm;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlSchema;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.MetaschemaException;
import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.info.Type;
import gov.nist.secauto.metaschema.model.info.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.InfoElementDefinition;

public class JavaGenerator {
	private static final Logger logger = LogManager.getLogger(JavaGenerator.class);

	public static void main(String[] args) throws IOException, MetaschemaException {
		File metaschemaFile = new File("target/src/metaschema/oscal_catalog_metaschema.xml");
		Metaschema metaschema = new MetaschemaLoader().loadXmlMetaschema(metaschemaFile);

		JavaGenerator.generate(metaschema, new File(args[0]));
	}

	private JavaGenerator() {
		// disable construction
	}

	public static Map<Metaschema, List<GeneratedClass>> generate(Metaschema metaschema, File dir) throws IOException {
		return generate(Collections.singletonList(metaschema), dir);
	}

	public static Map<Metaschema, List<GeneratedClass>> generate(Collection<? extends Metaschema> metaschemas, File dir) throws IOException {
		logger.info("Generating Java classes in: {}", dir.getPath());

		Map<Metaschema, List<GeneratedClass>> retval = new HashMap<>();
		Map<URI, String> xmlNamespaceToPackageNameMap = new HashMap<>();
		Map<URI, Set<Metaschema>> xmlNamespaceToMetaschemaMap = new HashMap<>();

		Map<Metaschema, List<InfoElementDefinition>> metaschemaToInformationElementsMap = buildMetaschemaMap(metaschemas);
		for (Map.Entry<Metaschema, List<InfoElementDefinition>> entry : metaschemaToInformationElementsMap.entrySet()) {
			Metaschema metaschema = entry.getKey();
			List<GeneratedClass> generatedClasses = null;
			Set<String> classNames = new HashSet<>();

			for (InfoElementDefinition definition : entry.getValue()) {
				ClassGenerator classGenerator = null;
				if (definition instanceof AssemblyDefinition) {
					classGenerator = new AssemblyClassGenerator((AssemblyDefinition)definition);
				} else if (definition instanceof FieldDefinition) {
					FieldDefinition fieldDefinition = (FieldDefinition)definition;
					
					// if field is just a simple data value, then no class is needed
					if (!fieldDefinition.getFlagInstances().isEmpty()) {
						classGenerator = new FieldClassGenerator(fieldDefinition);
					}
				} else {
					// Skip others
					continue;
				}

				if (classGenerator != null) {
					GeneratedClass generatedClass = classGenerator.generateClass(dir);
					String className = generatedClass.getClassName();
					if (classNames.contains(className)) {
						throw new IllegalStateException(String.format("Found duplicate class name '%s' in metaschema '%s'. If multiple metaschema are compiled for the same namespace, all class names must be unique.", className, metaschema.getLocation()));
					} else {
						classNames.add(className);
					}

					if (generatedClasses == null) {
						generatedClasses = new LinkedList<>();
						retval.put(metaschema, generatedClasses);
					}
					generatedClasses.add(generatedClass);

				}
			}

			URI xmlNamespace = metaschema.getXmlNamespace();
			String packageName = metaschema.getPackageName();

			if (xmlNamespaceToPackageNameMap.containsKey(xmlNamespace)) {
				String assignedPackage = xmlNamespaceToPackageNameMap.get(xmlNamespace);
				if (!assignedPackage.equals(packageName)) {
					throw new IllegalStateException(String.format("The metaschema '%s' is assigning the new package name '%s', which is different than the previously assigned package name '%s' for the same namespace. tA metaschema namespace must be assigned to a consistent package name.", metaschema.getLocation().toString(), metaschema.getPackageName(), assignedPackage));
				}
			} else {
				xmlNamespaceToPackageNameMap.put(xmlNamespace, packageName);
			}

			Set<Metaschema> metaschemaSet = xmlNamespaceToMetaschemaMap.get(xmlNamespace);
			if (metaschemaSet == null) {
				metaschemaSet = new HashSet<>();
				xmlNamespaceToMetaschemaMap.put(xmlNamespace, metaschemaSet);
			}
			metaschemaSet.add(metaschema);
		}

		for (Map.Entry<URI, String> entry : xmlNamespaceToPackageNameMap.entrySet()) {
			String packageName = entry.getValue();
			String packagePath = packageName.replace(".", "/");
			File packageInfo = new File(dir, packagePath+"/package-info.java");
			URI namespace = entry.getKey();
			String namespaceString = namespace.toString();

			try (FileWriter fileWriter = new FileWriter(packageInfo)) {
				PrintWriter writer = new PrintWriter(fileWriter);

				writer.format("@%1$s(namespace = \"%2$s\", xmlns = {@%3$s(prefix = \"\", namespace = \"%2$s\")}, xmlElementFormDefault = %4$s.QUALIFIED)%n", XmlSchema.class.getName(), namespaceString, XmlNs.class.getName(), XmlNsForm.class.getName());
				writer.format("package %s;%n", packageName);
			}

			for (Metaschema metaschema : xmlNamespaceToMetaschemaMap.get(namespace)) {
				retval.get(metaschema).add(new GeneratedClass(packageInfo, packageName+".package-info", false));
			}
		}
		return Collections.unmodifiableMap(retval);
	}

	private static Map<Metaschema, List<InfoElementDefinition>> buildMetaschemaMap(Collection<? extends Metaschema> metaschemas) {
		Map<Metaschema, List<InfoElementDefinition>> retval = new HashMap<>();

		for (Metaschema metaschema : metaschemas) {
			processMetaschema(metaschema, retval);
		}
		return retval;
	}

	private static void processMetaschema(Metaschema metaschema, Map<Metaschema, List<InfoElementDefinition>> map) {
		for (Metaschema importedMetaschema : metaschema.getImportedMetaschema().values()) {
			processMetaschema(importedMetaschema, map);
		}

		if (!map.containsKey(metaschema)) {
			List<InfoElementDefinition> definitions = metaschema.getInfoElementDefinitions().values().stream()
					.filter(c -> !Type.FLAG.equals(c.getType())).collect(Collectors.toList());
			map.put(metaschema, definitions);
		}
	}

	public static class GeneratedClass {
		private final File classFile;
		private final String className;
		private final boolean rootClass;

		public GeneratedClass(File classFile, String className, boolean rootClass) {
			Objects.requireNonNull(classFile, "classFile");
			Objects.requireNonNull(className, "className");
			this.classFile = classFile;
			this.className = className;
			this.rootClass = rootClass;
		}

		public File getClassFile() {
			return classFile;
		}

		public String getClassName() {
			return className;
		}

		public boolean isRootClass() {
			return rootClass;
		}
	}
}
