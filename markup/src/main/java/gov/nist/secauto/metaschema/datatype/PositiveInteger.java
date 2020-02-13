package gov.nist.secauto.metaschema.datatype;

import java.math.BigInteger;

public class PositiveInteger extends AbstractIntegerDatatype<PositiveInteger> {

	public PositiveInteger(BigInteger value) {
		super(value);
		if (value.signum() != 1) {
			throw new IllegalArgumentException("The value must be positive");
		}
	}

	@Override
	public PositiveInteger copy() {
		return new PositiveInteger(getValue());
	}
}
