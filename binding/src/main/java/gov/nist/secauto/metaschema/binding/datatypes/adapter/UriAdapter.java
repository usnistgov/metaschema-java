package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.datatype.URI;

public class UriAdapter extends SimpleJavaTypeAdapter<URI> {
	@Override
	public URI parse(String value) {
		return new URI(java.net.URI.create(value));
	}
}
