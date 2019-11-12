package gov.nist.secauto.metaschema.schemagen;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractMetaschema implements Metaschema {
	private final URI location;
	private final Map<URI, ? extends Metaschema> importedMetaschema;
	private Map<String, ? extends InfoElementDefinition> usedInfoElementDefinitions;
	private Map<String, ? extends FlagDefinition> usedFlagDefinitions;
	private Map<String, ? extends FieldDefinition> usedFieldDefinitions;
	private Map<String, ? extends AssemblyDefinition> usedAssemblyDefinitions;


	public AbstractMetaschema(URI metaschemaResource, Map<URI, ? extends Metaschema> importedMetaschema) {
		this.location = metaschemaResource;
		this.importedMetaschema = Collections.unmodifiableMap(importedMetaschema);
	}

	protected void parseUsedDefinitions() throws MetaschemaException {
		// handle definitions used in this metaschema, including any referenced definitions in included metaschema
		if (getImportedMetaschema().isEmpty()) {
			this.usedInfoElementDefinitions = getInfoElementDefinitions();
			this.usedFlagDefinitions = getFlagDefinitions();
			this.usedFieldDefinitions = getFieldDefinitions();
			this.usedAssemblyDefinitions = getAssemblyDefinitions();
		} else {
			AssemblyDefinition rootAssemblyDefinition = getRootAssemblyDefinition();
			Map<String, InfoElementDefinition> usedDefinitions = new HashMap<>();
			getUsedDefinitions(rootAssemblyDefinition, usedDefinitions);
			Set<String> usedKeys = usedDefinitions.keySet();

			for (InfoElementDefinition definition : usedDefinitions.values()) {
				System.out.println(String.format("%s:(%s)%s",
						definition.getContainingMetaschema().getLocation(),
						definition.getType(),
						definition.getName()));
			}
			
			this.usedInfoElementDefinitions = new LinkedHashMap<>();
			this.usedFlagDefinitions = new LinkedHashMap<>();
			this.usedFieldDefinitions = new LinkedHashMap<>();
			this.usedAssemblyDefinitions = new LinkedHashMap<>();
		}
			
	}

	private void getUsedDefinitions(AssemblyDefinition definition, Map<String, InfoElementDefinition> usedDefinitions) throws MetaschemaException {
		System.out.println(String.format("Assembly '%s:(%s)%s'",
				definition.getContainingMetaschema().getLocation(),
				definition.getType(),
				definition.getName()));
		if (checkViolation(definition, usedDefinitions)) {
			usedDefinitions.put(definition.getName(), definition);

			// get all referenced flags
			getUsedDefinitions((FlagContainer)definition, usedDefinitions);
	
			// get all referenced assemblies and fields, and anything they reference
			getUsedDefinitions((ModelContainer)definition, usedDefinitions);
		} else {
			System.out.println(String.format("  Skipping '%s:(%s)%s'",
					definition.getContainingMetaschema().getLocation(),
					definition.getType(),
					definition.getName()));
		}
	}

	private void getUsedDefinitions(FlagContainer container, Map<String, InfoElementDefinition> usedDefinitions) throws MetaschemaException {
		// get all referenced flags
		for (FlagInstance instance : container.getFlagInstances().values()) {
			System.out.println(String.format("  Instance '%s:(%s)%s'",
					instance.getContainingMetaschema().getLocation(),
					instance.getType(),
					instance.getName()));
			if (instance.isReference()) {
				FlagDefinition flagDefinition = instance.getFlagDefinition();
				if (checkViolation(flagDefinition, usedDefinitions)) {
					usedDefinitions.put(flagDefinition.getName(), flagDefinition);
				}
			} else {
				// a local definition
			}
		}
	}
	
	private void getUsedDefinitions(ModelContainer modelContainer, Map<String, InfoElementDefinition> usedDefinitions) throws MetaschemaException {
		for (InfoElementInstance instance : modelContainer.getModelInstances()) {
			System.out.println(String.format("  Instance '%s:(%s)%s'",
					instance.getContainingMetaschema().getLocation(),
					instance.getType(),
					instance.getName()));
			InfoElementDefinition definition = instance.getInfoElementDefinition();
			if (instance.isReference()) {
				if (!checkViolation(definition, usedDefinitions)) {
					System.out.println(String.format("    Skipping '%s:(%s)%s'",
							instance.getContainingMetaschema().getLocation(),
							instance.getType(),
							instance.getName()));
					// skip this one, since we have already processed it
					continue;
				}
			} else {
				// a local definition, we still need to process its instances
			}

			switch (instance.getType()) {
			case FLAG:
				// no child instances
				break;
			case FIELD:
				usedDefinitions.put(definition.getName(), definition);
				FieldInstance fieldInstance = (FieldInstance)instance;
				getUsedDefinitions((FlagContainer)fieldInstance.getFieldDefinition(), usedDefinitions);
				break;
			case ASSEMBLY:
				AssemblyInstance assemblyInstance = (AssemblyInstance)instance;
				getUsedDefinitions(assemblyInstance.getAssemblyDefinition(), usedDefinitions);
				break;
			case CHOICE:
				ChoiceInstance choiceInstance = (ChoiceInstance)instance;
				getUsedDefinitions((ModelContainer)choiceInstance, usedDefinitions);
				break;
			default:
				throw new RuntimeException("Unexpected type: "+instance.getType());
			}
		}
	}

	private boolean checkViolation(InfoElementDefinition definition,
			Map<String, InfoElementDefinition> usedDefinitions) throws MetaschemaException {
		boolean processElement = true;
		if (usedDefinitions.containsKey(definition.getName())) {
			InfoElementDefinition existingDefinition = usedDefinitions.get(definition.getName());
			if (!existingDefinition.getType().equals(definition.getType())) {
				throw(new MetaschemaException(String.format("New element '%s:(%s)%s' has conflicting type with old element '%s:(%s)%s'",
						definition.getContainingMetaschema().getLocation(),
						definition.getType(),
						definition.getName(),
						existingDefinition.getContainingMetaschema().getLocation(),
						existingDefinition.getType(),
						existingDefinition.getName()
						)));
			} else if (!existingDefinition.equals(definition)) {
				// the field or assembly is shadowing an existing definition
				throw(new MetaschemaException(String.format("New element '%s:(%s)%s' shadows old element '%s:(%s)%s'",
						definition.getContainingMetaschema().getLocation(),
						definition.getType(),
						definition.getName(),
						existingDefinition.getContainingMetaschema().getLocation(),
						existingDefinition.getType(),
						existingDefinition.getName()
						)));
			}
			processElement = false;
		}
		return processElement;
	}

	@Override
	public URI getLocation() {
		return location;
	}

	@Override
	public InfoElementDefinition getInfoElementDefinitionByName(String name) {
		InfoElementDefinition retval = getInfoElementDefinitions().get(name);
		if (retval == null) {
			for (Metaschema metaschema : getImportedMetaschema().values()) {
				if ((retval = metaschema.getInfoElementDefinitionByName(name)) != null) {
					break;
				}
			}
		}
		return retval;
	}

	@Override
	public AssemblyDefinition getAssemblyDefinitionByName(String name) {
		AssemblyDefinition retval = getAssemblyDefinitions().get(name);
		if (retval == null) {
			for (Metaschema metaschema : getImportedMetaschema().values()) {
				if ((retval = metaschema.getAssemblyDefinitionByName(name)) != null) {
					break;
				}
			}
		}
		return retval;
	}

	@Override
	public FieldDefinition getFieldDefinitionByName(String name) {
		FieldDefinition retval = getFieldDefinitions().get(name);
		if (retval == null) {
			for (Metaschema metaschema : getImportedMetaschema().values()) {
				if ((retval = metaschema.getFieldDefinitionByName(name)) != null) {
					break;
				}
			}
		}
		return retval;
	}

	@Override
	public FlagDefinition getFlagDefinitionByName(String name) {
		FlagDefinition retval = getFlagDefinitions().get(name);
		if (retval == null) {
			for (Metaschema metaschema : getImportedMetaschema().values()) {
				if ((retval = metaschema.getFlagDefinitionByName(name)) != null) {
					break;
				}
			}
		}
		return retval;
	}

}
