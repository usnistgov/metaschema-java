package gov.nist.secauto.metaschema.model;

import java.util.List;
import java.util.Map;

public interface ModelContainer extends FlagContainer {
	FieldInstance getFieldInstanceByName(String name);
	AssemblyInstance getAssemblyInstanceByName(String name);
	Map<String, ModelInstance> getNamedModelInstances();
	Map<String, ? extends FieldInstance> getFieldInstances();
	Map<String, ? extends AssemblyInstance> getAssemblyInstances();
	List<InfoElementInstance> getInstances();
}
