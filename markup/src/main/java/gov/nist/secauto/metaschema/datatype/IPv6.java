package gov.nist.secauto.metaschema.datatype;

public class IPv6 extends AbstractIPDatatype<IPv6> {

	public IPv6(String value) {
		super(value);
	}

	@Override
	public IPv6 copy() {
		return new IPv6(getValue());
	}

}
