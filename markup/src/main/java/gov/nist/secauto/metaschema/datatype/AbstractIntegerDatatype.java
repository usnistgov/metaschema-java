package gov.nist.secauto.metaschema.datatype;

import java.math.BigInteger;

public abstract class AbstractIntegerDatatype<TYPE extends AbstractDatatype<TYPE, BigInteger>> extends AbstractDatatype<TYPE, BigInteger> {

	protected AbstractIntegerDatatype(BigInteger value) {
		super(value);
	}
}
