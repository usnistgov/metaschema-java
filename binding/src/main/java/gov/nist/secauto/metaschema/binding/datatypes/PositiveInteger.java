package gov.nist.secauto.metaschema.binding.datatypes;

import java.math.BigInteger;

public class PositiveInteger extends AbstractIntegerDatatype {

	public PositiveInteger(BigInteger value) {
		super(value);
		if (value.signum() != 1) {
			throw new IllegalArgumentException("The value must be positive");
		}
	}
}
