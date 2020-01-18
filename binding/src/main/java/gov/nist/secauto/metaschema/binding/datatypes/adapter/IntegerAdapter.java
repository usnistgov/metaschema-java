package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.io.IOException;
import java.math.BigInteger;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.writer.json.FlagPropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.writer.json.JsonWritingContext;

public class IntegerAdapter extends SimpleJavaTypeAdapter<gov.nist.secauto.metaschema.binding.datatypes.Integer> {

	@Override
	public gov.nist.secauto.metaschema.binding.datatypes.Integer parse(String value) {
		return new gov.nist.secauto.metaschema.binding.datatypes.Integer(new BigInteger(value));
	}

	@Override
	public void writeJsonFieldValue(Object value, FlagPropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException {
		try {
			writingContext.getEventWriter().writeNumber(((gov.nist.secauto.metaschema.binding.datatypes.Integer)value).getValue());
		} catch (IOException | ClassCastException ex) {
			throw new BindingException(ex);
		}
	}

}
