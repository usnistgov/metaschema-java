package gov.nist.secauto.metaschema.codegen.context;

import gov.nist.secauto.metaschema.codegen.ModelInstanceGenerator;

public class ModelInstanceContext extends AbstractInstanceContext<ModelInstanceGenerator> {
	private final String actualJavaType;

	public ModelInstanceContext(ModelInstanceGenerator instanceGenerator, ClassContext classContext) {
		super(instanceGenerator, classContext);
		String className;
		if (getInstanceGenerator().getMaxOccurs() > 1 || getInstanceGenerator().getMaxOccurs() == -1) {
			StringBuilder classNameBuilder = new StringBuilder();
			classNameBuilder.append("List<");
			classNameBuilder.append(getJavaType());
			classNameBuilder.append(">");
			className = classNameBuilder.toString();
			addImport("java.util.List");
		} else {
			className = getJavaType();
		}
		this.actualJavaType = className;
	}

	@Override
	protected String getActualJavaType() {
		return actualJavaType;
	}
}
