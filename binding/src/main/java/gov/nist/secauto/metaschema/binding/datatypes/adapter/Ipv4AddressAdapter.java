package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.IPv4;

public class Ipv4AddressAdapter extends SimpleJavaTypeAdapter<IPv4> {

	@Override
	public IPv4 parse(String value) {
		return new IPv4(value);
	}

}
