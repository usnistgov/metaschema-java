package gov.nist.secauto.metaschema.binding;

import java.util.Objects;

import gov.nist.secauto.metaschema.binding.io.Configuration;
import gov.nist.secauto.metaschema.binding.io.MutableConfiguration;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;

abstract class AbstractSerializationBase<CLASS> {
	private final BindingContext bindingContext;
	private final AssemblyClassBinding<CLASS> classBinding;
	private final Configuration configuration;

	public AbstractSerializationBase(BindingContext bindingContext, AssemblyClassBinding<CLASS> classBinding,
			Configuration configuration) {
		Objects.requireNonNull(bindingContext, "bindingContext");
		Objects.requireNonNull(classBinding, "classBinding");
		this.bindingContext = bindingContext;
		this.classBinding = classBinding;
		if (configuration == null) {
			this.configuration = new MutableConfiguration();
		} else {
			this.configuration = configuration;
		}
	}

	protected BindingContext getBindingContext() {
		return bindingContext;
	}

	protected AssemblyClassBinding<CLASS> getClassBinding() {
		return classBinding;
	}

	protected Configuration getConfiguration() {
		return configuration;
	}
}
