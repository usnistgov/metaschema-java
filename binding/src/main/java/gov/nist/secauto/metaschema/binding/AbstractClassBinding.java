package gov.nist.secauto.metaschema.binding;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.property.FlagPropertyBinding;

public abstract class AbstractClassBinding<CLASS> implements ClassBinding<CLASS> {
	private final Class<CLASS> clazz;
	private final List<FlagPropertyBinding> flagPropertyBindings;

	public AbstractClassBinding(Class<CLASS> clazz) throws BindingException {
		Objects.requireNonNull(clazz, "clazz");
		this.clazz = clazz;
		this.flagPropertyBindings = Collections.unmodifiableList(ClassIntrospector.getFlagPropertyBindings(clazz));
	}

	@Override
	public Class<CLASS> getClazz() {
		return clazz;
	}

	@Override
	public List<FlagPropertyBinding> getFlagPropertyBindings() {
		return flagPropertyBindings;
	}
}
