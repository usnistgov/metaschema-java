package gov.nist.secauto.metaschema.codegen;

import java.io.PrintWriter;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.w3c.dom.Element;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import gov.nist.secauto.metaschema.codegen.builder.ClassBuilder;
import gov.nist.secauto.metaschema.codegen.builder.FieldBuilder;
import gov.nist.secauto.metaschema.codegen.builder.MethodBuilder;
import gov.nist.secauto.metaschema.codegen.item.DataTypeInstanceItemContext;
import gov.nist.secauto.metaschema.codegen.item.InstanceItemContext;
import gov.nist.secauto.metaschema.codegen.support.JsonSerializationSupport;
import gov.nist.secauto.metaschema.codegen.type.DataType;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.datatype.jaxb.DomUtil;
import gov.nist.secauto.metaschema.model.FieldInstance;

public class SingletonInstanceGenerator extends AbstractCardinalityInstanceGenerator<JavaType>
		implements CardinalityInstanceGenerator {

	public SingletonInstanceGenerator(InstanceItemContext instanceItemContext,
			AssemblyClassGenerator assemblyClassGenerator) {
		super(instanceItemContext, assemblyClassGenerator, instanceItemContext.getJavaType());
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

		// --- XML ---
		boolean simpleElement = true;
		InstanceItemContext instanceItemContext = getInstanceItemContext();
		if (instanceItemContext instanceof DataTypeInstanceItemContext) {
			DataTypeInstanceItemContext dataTypeInstanceItemContext = (DataTypeInstanceItemContext) instanceItemContext;
			FieldInstance fieldInstance = dataTypeInstanceItemContext.getModelInstance();
			if (!fieldInstance.hasXmlWrapper()) {
				if (DataType.MARKUP_MULTILINE.equals(dataTypeInstanceItemContext.getDataType())) {
					// this is going to require a work-around
					builder.annotation(XmlTransient.class);

					ClassBuilder classBuilder = builder.getClassBuilder();
					String tempFieldName = getInstanceName() + "_elements";
					FieldBuilder tempField = classBuilder.newFieldBuilder(JavaType.createGenericList(Element.class),
							tempFieldName);
					tempField.annotation(JsonIgnore.class);
					tempField.annotation(XmlAnyElement.class);

					MethodBuilder afterUnmarshal = classBuilder.getAfterUnmarshalMethod();
					{
						PrintWriter writer = afterUnmarshal.getBodyWriter();
						writer.format("%s = DomUtil.unmarshalToMarkupString(%s);", getInstanceName(), tempFieldName);
						afterUnmarshal.importEntry(DomUtil.class);
					}

					MethodBuilder beforeMarshal = classBuilder.getBeforeMarshalMethod();
					{
						PrintWriter writer = beforeMarshal.getBodyWriter();
						writer.format("%s = DomUtil.marshalFromMarkupString(%s);", tempFieldName, getInstanceName());
						afterUnmarshal.importEntry(DomUtil.class);
					}

					simpleElement = false;
				} else {
					// throw error, since this shouldn't happen
				}
			}
		}
		if (simpleElement) {
			builder.annotation(XmlElement.class, String.format("name = \"%s\", namespace = \"%s\", required = true",
					getInstanceName(), getInstanceItemContext().getXmlNamespace()));
		}

	}

}
