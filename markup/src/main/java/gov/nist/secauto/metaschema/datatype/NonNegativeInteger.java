package gov.nist.secauto.metaschema.datatype;

import java.math.BigInteger;

public class NonNegativeInteger extends AbstractIntegerDatatype<NonNegativeInteger> {

	public NonNegativeInteger(BigInteger value) {
		super(value);
		if (value.signum() == -1) {
			throw new IllegalArgumentException("The value must be non-negative");
		}
	}

	@Override
	public NonNegativeInteger copy() {
		return new NonNegativeInteger(getValue());
	}

}
