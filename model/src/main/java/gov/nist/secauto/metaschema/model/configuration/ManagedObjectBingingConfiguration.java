package gov.nist.secauto.metaschema.model.configuration;

import java.util.List;

public interface ManagedObjectBingingConfiguration extends BindingConfiguration {
	String getClassName();
	String getQualifiedBaseClassName();
	List<String> getInterfacesToImplement();
}
