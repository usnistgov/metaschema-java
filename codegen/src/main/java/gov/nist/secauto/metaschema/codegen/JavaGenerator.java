package gov.nist.secauto.metaschema.codegen;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.model.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.FieldDefinition;
import gov.nist.secauto.metaschema.model.InfoElementDefinition;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.MetaschemaException;
import gov.nist.secauto.metaschema.model.MetaschemaFactory;
import gov.nist.secauto.metaschema.model.Type;

public class JavaGenerator {
	private static final Logger logger = LogManager.getLogger(JavaGenerator.class);

	public static void main(String[] args) throws IOException, MetaschemaException {
		File metaschemaFile = new File("target/src/metaschema/oscal_catalog_metaschema.xml");
		Metaschema metaschema = MetaschemaFactory.loadMetaschemaFromXml(metaschemaFile);

		new JavaGenerator(Collections.singletonList(metaschema)).generate(new File(args[0]));
	}

	private final List<Metaschema> metaschemas;

	public JavaGenerator(Metaschema metaschema) {
		this(Collections.singletonList(metaschema));
	}

	public JavaGenerator(List<Metaschema> metaschemas) {
		this.metaschemas = metaschemas;
	}

	protected List<Metaschema> getMetaschemas() {
		return metaschemas;
	}

	public Map<Metaschema, String> generate(File dir) throws IOException {
		logger.info("Generating Java classes in: {}", dir.getPath());

		Map<Metaschema, String> rootClassMap = new HashMap<>();

		Map<Metaschema, List<InfoElementDefinition>> metaschemaToInformationElementsMap = buildMetaschemaMap();
		for (Map.Entry<Metaschema, List<InfoElementDefinition>> entry : metaschemaToInformationElementsMap.entrySet()) {
			Metaschema metaschema = entry.getKey();
			AssemblyDefinition rootAssembly = metaschema.getRootAssemblyDefinition();

			for (InfoElementDefinition definition : entry.getValue()) {
				AbstractClassGenerator<?> classGenerator = null;
				if (definition instanceof AssemblyDefinition) {
					classGenerator = new AssemblyClassGenerator((AssemblyDefinition)definition);
					classGenerator.generateClass(dir);
					if (Objects.equals(rootAssembly, definition)) {
						rootClassMap.put(metaschema, classGenerator.getQualifiedClassName());
					}
				} else if (definition instanceof FieldDefinition) {
					FieldDefinition fieldDefinition = (FieldDefinition)definition;
					
					// if field is just a simple data value, then no class is needed
					if (!fieldDefinition.getFlagInstances().isEmpty()) {
						classGenerator = new FieldClassGenerator(fieldDefinition);
						classGenerator.generateClass(dir);
					}
				} else {
					// Skip others
					continue;
				}
			}
		}
		return Collections.unmodifiableMap(rootClassMap);
	}

	private Map<Metaschema, List<InfoElementDefinition>> buildMetaschemaMap() {
		Map<Metaschema, List<InfoElementDefinition>> retval = new HashMap<>();

		for (Metaschema metaschema : getMetaschemas()) {
			processMetaschema(metaschema, retval);
		}
		return retval;
	}

	private void processMetaschema(Metaschema metaschema, Map<Metaschema, List<InfoElementDefinition>> map) {
		for (Metaschema importedMetaschema : metaschema.getImportedMetaschema().values()) {
			processMetaschema(importedMetaschema, map);
		}

		if (!map.containsKey(metaschema)) {
			List<InfoElementDefinition> definitions = metaschema.getInfoElementDefinitions().values().stream()
					.filter(c -> !Type.FLAG.equals(c.getType())).collect(Collectors.toList());
			map.put(metaschema, definitions);
		}
	}
}
