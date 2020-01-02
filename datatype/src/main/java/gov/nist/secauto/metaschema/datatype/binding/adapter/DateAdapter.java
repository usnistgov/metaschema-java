package gov.nist.secauto.metaschema.datatype.binding.adapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import gov.nist.secauto.metaschema.datatype.Date;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class DateAdapter extends SimpleTypeAdapter<Date> {
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddXXX");
	private static final DateFormat dateFormatWithoutTZ = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public Date parseValue(String value) throws BindingException {
		try {
			return new Date(dateFormat.parse(value));
		} catch (ParseException e) {
			try {
				return new Date(dateFormatWithoutTZ.parse(value));
			} catch (ParseException ex) {
				throw new BindingException(ex);
			}
		}
	}

}
