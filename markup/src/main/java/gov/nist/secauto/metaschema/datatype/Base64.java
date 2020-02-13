package gov.nist.secauto.metaschema.datatype;

public class Base64 extends AbstractStringDatatype<Base64> {

	public Base64(String value) {
		super(value);
	}

	@Override
	public Base64 copy() {
		return new Base64(getValue());
	}
}
