package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.NCName;

public class NcNameAdapter extends SimpleJavaTypeAdapter<NCName> {

	@Override
	public NCName parse(String value) {
		return new NCName(value);
	}

}
