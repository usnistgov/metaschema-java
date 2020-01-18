package gov.nist.secauto.metaschema.model;

import java.net.URI;
import java.util.Map;

import gov.nist.secauto.metaschema.model.info.definitions.AssemblyDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.FieldDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.FlagDefinition;
import gov.nist.secauto.metaschema.model.info.definitions.InfoElementDefinition;

public interface Metaschema {
	URI getLocation();
	String getShortName();
	URI getXmlNamespace();
	Map<URI, Metaschema> getImportedMetaschema();
	Map<String, ? extends InfoElementDefinition> getInfoElementDefinitions();
	Map<String, ? extends AssemblyDefinition> getAssemblyDefinitions();
	Map<String, ? extends FieldDefinition> getFieldDefinitions();
	Map<String, ? extends FlagDefinition> getFlagDefinitions();
	InfoElementDefinition getInfoElementDefinitionByName(String name);
	AssemblyDefinition getAssemblyDefinitionByName(String name);
	FieldDefinition getFieldDefinitionByName(String name);
	FlagDefinition getFlagDefinitionByName(String name);
	AssemblyDefinition getRootAssemblyDefinition();
	Map<String, InfoElementDefinition> getUsedInfoElementDefinitions();
	Map<String, FlagDefinition> getUsedFlagDefinitions();
	Map<String, FieldDefinition> getUsedFieldDefinitions();
	Map<String, AssemblyDefinition> getUsedAssemblyDefinitions();
	String getPackageName();
}
