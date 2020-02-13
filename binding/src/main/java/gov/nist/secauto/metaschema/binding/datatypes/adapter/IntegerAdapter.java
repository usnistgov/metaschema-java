package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.io.IOException;
import java.math.BigInteger;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

public class IntegerAdapter extends SimpleJavaTypeAdapter<gov.nist.secauto.metaschema.datatype.Integer> {

	@Override
	public gov.nist.secauto.metaschema.datatype.Integer parse(String value) {
		return new gov.nist.secauto.metaschema.datatype.Integer(new BigInteger(value));
	}

	@Override
	public void writeJsonFieldValue(Object value, PropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException {
		try {
			writingContext.getEventWriter().writeNumber(((gov.nist.secauto.metaschema.datatype.Integer)value).getValue());
		} catch (IOException | ClassCastException ex) {
			throw new BindingException(ex);
		}
	}

}
