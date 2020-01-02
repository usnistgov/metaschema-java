package gov.nist.secauto.metaschema.datatype.binding.adapter;

import java.math.BigInteger;

public class IntegerAdapter extends SimpleTypeAdapter<gov.nist.secauto.metaschema.datatype.Integer> {

	@Override
	public gov.nist.secauto.metaschema.datatype.Integer parseValue(String value) {
		return new gov.nist.secauto.metaschema.datatype.Integer(new BigInteger(value));
	}

}
