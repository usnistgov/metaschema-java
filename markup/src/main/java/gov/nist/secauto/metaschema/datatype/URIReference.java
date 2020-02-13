package gov.nist.secauto.metaschema.datatype;

public class URIReference extends AbstractURIDatatype<URIReference> {

	public URIReference(java.net.URI value) {
		super(value);
	}

	@Override
	public URIReference copy() {
		return new URIReference(getValue());
	}

}
