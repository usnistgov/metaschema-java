package gov.nist.secauto.metaschema.datatype;

public abstract class AbstractStringDatatype<TYPE extends AbstractDatatype<TYPE, String>> extends AbstractDatatype<TYPE, String> {

	protected AbstractStringDatatype(String value) {
		super(value);
	}
}
