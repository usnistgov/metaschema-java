package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.math.BigInteger;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;

public class IntegerAdapter extends SimpleJavaTypeAdapter<gov.nist.secauto.metaschema.binding.datatypes.Integer> {

	@Override
	public gov.nist.secauto.metaschema.binding.datatypes.Integer parse(String value) {
		return new gov.nist.secauto.metaschema.binding.datatypes.Integer(new BigInteger(value));
	}

}
