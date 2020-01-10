package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;

public class BooleanAdapter extends SimpleJavaTypeAdapter<Boolean> {
	@Override
	public Boolean parse(String value) {
		return Boolean.valueOf(value);
	}
}
