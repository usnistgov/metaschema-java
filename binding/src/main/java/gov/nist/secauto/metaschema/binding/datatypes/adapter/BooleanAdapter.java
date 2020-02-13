package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.io.IOException;

import gov.nist.secauto.metaschema.binding.AbstractJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public class BooleanAdapter extends AbstractJavaTypeAdapter<Boolean> {
	@Override
	public Boolean parse(String value) {
		return Boolean.valueOf(value);
	}

	@Override
	public Boolean parse(JsonParsingContext parsingContext) throws BindingException {
		try {
			return parsingContext.getEventReader().getBooleanValue();
		} catch (IOException ex) {
			throw new BindingException(ex);
		}
	}

	@Override
	public void writeJsonFieldValue(Object value, PropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException {
		try {
			writingContext.getEventWriter().writeBoolean(((Boolean)value).booleanValue());
		} catch (IOException | ClassCastException ex) {
			throw new BindingException(ex);
		}
	}

	@Override
	public Boolean copy(Boolean obj) {
		return Boolean.valueOf(obj.booleanValue());
	}
}
