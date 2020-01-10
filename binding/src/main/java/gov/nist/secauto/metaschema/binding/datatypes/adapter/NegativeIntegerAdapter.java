package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.math.BigInteger;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.NonNegativeInteger;

public class NegativeIntegerAdapter extends SimpleJavaTypeAdapter<NonNegativeInteger> {

	@Override
	public NonNegativeInteger parse(String value) {
		return new NonNegativeInteger(new BigInteger(value));
	}

}
