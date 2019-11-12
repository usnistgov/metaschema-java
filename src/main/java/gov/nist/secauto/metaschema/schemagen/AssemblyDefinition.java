package gov.nist.secauto.metaschema.schemagen;

import java.util.List;

public interface AssemblyDefinition extends InfoElementDefinition, Assembly, ModelContainer {
	List<? extends ChoiceInstance> getChoiceInstances();

}
