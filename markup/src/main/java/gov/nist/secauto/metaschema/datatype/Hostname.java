package gov.nist.secauto.metaschema.datatype;

public class Hostname extends AbstractStringDatatype<Hostname> {

	public Hostname(String value) {
		super(value);
	}

	@Override
	public Hostname copy() {
		return new Hostname(getValue());
	}

}
