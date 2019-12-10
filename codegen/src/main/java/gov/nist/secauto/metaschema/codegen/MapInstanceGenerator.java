package gov.nist.secauto.metaschema.codegen;

import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gov.nist.secauto.metaschema.codegen.builder.ClassBuilder;
import gov.nist.secauto.metaschema.codegen.builder.FieldBuilder;
import gov.nist.secauto.metaschema.codegen.builder.InnerClassBuilder;
import gov.nist.secauto.metaschema.codegen.builder.MethodBuilder;
import gov.nist.secauto.metaschema.codegen.item.InstanceItemContext;
import gov.nist.secauto.metaschema.codegen.type.JavaType;
import gov.nist.secauto.metaschema.codegen.type.MapJavaType;
import gov.nist.secauto.metaschema.model.FieldInstance;

public class MapInstanceGenerator extends AbstractCardinalityInstanceGenerator<MapJavaType> implements CardinalityInstanceGenerator {

	public MapInstanceGenerator(InstanceItemContext itemInstanceContext, AssemblyClassGenerator assemblyClassGenerator) {
		super(itemInstanceContext, assemblyClassGenerator, JavaType.createGenericMap(JavaType.create(String.class),itemInstanceContext.getJavaType()));
	}


	@Override
	protected void buildField(FieldBuilder builder) {
		super.buildField(builder);

		// --- JSON ---
		builder.annotation(JsonProperty.class, String.format("value = \"%s\", required = true", getInstanceName()));

		String deserializaerName = getPropertyName()+"Deserializer";
		InnerClassBuilder innerClass = builder.getClassBuilder().getActualClassBuilder().newInnerClassBuilder(deserializaerName);
		innerClass.extendsClass(String.format("com.fasterxml.jackson.databind.JsonDeserializer<%s>", getInstanceItemContext().getJavaType().getType()));
		MethodBuilder deserializeMethod = innerClass.newMethodBuilder("deserialize").returnType(getInstanceItemContext().getJavaType()).arguments("com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt");
		deserializeMethod.annotation(Override.class);
		deserializeMethod.throwsDeclaration(IOException.class).throwsDeclaration(JsonProcessingException.class);
		PrintWriter bodyWriter = deserializeMethod.getBodyWriter();
		bodyWriter.println("String id = ctxt.getParser().getCurrentName();");

		bodyWriter.format("%1$s item = p.readValueAs(%1$s.class);%n", getInstanceItemContext().getJavaType().getType());
		bodyWriter.format("item.set%s(id);%n", ClassUtils.toPropertyName(((FieldInstance)getModelInstance()).getDefinition().getJsonKeyFlagInstance().getName()));
		bodyWriter.println("return item;");

		builder.annotation(JsonDeserialize.class, String.format("contentUsing = %s.class", innerClass.getJavaType().getType(builder.getClassBuilder().getClashEvaluator())));

		// --- XML ---
		builder.annotation(XmlTransient.class);

		ClassBuilder classBuilder = builder.getClassBuilder().getActualClassBuilder();
		String tempFieldName = getVariableName() + "_elements";
		FieldBuilder tempField = classBuilder.newFieldBuilder(JavaType.createGenericList(getInstanceItemContext().getJavaType()),
				tempFieldName);
		tempField.annotation(JsonIgnore.class);
		tempField.annotation(XmlElement.class, String.format("name = \"%s\", namespace = \"%s\", required = true",
				getModelInstance().getDefinition().getName(), getInstanceItemContext().getXmlNamespace()));

		MethodBuilder afterUnmarshal = classBuilder.getActualClassBuilder().getAfterUnmarshalMethod();
		{
			PrintWriter writer = afterUnmarshal.getBodyWriter();
			writer.format("%s = gov.nist.secauto.metaschema.datatype.jaxb.JaxBUtil.listToMap(%s, (v) -> v.get%s());%n", getVariableName(), tempFieldName, ClassUtils.toPropertyName(((FieldInstance)getModelInstance()).getDefinition().getJsonKeyFlagInstance().getName()));
		}

		MethodBuilder beforeMarshal = classBuilder.getActualClassBuilder().getBeforeMarshalMethod();
		{
			PrintWriter writer = beforeMarshal.getBodyWriter();
			writer.format("%s = gov.nist.secauto.metaschema.datatype.jaxb.JaxBUtil.mapToList(%s);", tempFieldName, getVariableName());
		}
		
	}

}
