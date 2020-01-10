package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.math.BigInteger;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.PositiveInteger;

public class PositiveIntegerAdapter extends SimpleJavaTypeAdapter<PositiveInteger> {

	@Override
	public PositiveInteger parse(String value) {
		return new PositiveInteger(new BigInteger(value));
	}

}
