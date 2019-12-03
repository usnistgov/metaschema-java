package gov.nist.secauto.metaschema.codegen;

import java.io.PrintWriter;

import gov.nist.secauto.metaschema.codegen.item.InstanceItemContext;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.MarkupString;

public abstract class AbstractCardinalityInstanceGenerator<JAVA_TYPE extends JavaType> extends AbstractInstanceGenerator<AssemblyClassGenerator> implements CardinalityInstanceGenerator {
	private final InstanceItemContext itemInstanceContext;
	private final JAVA_TYPE javaType;

	public AbstractCardinalityInstanceGenerator(InstanceItemContext itemInstanceContext, AssemblyClassGenerator classContext, JAVA_TYPE javaType) {
		super(classContext);
		this.itemInstanceContext = itemInstanceContext;
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


	protected InstanceItemContext getItemInstanceContext() {
		return itemInstanceContext;
	}


	protected JAVA_TYPE getJavaType() {
		return javaType;
	}
	
	@Override
	protected String getInstanceName() {
		return getItemInstanceContext().getInstanceName();
	}

	@Override
	public MarkupString getDescription() {
		return getItemInstanceContext().getDescription();
	}

	@Override
	protected void writeVariableAnnotations(PrintWriter writer) {
		writer.printf("\t@XmlElement(name = \"%s\", namespace=\"%s\", required = true)%n", getInstanceName(), getItemInstanceContext().getXmlNamespace());
	}

}
