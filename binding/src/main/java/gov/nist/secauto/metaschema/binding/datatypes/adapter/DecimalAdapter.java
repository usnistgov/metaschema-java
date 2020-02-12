package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.io.IOException;
import java.math.BigDecimal;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.Decimal;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

public class DecimalAdapter extends SimpleJavaTypeAdapter<Decimal> {

	@Override
	public Decimal parse(String value) {
		return new Decimal(new BigDecimal(value));
	}

	@Override
	public void writeJsonFieldValue(Object value, NamedPropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException {
		try {
			writingContext.getEventWriter().writeNumber(((Decimal)value).getValue());
		} catch (IOException | ClassCastException ex) {
			throw new BindingException(ex);
		}
	}

}
