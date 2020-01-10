package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.Hostname;
import gov.nist.secauto.metaschema.binding.parser.BindingException;

public class HostnameAdapter extends SimpleJavaTypeAdapter<Hostname> {

	@Override
	public Hostname parse(String value) throws BindingException {
		return new Hostname(value);
	}

}
