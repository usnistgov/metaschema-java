package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.io.IOException;
import java.math.BigDecimal;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.Decimal;
import gov.nist.secauto.metaschema.binding.parser.BindingException;
import gov.nist.secauto.metaschema.binding.writer.json.FlagPropertyBindingFilter;
import gov.nist.secauto.metaschema.binding.writer.json.JsonWritingContext;

public class DecimalAdapter extends SimpleJavaTypeAdapter<Decimal> {

	@Override
	public Decimal parse(String value) {
		return new Decimal(new BigDecimal(value));
	}

	@Override
	public void writeJsonFieldValue(Object value, FlagPropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException {
		try {
			writingContext.getEventWriter().writeNumber(((Decimal)value).getValue());
		} catch (IOException | ClassCastException ex) {
			throw new BindingException(ex);
		}
	}

}
