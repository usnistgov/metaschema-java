package gov.nist.secauto.metaschema.datatype;

public class EmailAddress extends AbstractStringDatatype<EmailAddress> {

	public EmailAddress(String value) {
		super(value);
	}

	@Override
	public EmailAddress copy() {
		return new EmailAddress(getValue());
	}

}
