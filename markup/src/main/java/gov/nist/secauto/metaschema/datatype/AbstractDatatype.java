package gov.nist.secauto.metaschema.datatype;

import java.util.Objects;

public abstract class AbstractDatatype<TYPE extends Datatype<TYPE>, VALUE> implements Datatype<TYPE> {
	private final VALUE value;
	
	protected AbstractDatatype(VALUE value) {
		Objects.requireNonNull(value, "value");
		this.value = value;
	}

	public VALUE getValue() {
		return value;
	}
//
//	public void setValue(T value) {
//		this.value = value;
//	}

	@Override
	public int hashCode() {
		return value == null ? 0 : value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return Objects.equals(value, obj);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
