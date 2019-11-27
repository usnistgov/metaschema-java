package gov.nist.secauto.metaschema.model;

import java.util.List;

public interface AssemblyDefinition extends ManagedObject, Assembly, ModelContainer {
	List<? extends ChoiceInstance> getChoiceInstances();

}
