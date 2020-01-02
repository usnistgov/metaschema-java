package gov.nist.secauto.metaschema.datatype.binding.adapter;

import gov.nist.secauto.metaschema.datatype.DateTimeZone;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class DateWithTZAdapter extends SimpleTypeAdapter<DateTimeZone> {

	@Override
	public DateTimeZone parseValue(String value) throws BindingException {
		throw new UnsupportedOperationException();
	}

}
