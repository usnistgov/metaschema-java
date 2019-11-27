package gov.nist.secauto.metaschema.codegen.type;

import java.util.List;

import gov.nist.secauto.metaschema.codegen.AbstractClassGenerator;

public class ListJavaType extends AbstractCollectionJavaType {

	public ListJavaType(JavaType itemClass) {
		super(List.class, itemClass);
	}

	@Override
	protected String getGenerics(AbstractClassGenerator<?> classContext) {
		return getItemClass().getType(classContext);
	}

}
