package gov.nist.secauto.metaschema.datatype;

public abstract class AbstractIPDatatype<TYPE extends AbstractStringDatatype<TYPE>> extends AbstractStringDatatype<TYPE> {

	protected AbstractIPDatatype(String value) {
		super(value);
	}
}
