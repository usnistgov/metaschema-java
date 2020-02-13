package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.DateTime;

public class DateTimeAdapter extends SimpleJavaTypeAdapter<DateTime> {

	@Override
	public DateTime parse(String value) {
		throw new UnsupportedOperationException();
	}

}
