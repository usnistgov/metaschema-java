package gov.nist.secauto.metaschema.datatype.binding.adapter;

import gov.nist.secauto.metaschema.datatype.URIReference;

public class UriReferenceAdapter extends SimpleJavaTypeAdapter<URIReference> {
	@Override
	public URIReference parseValue(String value) {
		return new URIReference(java.net.URI.create(value));
	}
}
