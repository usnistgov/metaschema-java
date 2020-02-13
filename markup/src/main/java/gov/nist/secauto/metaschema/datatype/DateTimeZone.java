package gov.nist.secauto.metaschema.datatype;

import java.time.LocalDateTime;

public class DateTimeZone extends AbstractDatatype<DateTimeZone, LocalDateTime> {

	public DateTimeZone(LocalDateTime value) {
		super(value);
	}

	@Override
	public DateTimeZone copy() {
		return new DateTimeZone(getValue());
	}

}
