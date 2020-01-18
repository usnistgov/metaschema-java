package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.io.IOException;
import java.math.BigInteger;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.NonNegativeInteger;
import gov.nist.secauto.metaschema.binding.datatypes.PositiveInteger;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.writer.json.FlagPropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.writer.json.JsonWritingContext;

public class PositiveIntegerAdapter extends SimpleJavaTypeAdapter<PositiveInteger> {

	@Override
	public PositiveInteger parse(String value) {
		return new PositiveInteger(new BigInteger(value));
	}

	@Override
	public void writeJsonFieldValue(Object value, FlagPropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException {
		try {
			writingContext.getEventWriter().writeNumber(((NonNegativeInteger)value).getValue());
		} catch (IOException | ClassCastException ex) {
			throw new BindingException(ex);
		}
	}

}
