package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.EmailAddress;
import gov.nist.secauto.metaschema.binding.parser.BindingException;

public class EmailAddressAdapter extends SimpleJavaTypeAdapter<EmailAddress> {

	@Override
	public EmailAddress parse(String value) throws BindingException {
		return new EmailAddress(value);
	}

}
