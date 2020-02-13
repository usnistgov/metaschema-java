package gov.nist.secauto.metaschema.datatype;

import java.time.ZonedDateTime;

public class DateTimeTimeZone extends AbstractDatatype<DateTimeTimeZone, ZonedDateTime> {

	public DateTimeTimeZone(ZonedDateTime value) {
		super(value);
	}

	@Override
	public DateTimeTimeZone copy() {
		return new DateTimeTimeZone(getValue());
	}

}
