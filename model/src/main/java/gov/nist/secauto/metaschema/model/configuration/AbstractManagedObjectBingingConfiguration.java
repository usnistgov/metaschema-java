package gov.nist.secauto.metaschema.model.configuration;

import java.util.Collections;
import java.util.List;

public class AbstractManagedObjectBingingConfiguration implements ManagedObjectBingingConfiguration {
	private final String className;
	private final String baseClassName;
	private final List<String> interfacesToImplement;

	public AbstractManagedObjectBingingConfiguration(String className, String baseClassName,
			List<String> interfacesToImplement) {
		this.className = className;
		this.baseClassName = baseClassName;
		this.interfacesToImplement = interfacesToImplement != null ? Collections.unmodifiableList(interfacesToImplement) : Collections.emptyList();
	}

	@Override
	public String getClassName() {
		return className;
	}
	@Override
	public String getQualifiedBaseClassName() {
		return baseClassName;
	}
	@Override
	public List<String> getInterfacesToImplement() {
		return interfacesToImplement;
	}

	
}
