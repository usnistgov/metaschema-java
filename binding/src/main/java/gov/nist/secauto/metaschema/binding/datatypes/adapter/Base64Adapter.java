package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.Base64;

public class Base64Adapter extends SimpleJavaTypeAdapter<Base64> {
	@Override
	public Base64 parse(String value) {
		return new Base64(value);
	}
}
