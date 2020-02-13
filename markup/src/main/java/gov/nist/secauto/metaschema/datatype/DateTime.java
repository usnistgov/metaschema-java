package gov.nist.secauto.metaschema.datatype;

import java.util.Date;

public class DateTime extends AbstractDatatype<DateTime, java.util.Date> {

	public DateTime(Date value) {
		super(value);
	}

	@Override
	public DateTime copy() {
		return new DateTime(getValue());
	}

}
