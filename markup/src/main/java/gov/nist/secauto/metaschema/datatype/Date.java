package gov.nist.secauto.metaschema.datatype;

public class Date extends AbstractDatatype<Date, java.util.Date> {

	public Date(java.util.Date value) {
		super(value);
	}

	@Override
	public Date copy() {
		return new Date(getValue());
	}

}
