package gov.nist.secauto.metaschema.codegen;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sun.xml.bind.api.impl.NameConverter;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator.Builder;
import gov.nist.secauto.metaschema.model.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.ChoiceInstance;
import gov.nist.secauto.metaschema.model.FieldDefinition;
import gov.nist.secauto.metaschema.model.FlagContainer;
import gov.nist.secauto.metaschema.model.FlagInstance;
import gov.nist.secauto.metaschema.model.InfoElementDefinition;
import gov.nist.secauto.metaschema.model.InfoElementInstance;
import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.MetaschemaException;
import gov.nist.secauto.metaschema.model.MetaschemaFactory;
import gov.nist.secauto.metaschema.model.ModelContainer;
import gov.nist.secauto.metaschema.model.ModelInstance;
import gov.nist.secauto.metaschema.model.Type;

public class JavaGenerator {
	public static void main(String[] args) throws IOException, MetaschemaException {
		File metaschemaFile = new File("target/src/metaschema/oscal_catalog_metaschema.xml");
		Metaschema metaschema = MetaschemaFactory.loadMetaschemaFromXml(metaschemaFile);

		new JavaGenerator(Collections.singletonList(metaschema)).generate(new File(args[0]));
	}

	protected static String toPackageName(FieldDefinition field) {
		return toPackageName(field.getContainingMetaschema().getXmlNamespace());
	}

	protected static String toPackageName(AssemblyDefinition assembly) {
		return toPackageName(assembly.getContainingMetaschema().getXmlNamespace());
	}

	private static String toPackageName(URI nsURI) {
		return NameConverter.standard.toPackageName(nsURI.toString());
	}

	protected static String toClassName(FieldDefinition field) {
		return toClassName(field.getName());
	}

	protected static String toClassName(AssemblyDefinition assembly) {
		return toClassName(assembly.getName());
	}

	private static String toClassName(String name) {
		return NameConverter.standard.toClassName(name);
	}

	private final List<Metaschema> metaschemas;

	public JavaGenerator(List<Metaschema> metaschemas) {
		this.metaschemas = metaschemas;
	}

	protected List<Metaschema> getMetaschemas() {
		return metaschemas;
	}

	public void generate(File dir) throws IOException {
		Map<Metaschema, List<InfoElementDefinition>> metaschemaToInformationElementsMap = buildMetaschemaMap();
		for (Map.Entry<Metaschema, List<InfoElementDefinition>> entry : metaschemaToInformationElementsMap.entrySet()) {
			for (InfoElementDefinition definition : entry.getValue()) {
				AbstractClassGenerator.Builder<?, ?> builder = null;
				if (definition instanceof AssemblyDefinition) {
					AssemblyClassGenerator.Builder<?, ?> assemblyBuilder = AssemblyClassGenerator.builder();

					AssemblyDefinition assembly = (AssemblyDefinition)definition;
					assemblyBuilder.packageName(toPackageName(assembly)).className(toClassName(assembly));
					
					processModel(assembly, assemblyBuilder);

					builder = assemblyBuilder;
				} else if (definition instanceof FieldDefinition) {
					FieldClassGenerator.Builder<?, ?> fieldBuilder = FieldClassGenerator.builder();

					FieldDefinition field = (FieldDefinition)definition;
					fieldBuilder.type(field.getDatatype());
					fieldBuilder.packageName(toPackageName(field)).className(toClassName(field));

					builder = fieldBuilder;
				} else {
					// Skip others
					continue;
				}
				
				if (definition instanceof FlagContainer) {
					FlagContainer container = (FlagContainer)definition;
					for (FlagInstance instance : container.getFlagInstances().values()) {
						
						FlagInstanceGenerator.Builder<?, ?> flagBuilder = FlagInstanceGenerator.builder();
						flagBuilder.name(instance.getName()).type(instance.getDatatype());
						FlagInstanceGenerator flag = flagBuilder.build();
						builder.flag(flag);
					}
				}

				builder.build().writeClass(dir);
			}
		}
	}

	private void processModel(ModelContainer model, Builder<?, ?> assemblyBuilder) {
		for (InfoElementInstance instance : model.getInstances()) {
			if (instance instanceof ChoiceInstance) {
				processModel((ChoiceInstance)instance, assemblyBuilder);
				continue;
			}

			// else the instance is a model instance with a named instance
			ModelInstance modelInstance = (ModelInstance)instance;
			ModelInstanceGenerator.Builder<?, ?> builder = ModelInstanceGenerator.builder();

			InfoElementDefinition definition = modelInstance.getInfoElementDefinition();
			if (definition instanceof AssemblyDefinition) {
				AssemblyDefinition assembly = (AssemblyDefinition)definition;

				builder.packageName(toPackageName(assembly)).className(toClassName(assembly));
			} else if (definition instanceof FieldDefinition) {
				FieldDefinition field = (FieldDefinition)definition;
				builder.packageName(toPackageName(field)).className(toClassName(field));
			}

			builder.description(instance.getDescription());
			String groupAsName = modelInstance.getGroupAsName();
			if (groupAsName != null) {
				builder.name(groupAsName);
			} else {
				builder.name(modelInstance.getName());
			}
			builder.minOccurs(modelInstance.getMinOccurs());
			builder.maxOccurs(modelInstance.getMaxOccurs());
					
			assemblyBuilder.instance(builder.build());
		}
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
