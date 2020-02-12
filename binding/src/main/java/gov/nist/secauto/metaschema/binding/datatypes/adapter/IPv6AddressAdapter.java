package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.IPv6;

public class IPv6AddressAdapter extends SimpleJavaTypeAdapter<IPv6> {

	@Override
	public IPv6 parse(String value) throws BindingException {
		return new IPv6(value);
	}

}
