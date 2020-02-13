package gov.nist.secauto.metaschema.datatype;

public class IPv4 extends AbstractIPDatatype<IPv4> {

	public IPv4(String value) {
		super(value);
	}

	@Override
	public IPv4 copy() {
		return new IPv4(getValue());
	}

}
