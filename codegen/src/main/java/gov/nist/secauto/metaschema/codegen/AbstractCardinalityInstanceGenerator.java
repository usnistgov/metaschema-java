package gov.nist.secauto.metaschema.codegen;

import gov.nist.secauto.metaschema.codegen.item.InstanceItemContext;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.MarkupString;

public abstract class AbstractCardinalityInstanceGenerator<JAVA_TYPE extends JavaType> extends AbstractInstanceGenerator<AssemblyClassGenerator> implements CardinalityInstanceGenerator {
	private final InstanceItemContext instanceItemContext;
	private final JAVA_TYPE javaType;

	public AbstractCardinalityInstanceGenerator(InstanceItemContext instanceItemContext, AssemblyClassGenerator classContext, JAVA_TYPE javaType) {
		super(classContext);
		this.instanceItemContext = instanceItemContext;
		this.javaType = javaType;
//
//		
//		String className;
//		if (instance.getMaxOccurs() > 1 || instance.getMaxOccurs() == -1) {
//			StringBuilder classNameBuilder = new StringBuilder();
//			classNameBuilder.append("List<");
//			classNameBuilder.append(getJavaType());
//			classNameBuilder.append(">");
//			className = classNameBuilder.toString();
//			addImport("java.util.List");
//		} else {
//			className = getJavaType();
//		}
//		this.actualJavaType = className;
	}
//
//	protected ModelInstance getInstance() {
//		return instance;
//	}


	protected InstanceItemContext getInstanceItemContext() {
		return instanceItemContext;
	}


	protected JAVA_TYPE getJavaType() {
		return javaType;
	}
	
	@Override
	protected String getInstanceName() {
		return getInstanceItemContext().getInstanceName();
	}

	@Override
	public MarkupString getDescription() {
		return getInstanceItemContext().getDescription();
	}

}
