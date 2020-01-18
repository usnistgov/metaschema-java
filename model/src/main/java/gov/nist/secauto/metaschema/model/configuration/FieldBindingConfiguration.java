package gov.nist.secauto.metaschema.model.configuration;

import java.util.List;

public class FieldBindingConfiguration extends AbstractManagedObjectBingingConfiguration {

	public static final FieldBindingConfiguration NULL_CONFIG;
	static {
		NULL_CONFIG = new FieldBindingConfiguration(null, null, null);
	}

	public FieldBindingConfiguration(String className, String baseClass,
			List<String> interfacesToImplement) {
		super(className, baseClass, interfacesToImplement);
	}

}
