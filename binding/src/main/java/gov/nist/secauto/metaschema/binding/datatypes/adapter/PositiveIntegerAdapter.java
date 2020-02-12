package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.io.IOException;
import java.math.BigInteger;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.NonNegativeInteger;
import gov.nist.secauto.metaschema.binding.datatypes.PositiveInteger;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public class PositiveIntegerAdapter extends SimpleJavaTypeAdapter<PositiveInteger> {

	@Override
	public PositiveInteger parse(String value) {
		return new PositiveInteger(new BigInteger(value));
	}

	@Override
	public void writeJsonFieldValue(Object value, PropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException {
		try {
			writingContext.getEventWriter().writeNumber(((NonNegativeInteger)value).getValue());
		} catch (IOException | ClassCastException ex) {
			throw new BindingException(ex);
		}
	}

}
