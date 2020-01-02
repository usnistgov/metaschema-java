package gov.nist.secauto.metaschema.datatype.binding.adapter;

import gov.nist.secauto.metaschema.datatype.Hostname;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class HostnameAdapter extends SimpleTypeAdapter<Hostname> {

	@Override
	public Hostname parseValue(String value) throws BindingException {
		return new Hostname(value);
	}

}
