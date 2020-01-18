package gov.nist.secauto.metaschema.model.info.definitions;

import java.util.List;

import gov.nist.secauto.metaschema.model.info.Assembly;
import gov.nist.secauto.metaschema.model.info.instances.ChoiceInstance;

public interface AssemblyDefinition extends ManagedObject, Assembly, ModelContainer {
	List<? extends ChoiceInstance> getChoiceInstances();

}
