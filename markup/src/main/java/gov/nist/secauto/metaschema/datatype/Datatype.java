package gov.nist.secauto.metaschema.datatype;

public interface Datatype<TYPE extends Datatype<TYPE>> {

	TYPE copy();

}
