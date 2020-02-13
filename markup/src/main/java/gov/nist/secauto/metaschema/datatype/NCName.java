package gov.nist.secauto.metaschema.datatype;

public class NCName extends AbstractStringDatatype<NCName> {

	public NCName(String value) {
		super(value);
	}

	@Override
	public NCName copy() {
		return new NCName(getValue());
	}

}
