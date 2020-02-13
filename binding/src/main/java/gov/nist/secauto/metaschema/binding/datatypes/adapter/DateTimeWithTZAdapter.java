package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import java.time.ZonedDateTime;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.DateTimeTimeZone;

public class DateTimeWithTZAdapter extends SimpleJavaTypeAdapter<DateTimeTimeZone> {

	@Override
	public DateTimeTimeZone parse(String value) throws BindingException {
		return new DateTimeTimeZone(ZonedDateTime.parse(value));
	}

}
