package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.DateTimeZone;
import gov.nist.secauto.metaschema.binding.parser.BindingException;

public class DateWithTZAdapter extends SimpleJavaTypeAdapter<DateTimeZone> {

	@Override
	public DateTimeZone parse(String value) throws BindingException {
		throw new UnsupportedOperationException();
	}

}
