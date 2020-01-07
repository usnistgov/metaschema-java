package gov.nist.secauto.metaschema.datatype.binding.adapter;

import java.time.ZonedDateTime;

import gov.nist.secauto.metaschema.datatype.DateTimeTimeZone;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class DateTimeWithTZAdapter extends SimpleJavaTypeAdapter<DateTimeTimeZone> {

	@Override
	public DateTimeTimeZone parseValue(String value) throws BindingException {
		return new DateTimeTimeZone(ZonedDateTime.parse(value));
	}

}
