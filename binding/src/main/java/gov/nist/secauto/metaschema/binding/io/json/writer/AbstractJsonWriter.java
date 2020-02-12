package gov.nist.secauto.metaschema.binding.io.json.writer;

import java.util.Objects;

import gov.nist.secauto.metaschema.binding.model.ClassBinding;

public abstract class AbstractJsonWriter<CLASS, CLASS_BINDING extends ClassBinding<CLASS>> implements JsonWriter<CLASS> {
	private final CLASS_BINDING classBinding;

	public AbstractJsonWriter(CLASS_BINDING classBinding) {
		Objects.requireNonNull(classBinding, "classBinding");
		this.classBinding = classBinding;
	}

	public CLASS_BINDING getClassBinding() {
		return classBinding;
	}

}
