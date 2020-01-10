package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;

public class StringAdapter extends SimpleJavaTypeAdapter<String> {
	@Override
	public String parse(String value) {
		return value;
	}
}
