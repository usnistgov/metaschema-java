package gov.nist.secauto.metaschema.datatype.binding.adapter;

public class BooleanAdapter extends SimpleTypeAdapter<Boolean> {
	@Override
	public Boolean parseValue(String value) {
		return Boolean.valueOf(value);
	}
}
