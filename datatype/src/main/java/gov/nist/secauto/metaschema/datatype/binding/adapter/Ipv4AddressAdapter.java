package gov.nist.secauto.metaschema.datatype.binding.adapter;

import gov.nist.secauto.metaschema.datatype.IPv4;

public class Ipv4AddressAdapter extends SimpleTypeAdapter<IPv4> {

	@Override
	public IPv4 parseValue(String value) {
		return new IPv4(value);
	}

}
