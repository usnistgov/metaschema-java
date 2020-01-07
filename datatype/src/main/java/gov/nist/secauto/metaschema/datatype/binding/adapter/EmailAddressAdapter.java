package gov.nist.secauto.metaschema.datatype.binding.adapter;

import gov.nist.secauto.metaschema.datatype.EmailAddress;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class EmailAddressAdapter extends SimpleJavaTypeAdapter<EmailAddress> {

	@Override
	public EmailAddress parseValue(String value) throws BindingException {
		return new EmailAddress(value);
	}

}
