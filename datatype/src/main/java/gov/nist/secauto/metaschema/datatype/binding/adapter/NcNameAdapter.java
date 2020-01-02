package gov.nist.secauto.metaschema.datatype.binding.adapter;

import gov.nist.secauto.metaschema.datatype.NCName;

public class NcNameAdapter extends SimpleTypeAdapter<NCName> {

	@Override
	public NCName parseValue(String value) {
		return new NCName(value);
	}

}
