package gov.nist.secauto.metaschema.datatype;

import java.math.BigInteger;

public class Integer extends AbstractIntegerDatatype<Integer> {

	public Integer(BigInteger value) {
		super(value);
	}

	@Override
	public Integer copy() {
		return new Integer(getValue());
	}

}
