package gov.nist.secauto.metaschema.datatype;

public class URI extends AbstractURIDatatype<URI> {

	public URI(java.net.URI value) {
		super(value);
	}

	@Override
	public URI copy() {
		return new URI(getValue());
	}

}
