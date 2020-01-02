package gov.nist.secauto.metaschema.datatype.binding.adapter;

import gov.nist.secauto.metaschema.datatype.Base64;

public class Base64Adapter extends SimpleTypeAdapter<Base64> {
	@Override
	public Base64 parseValue(String value) {
		return new Base64(value);
	}
}
