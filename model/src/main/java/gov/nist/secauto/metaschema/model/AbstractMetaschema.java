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
package gov.nist.secauto.metaschema.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.nist.secauto.metaschema.model.configuration.ModelBindingConfiguration;
import gov.nist.secauto.metaschema.model.info.Util;
import gov.nist.secauto.metaschema.model.info.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.FlagContainer;
import gov.nist.secauto.metaschema.model.info.definitions.FlagDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.InfoElementDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.ModelContainer;
import gov.nist.secauto.metaschema.model.info.instances.AssemblyInstance;
import gov.nist.secauto.metaschema.model.info.instances.ChoiceInstance;
import gov.nist.secauto.metaschema.model.info.instances.FieldInstance;
import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;
import gov.nist.secauto.metaschema.model.info.instances.InfoElementInstance;

public abstract class AbstractMetaschema implements Metaschema {
	private static final Logger logger = LogManager.getLogger(AbstractMetaschema.class);
	
	private final URI location;
	private final ModelBindingConfiguration bindingConfiguration;
	private final Map<URI, Metaschema> importedMetaschema;
	private Map<String, InfoElementDefinition> usedInfoElementDefinitions;
	private Map<String, FlagDefinition> usedFlagDefinitions;
	private Map<String, FieldDefinition> usedFieldDefinitions;
	private Map<String, AssemblyDefinition> usedAssemblyDefinitions;


	public AbstractMetaschema(URI metaschemaResource, ModelBindingConfiguration bindingConfiguration, Map<URI, ? extends Metaschema> importedMetaschema) {
		Objects.requireNonNull(metaschemaResource, "metaschemaResource");
		Objects.requireNonNull(bindingConfiguration, "bindingConfiguration");
		Objects.requireNonNull(importedMetaschema, "importedMetaschema");
		this.location = metaschemaResource;
		this.bindingConfiguration = bindingConfiguration;
		this.importedMetaschema = Collections.unmodifiableMap(importedMetaschema);
		logger.trace("Creating metaschema '{}'",metaschemaResource);
	}

	public ModelBindingConfiguration getBindingConfiguration() {
		return bindingConfiguration;
	}

	@Override
	public Map<URI, Metaschema> getImportedMetaschema() {
		return importedMetaschema;
	}

	@Override
	public Map<String, InfoElementDefinition> getUsedInfoElementDefinitions() {
		return Collections.unmodifiableMap(usedInfoElementDefinitions);
	}

	@Override
	public Map<String, FlagDefinition> getUsedFlagDefinitions() {
		return Collections.unmodifiableMap(usedFlagDefinitions);
	}

	@Override
	public Map<String, FieldDefinition> getUsedFieldDefinitions() {
		return Collections.unmodifiableMap(usedFieldDefinitions);
	}

	@Override
	public Map<String, AssemblyDefinition> getUsedAssemblyDefinitions() {
		return Collections.unmodifiableMap(usedAssemblyDefinitions);
	}

	protected void parseUsedDefinitions() throws MetaschemaException {
		logger.debug("Processing metaschema '{}'",this.getLocation());

		// handle definitions used in this metaschema, including any referenced definitions in included metaschema
		if (getImportedMetaschema().isEmpty()) {
			this.usedInfoElementDefinitions = new LinkedHashMap<>(getInfoElementDefinitions());
			this.usedFlagDefinitions = new LinkedHashMap<>(getFlagDefinitions());
			this.usedFieldDefinitions = new LinkedHashMap<>(getFieldDefinitions());
			this.usedAssemblyDefinitions = new LinkedHashMap<>(getAssemblyDefinitions());
		} else {
			AssemblyDefinition rootAssemblyDefinition = getRootAssemblyDefinition();
			Map<String, InfoElementDefinition> usedDefinitions = new LinkedHashMap<>();
			processUsedDefinitions(rootAssemblyDefinition, usedDefinitions);

			this.usedInfoElementDefinitions = new LinkedHashMap<>();
			this.usedFlagDefinitions = new LinkedHashMap<>();
			this.usedFieldDefinitions = new LinkedHashMap<>();
			this.usedAssemblyDefinitions = new LinkedHashMap<>();

			List<Metaschema> metaschemas = new ArrayList<>(getImportedMetaschema().size() + 1);
			metaschemas.addAll(getImportedMetaschema().values());
			metaschemas.add(this);

			for (Metaschema metaschema : metaschemas) {
				for (InfoElementDefinition definition : metaschema.getInfoElementDefinitions().values()) {
					logger.trace("Checking use of '{}'", Util.toCoordinates(definition));

					InfoElementDefinition usedDefinition = usedDefinitions.get(definition.getName());
					boolean used = definition.equals(usedDefinition);
					if (used) {
						logger.debug("Using definition '{}'", Util.toCoordinates(usedDefinition));
					} else {
						if (usedDefinition != null) {
							logger.trace("  Unused definition '{}'", Util.toCoordinates(usedDefinition));
						} else {
							logger.trace("  No match for definition '{}'", Util.toCoordinates(definition));
						}
						continue;
					}
					
					String usedName = usedDefinition.getName();
					this.usedInfoElementDefinitions.put(usedName, usedDefinition);
					if (usedDefinition instanceof FlagDefinition) {
						this.usedFlagDefinitions.put(usedName, (FlagDefinition)usedDefinition);
					} else if (usedDefinition instanceof FieldDefinition) {
						this.usedFieldDefinitions.put(usedName, (FieldDefinition)usedDefinition);
					} else if (usedDefinition instanceof AssemblyDefinition) {
						this.usedAssemblyDefinitions.put(usedName, (AssemblyDefinition)usedDefinition);
					}
				}
			}
		}
			
	}

	private void processUsedDefinitions(AssemblyDefinition definition, Map<String, InfoElementDefinition> usedDefinitions) throws MetaschemaException {
		logger.trace("Analyzing use of assembly '{}'", Util.toCoordinates(definition));
		if (isNewDefinition(definition, usedDefinitions)) {
			usedDefinitions.put(definition.getName(), definition);

			// get all referenced flags
			processUsedDefinitions((FlagContainer)definition, usedDefinitions);
	
			// get all referenced assemblies and fields, and anything they reference
			processUsedDefinitions((ModelContainer)definition, usedDefinitions);
		} else {
			logger.trace("  skipping assembly '{}'", Util.toCoordinates(definition));
		}
	}

	private void processUsedDefinitions(FlagContainer container, Map<String, InfoElementDefinition> usedDefinitions) throws MetaschemaException {
		// get all referenced flags
		for (FlagInstance instance : container.getFlagInstances().values()) {
			logger.trace("Analyzing use of flag '{}'", Util.toCoordinates(instance));
			if (instance.isReference()) {
				FlagDefinition flagDefinition = instance.getDefinition();
				if (isNewDefinition(flagDefinition, usedDefinitions)) {
					usedDefinitions.put(flagDefinition.getName(), flagDefinition);
					logger.debug("Using definition '{}'", Util.toCoordinates(flagDefinition));
				} else {
					logger.debug("  Skipping known definition '{}'", Util.toCoordinates(flagDefinition));
				}
			} else {
				// a local definition
				logger.debug("  Skipping local instance '{}'", Util.toCoordinates(instance));
			}
		}
	}
	
	private void processUsedDefinitions(ModelContainer modelContainer, Map<String, InfoElementDefinition> usedDefinitions) throws MetaschemaException {
		for (InfoElementInstance instance : modelContainer.getInstances()) {
			logger.trace("Processing model instance '{}'", Util.toCoordinates(instance));
			InfoElementDefinition definition = instance.getDefinition();
			if (instance.isReference()) {
				if (!isNewDefinition(definition, usedDefinitions)) {
					logger.debug("  Skipping known definition '{}'", Util.toCoordinates(definition));
					// skip this one, since we have already processed it
					continue;
				} else {
					// handle below
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
				logger.debug("Using definition '{}'", Util.toCoordinates(definition));
				FieldInstance fieldInstance = (FieldInstance)instance;
				processUsedDefinitions((FlagContainer)fieldInstance.getDefinition(), usedDefinitions);
				break;
			case ASSEMBLY:
				AssemblyInstance assemblyInstance = (AssemblyInstance)instance;
				processUsedDefinitions(assemblyInstance.getDefinition(), usedDefinitions);
				break;
			case CHOICE:
				ChoiceInstance choiceInstance = (ChoiceInstance)instance;
				processUsedDefinitions((ModelContainer)choiceInstance, usedDefinitions);
				break;
			default:
				throw new RuntimeException("Unexpected type: "+instance.getType());
			}
		}
	}

	private boolean isNewDefinition(InfoElementDefinition definition,
			Map<String, InfoElementDefinition> usedDefinitions) throws MetaschemaException {
		boolean processElement = true;
		if (usedDefinitions.containsKey(definition.getName())) {
			InfoElementDefinition existingDefinition = usedDefinitions.get(definition.getName());
			if (!existingDefinition.getType().equals(definition.getType())) {
				// check if the types differ
				throw(new MetaschemaException(String.format("New element '%s: (%s) %s' has conflicting type with old element '%s:(%s)%s'",
						definition.getContainingMetaschema().getLocation(),
						definition.getType(),
						definition.getName(),
						existingDefinition.getContainingMetaschema().getLocation(),
						existingDefinition.getType(),
						existingDefinition.getName()
						)));
			} else if (!existingDefinition.equals(definition)) {
				// the field or assembly is shadowing an existing definition
				throw(new MetaschemaException(String.format("New element '%s: (%s) %s' shadows old element '%s:(%s)%s'",
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
