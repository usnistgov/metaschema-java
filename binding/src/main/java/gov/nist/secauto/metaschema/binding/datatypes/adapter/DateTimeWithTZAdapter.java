package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.time.ZonedDateTime;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.DateTimeTimeZone;
import gov.nist.secauto.metaschema.binding.parser.BindingException;

public class DateTimeWithTZAdapter extends SimpleJavaTypeAdapter<DateTimeTimeZone> {

	@Override
	public DateTimeTimeZone parse(String value) throws BindingException {
		return new DateTimeTimeZone(ZonedDateTime.parse(value));
	}

}
