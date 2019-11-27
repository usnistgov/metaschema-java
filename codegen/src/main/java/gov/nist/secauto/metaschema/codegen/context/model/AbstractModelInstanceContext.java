package gov.nist.secauto.metaschema.codegen.context.model;

import java.io.PrintWriter;

import gov.nist.secauto.metaschema.codegen.AssemblyClassGenerator;
import gov.nist.secauto.metaschema.codegen.context.AbstractInstanceContext;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.MarkupString;

public abstract class AbstractModelInstanceContext<JAVA_TYPE extends JavaType> extends AbstractInstanceContext<AssemblyClassGenerator> implements ModelInstanceContext {
	private final ModelItemInstanceContext itemInstanceContext;
	private final JAVA_TYPE javaType;

	public AbstractModelInstanceContext(ModelItemInstanceContext itemInstanceContext, AssemblyClassGenerator classContext, JAVA_TYPE javaType) {
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


	protected ModelItemInstanceContext getItemInstanceContext() {
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
