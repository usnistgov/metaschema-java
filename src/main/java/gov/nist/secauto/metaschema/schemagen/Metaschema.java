package gov.nist.secauto.metaschema.schemagen;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface Metaschema {
	URI getLocation();
	String getShortName();
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
}
