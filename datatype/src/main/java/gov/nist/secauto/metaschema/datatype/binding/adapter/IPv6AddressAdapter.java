package gov.nist.secauto.metaschema.datatype.binding.adapter;

import gov.nist.secauto.metaschema.datatype.IPv6;
import gov.nist.secauto.metaschema.datatype.parser.BindingException;

public class IPv6AddressAdapter extends SimpleJavaTypeAdapter<IPv6> {

	@Override
	public IPv6 parseValue(String value) throws BindingException {
		return new IPv6(value);
	}

}
