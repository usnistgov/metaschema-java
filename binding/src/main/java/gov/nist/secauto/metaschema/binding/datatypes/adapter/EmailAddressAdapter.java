package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.EmailAddress;

public class EmailAddressAdapter extends SimpleJavaTypeAdapter<EmailAddress> {

	@Override
	public EmailAddress parse(String value) throws BindingException {
		return new EmailAddress(value);
	}

}
