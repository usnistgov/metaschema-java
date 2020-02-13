package gov.nist.secauto.metaschema.binding;

import gov.nist.secauto.metaschema.datatype.Datatype;

// TODO: rename to DatatypeJavaTypeAdapter
public abstract class SimpleJavaTypeAdapter<TYPE extends Datatype<TYPE>> extends AbstractJavaTypeAdapter<TYPE> {
	
	@Override
	public TYPE copy(TYPE obj) {
		return obj.copy();
	}

}
