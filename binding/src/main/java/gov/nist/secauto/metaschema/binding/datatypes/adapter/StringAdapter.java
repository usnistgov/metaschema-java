package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.AbstractJavaTypeAdapter;

public class StringAdapter extends AbstractJavaTypeAdapter<String> {
	@Override
	public String parse(String value) {
		return value;
	}

	@Override
	public String copy(String obj) {
		return obj;
	}
}
