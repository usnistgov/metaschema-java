package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.io.IOException;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

public class BooleanAdapter extends SimpleJavaTypeAdapter<Boolean> {
	@Override
	public Boolean parse(String value) {
		return Boolean.valueOf(value);
	}

	@Override
	public void writeJsonFieldValue(Object value, NamedPropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException {
		try {
			writingContext.getEventWriter().writeBoolean(((Boolean)value).booleanValue());
		} catch (IOException | ClassCastException ex) {
			throw new BindingException(ex);
		}
	}
}
