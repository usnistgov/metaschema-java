package gov.nist.secauto.metaschema.datatype;

import java.util.Objects;

public abstract class AbstractDatatype<T> {
	private T value;
	
	public AbstractDatatype(T value) {
		Objects.requireNonNull(value, "value");
		this.value = value;
	}
//
//	public T getValue() {
//		return value;
//	}
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
