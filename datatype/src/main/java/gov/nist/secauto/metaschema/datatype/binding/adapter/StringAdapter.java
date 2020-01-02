package gov.nist.secauto.metaschema.datatype.binding.adapter;

public class StringAdapter extends SimpleTypeAdapter<String> {
	@Override
	public String parseValue(String value) {
		return value;
	}
}
