package gov.nist.secauto.metaschema.datatype.binding.adapter;

import java.math.BigInteger;

import gov.nist.secauto.metaschema.datatype.PositiveInteger;

public class PositiveIntegerAdapter extends SimpleJavaTypeAdapter<PositiveInteger> {

	@Override
	public PositiveInteger parseValue(String value) {
		return new PositiveInteger(new BigInteger(value));
	}

}
