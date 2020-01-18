package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.io.IOException;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.writer.json.FlagPropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.writer.json.JsonWritingContext;

public class BooleanAdapter extends SimpleJavaTypeAdapter<Boolean> {
	@Override
	public Boolean parse(String value) {
		return Boolean.valueOf(value);
	}

	@Override
	public void writeJsonFieldValue(Object value, FlagPropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException {
		try {
			writingContext.getEventWriter().writeBoolean(((Boolean)value).booleanValue());
		} catch (IOException | ClassCastException ex) {
			throw new BindingException(ex);
		}
	}
}
