package gov.nist.secauto.metaschema.datatype.binding.adapter;

public class StringAdapter extends SimpleJavaTypeAdapter<String> {
	@Override
	public String parseValue(String value) {
		return value;
	}
}
