package gov.nist.secauto.metaschema.schemagen;

import java.util.List;
import java.util.Map;

public interface ModelContainer extends FlagContainer {
	FieldInstance getFieldInstanceByName(String name);
	AssemblyInstance getAssemblyInstanceByName(String name);
	Map<String, InfoElementInstance> getNamedModelInstances();
	Map<String, ? extends FieldInstance> getFieldInstances();
	Map<String, ? extends AssemblyInstance> getAssemblyInstances();
	List<InfoElementInstance> getModelInstances();
}
