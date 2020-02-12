package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.Hostname;

public class HostnameAdapter extends SimpleJavaTypeAdapter<Hostname> {

	@Override
	public Hostname parse(String value) throws BindingException {
		return new Hostname(value);
	}

}
