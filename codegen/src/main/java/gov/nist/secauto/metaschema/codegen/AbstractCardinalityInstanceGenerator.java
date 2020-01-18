package gov.nist.secauto.metaschema.codegen;

import gov.nist.secauto.metaschema.binding.annotations.Assembly;
import gov.nist.secauto.metaschema.binding.annotations.Field;
import gov.nist.secauto.metaschema.binding.annotations.GroupAs;
import gov.nist.secauto.metaschema.codegen.builder.FieldBuilder;
import gov.nist.secauto.metaschema.codegen.item.InstanceItemContext;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.markup.MarkupString;
import gov.nist.secauto.metaschema.model.info.instances.AssemblyInstance;
import gov.nist.secauto.metaschema.model.info.instances.FieldInstance;
import gov.nist.secauto.metaschema.model.info.instances.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.model.info.instances.ModelInstance;
import gov.nist.secauto.metaschema.model.info.instances.XmlGroupAsBehavior;

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


	protected ModelInstance getModelInstance() {
		return getInstanceItemContext().getModelInstance();
	}

	protected InstanceItemContext getInstanceItemContext() {
		return instanceItemContext;
	}


	@Override
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

	@Override
	public void buildField(FieldBuilder builder) {
		
		ModelInstance modelInstance = getModelInstance();
		
		StringBuilder instanceBuilder = new StringBuilder();
		instanceBuilder.append("name = \"");
		instanceBuilder.append(getModelInstance().getDefinition().getName());
		instanceBuilder.append("\"");

		String namespace = getInstanceItemContext().getXmlNamespace().toString();
		String containingNamespace = getModelInstance().getContainingMetaschema().getXmlNamespace().toString();
		if (!containingNamespace.equals(namespace)) {
			instanceBuilder.append(", namespace = \"");
			instanceBuilder.append(namespace);
			instanceBuilder.append("\"");
		}

		boolean isRequired = getModelInstance().getMinOccurs() > 0;
		if (isRequired) {
			instanceBuilder.append(", required = true");
		}

		if (modelInstance instanceof FieldInstance) {
			if (!((FieldInstance)modelInstance).hasXmlWrapper()) {
				instanceBuilder.append(", inXmlWrapped = false");
			}
			builder.annotation(Field.class, instanceBuilder.toString());
		} else if (modelInstance instanceof AssemblyInstance) {
			builder.annotation(Assembly.class, instanceBuilder.toString());
		}

		int maxOccurs = modelInstance.getMaxOccurs();
		if (maxOccurs == -1 || maxOccurs > 1) {
			int minOccurs = modelInstance.getMinOccurs();
			XmlGroupAsBehavior xmlGroupAsBehavior = modelInstance.getXmlGroupAsBehavior();
			JsonGroupAsBehavior jsonGroupAsBehavior = modelInstance.getJsonGroupAsBehavior();
	
			StringBuilder groupAs = new StringBuilder();
			groupAs.append("name = \"");
			groupAs.append(getInstanceName());
			groupAs.append("\"");

			if (!containingNamespace.equals(namespace)) {
				groupAs.append(", namespace = \"");
				groupAs.append(namespace);
				groupAs.append("\"");
			}

			if (minOccurs != 0) {
				groupAs.append(", minOccurs = ");
				groupAs.append(minOccurs);
			}
			if (maxOccurs != 1) {
				groupAs.append(", maxOccurs = ");
				groupAs.append(maxOccurs);
			}
	
			if (!JsonGroupAsBehavior.SINGLETON_OR_LIST.equals(jsonGroupAsBehavior)) {
				groupAs.append(", inJson = "+gov.nist.secauto.metaschema.binding.annotations.JsonGroupAsBehavior.class.getName()+".");
				groupAs.append(jsonGroupAsBehavior.toString());
			}
			if (!XmlGroupAsBehavior.UNGROUPED.equals(xmlGroupAsBehavior)) {
				groupAs.append(", inXml = "+gov.nist.secauto.metaschema.binding.annotations.XmlGroupAsBehavior.class.getName()+".");
				groupAs.append(xmlGroupAsBehavior.toString());
			}
			builder.annotation(GroupAs.class, groupAs.toString());
		}
	}

}
