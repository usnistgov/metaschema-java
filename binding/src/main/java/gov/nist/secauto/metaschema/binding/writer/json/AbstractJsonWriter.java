package gov.nist.secauto.metaschema.binding.writer.json;

import java.util.Objects;

import gov.nist.secauto.metaschema.binding.ClassBinding;

public abstract class AbstractJsonWriter<CLASS, CLASS_BINDING extends ClassBinding<CLASS>> implements JsonWriter {
	private final CLASS_BINDING classBinding;

	public AbstractJsonWriter(CLASS_BINDING classBinding) {
		Objects.requireNonNull(classBinding, "classBinding");
		this.classBinding = classBinding;
	}

	public CLASS_BINDING getClassBinding() {
		return classBinding;
	}

}
