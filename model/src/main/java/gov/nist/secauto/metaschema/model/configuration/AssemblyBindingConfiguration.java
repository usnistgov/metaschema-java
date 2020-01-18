package gov.nist.secauto.metaschema.model.configuration;

import java.util.List;

public class AssemblyBindingConfiguration extends AbstractManagedObjectBingingConfiguration {

	public static final AssemblyBindingConfiguration NULL_CONFIG;
	static {
		NULL_CONFIG = new AssemblyBindingConfiguration(null, null, null);
	}

	public AssemblyBindingConfiguration(String className, String baseClass,
			List<String> interfacesToImplement) {
		super(className, baseClass, interfacesToImplement);
	}

}
