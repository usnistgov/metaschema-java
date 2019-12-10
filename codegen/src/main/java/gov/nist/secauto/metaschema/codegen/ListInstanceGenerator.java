package gov.nist.secauto.metaschema.codegen;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import gov.nist.secauto.metaschema.codegen.builder.FieldBuilder;
import gov.nist.secauto.metaschema.codegen.item.InstanceItemContext;
import gov.nist.secauto.metaschema.codegen.support.JsonSerializationSupport;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.codegen.type.ListJavaType;

public class ListInstanceGenerator extends AbstractCardinalityInstanceGenerator<ListJavaType>
		implements CardinalityInstanceGenerator {
	public ListInstanceGenerator(InstanceItemContext itemInstanceContext,
			AssemblyClassGenerator assemblyClassGenerator) {
		super(itemInstanceContext, assemblyClassGenerator,
				JavaType.createGenericList(itemInstanceContext.getJavaType()));
	}

	@Override
	protected void buildField(FieldBuilder builder) {
		super.buildField(builder);

		// --- JSON ---
		builder.annotation(JsonProperty.class, String.format("value = \"%s\", required = true", getInstanceName()));
		JsonSerializationSupport.generateJsonSerializerAnnotations(builder,
				getInstanceItemContext().getJsonSerializerClass());
		JsonSerializationSupport.generateJsonDeserializerAnnotations(builder,
				getInstanceItemContext().getJsonDeserializerClass());

		switch (getModelInstance().getGroupBehaviorJson()) {
		case LIST:
			// do nothing
			break;
		case SINGLETON_OR_LIST:
			builder.annotation(JsonFormat.class, "with = {com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED}");
			builder.importEntry(JsonFormat.class);
			break;
		case KEYED:
			// a map will be used in this case, so this generator will never encounter this situation
			throw new UnsupportedOperationException();
		}

		// --- XML ---
		if (getInstanceItemContext().getModelInstance().isGroupBehaviorXmlGrouped()) {
			builder.annotation(XmlElement.class, String.format("name = \"%s\", namespace = \"%s\", required = true",
					getModelInstance().getDefinition().getName(), getInstanceItemContext().getXmlNamespace()));

			builder.annotation(XmlElementWrapper.class,
					String.format("name = \"%s\", namespace = \"%s\", required = %s", getInstanceName(),
							getInstanceItemContext().getXmlNamespace(), getModelInstance().getMinOccurs() >= 1));
		} else {
			builder.annotation(XmlElement.class,
					String.format("name = \"%s\", namespace = \"%s\", required = %s",
							getModelInstance().getDefinition().getName(), getInstanceItemContext().getXmlNamespace(),
							getModelInstance().getMinOccurs() >= 1));
		}
	}
}
