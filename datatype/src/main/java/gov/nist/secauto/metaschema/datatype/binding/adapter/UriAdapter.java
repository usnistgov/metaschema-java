package gov.nist.secauto.metaschema.datatype.binding.adapter;

import gov.nist.secauto.metaschema.datatype.URI;

public class UriAdapter extends SimpleTypeAdapter<URI> {
	@Override
	public URI parseValue(String value) {
		return new URI(java.net.URI.create(value));
	}
}
