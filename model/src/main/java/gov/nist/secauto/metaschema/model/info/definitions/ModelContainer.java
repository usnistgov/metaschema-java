package gov.nist.secauto.metaschema.model.info.definitions;

import java.util.List;
import java.util.Map;

import gov.nist.secauto.metaschema.model.info.instances.AssemblyInstance;
import gov.nist.secauto.metaschema.model.info.instances.FieldInstance;
import gov.nist.secauto.metaschema.model.info.instances.InfoElementInstance;
import gov.nist.secauto.metaschema.model.info.instances.ModelInstance;

public interface ModelContainer extends FlagContainer {
	FieldInstance getFieldInstanceByName(String name);
	AssemblyInstance getAssemblyInstanceByName(String name);
	Map<String, ModelInstance> getNamedModelInstances();
	Map<String, ? extends FieldInstance> getFieldInstances();
	Map<String, ? extends AssemblyInstance> getAssemblyInstances();
	List<InfoElementInstance> getInstances();
}
