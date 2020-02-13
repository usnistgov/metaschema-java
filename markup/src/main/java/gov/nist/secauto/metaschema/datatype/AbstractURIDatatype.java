package gov.nist.secauto.metaschema.datatype;

public abstract class AbstractURIDatatype<TYPE extends AbstractDatatype<TYPE, java.net.URI>> extends AbstractDatatype<TYPE, java.net.URI> {

	protected AbstractURIDatatype(java.net.URI value) {
		super(value);
	}

}
