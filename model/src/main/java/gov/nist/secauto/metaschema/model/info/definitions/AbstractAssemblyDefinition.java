package gov.nist.secauto.metaschema.model.info.definitions;

import java.util.Objects;

import com.sun.xml.bind.api.impl.NameConverter;

import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.configuration.AssemblyBindingConfiguration;
import gov.nist.secauto.metaschema.model.info.Type;
import gov.nist.secauto.metaschema.model.info.instances.AssemblyInstance;
import gov.nist.secauto.metaschema.model.info.instances.FieldInstance;
import gov.nist.secauto.metaschema.model.info.instances.FlagInstance;

public abstract class AbstractAssemblyDefinition<METASCHEMA extends Metaschema> extends AbstractInfoElementDefinition<METASCHEMA> implements AssemblyDefinition {
	private final AssemblyBindingConfiguration bindingConfiguration;

	public AbstractAssemblyDefinition(AssemblyBindingConfiguration bindingConfiguration, METASCHEMA metaschema) {
		super(metaschema);
		Objects.requireNonNull(bindingConfiguration, "bindingConfiguration");
		this.bindingConfiguration = bindingConfiguration;
	}

	@Override
	public Type getType() {
		return Type.ASSEMBLY;
	}

	protected AssemblyBindingConfiguration getBindingConfiguration() {
		return bindingConfiguration;
	}

	@Override
	public String getClassName() {
		String retval = getBindingConfiguration().getClassName();
		if (retval == null) {
			retval = NameConverter.standard.toClassName(getName());
		}
		return retval;
	}

	@Override
	public String getPackageName() {
		return getContainingMetaschema().getPackageName();
	}

	@Override
	public FlagInstance getFlagInstanceByName(String name) {
		return getFlagInstances().get(name);
	}

	@Override
	public FieldInstance getFieldInstanceByName(String name) {
		return getFieldInstances().get(name);
	}

	@Override
	public AssemblyInstance getAssemblyInstanceByName(String name) {
		return getAssemblyInstances().get(name);
	}
}
