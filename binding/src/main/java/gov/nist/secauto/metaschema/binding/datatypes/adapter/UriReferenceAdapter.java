package gov.nist.secauto.metaschema.binding.datatypes.adapter;

import gov.nist.secauto.metaschema.binding.SimpleJavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.datatypes.URIReference;

public class UriReferenceAdapter extends SimpleJavaTypeAdapter<URIReference> {
	@Override
	public URIReference parse(String value) {
		return new URIReference(java.net.URI.create(value));
	}
}
