package gov.nist.secauto.metaschema.datatype.binding.adapter;

import java.math.BigInteger;

import gov.nist.secauto.metaschema.datatype.NonNegativeInteger;

public class NegativeIntegerAdapter extends SimpleTypeAdapter<NonNegativeInteger> {

	@Override
	public NonNegativeInteger parseValue(String value) {
		return new NonNegativeInteger(new BigInteger(value));
	}

}
