package gov.nist.secauto.metaschema.codegen.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField4;
import gov.nist.secauto.metaschema.datatype.jaxb.JaxBUtil;

@javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
@com.fasterxml.jackson.annotation.JsonRootName(value = "top-level", namespace = "http://csrc.nist.gov/ns/metaschema/testing/field/with/flags")
@javax.xml.bind.annotation.XmlRootElement(name = "top-level", namespace = "http://csrc.nist.gov/ns/metaschema/testing/field/with/flags")
public class TopLevel {
	public static void main(String[] args) throws JAXBException, JsonParseException, JsonMappingException, IOException {
		Class<TopLevel> clazz = TopLevel.class;
		TopLevel root;
//		{
//			File jsonFile = new File("src/test/resources/metaschema/field_with_flags/example.json");
//			try (FileReader reader = new FileReader(jsonFile)) {
//				root = JacksonUtil.parse(reader, clazz);
//			}
//			System.out.println(String.format("JSON: %s", root.toString()));
//		}
		{
			File xmlFile = new File("src/test/resources/metaschema/field_with_flags/example.xml");
			try (FileReader reader = new FileReader(xmlFile)) {
				root = JaxBUtil.parse(reader, clazz);
			}
			System.out.println(String.format("XML: O%s", root.toString()));
		}
	}

	@com.fasterxml.jackson.annotation.JsonProperty(value = "id")
	@javax.xml.bind.annotation.XmlAttribute(name = "id")
	private java.lang.String id;

	@com.fasterxml.jackson.annotation.JsonProperty(value = "complex-field1", required = true)
	@javax.xml.bind.annotation.XmlElement(name = "complex-field1", namespace = "http://csrc.nist.gov/ns/metaschema/testing/field/with/flags", required = true)
	private gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField1 complexField1;

	@com.fasterxml.jackson.annotation.JsonProperty(value = "complex-fields2", required = true)
	@com.fasterxml.jackson.annotation.JsonFormat(with = {
			com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
			com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED })
	@javax.xml.bind.annotation.XmlElement(name = "complex-field2", namespace = "http://csrc.nist.gov/ns/metaschema/testing/field/with/flags", required = true)
	private java.util.List<gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField2> complexFields2;

	@com.fasterxml.jackson.annotation.JsonProperty(value = "complex-fields3", required = true)
	@javax.xml.bind.annotation.XmlElement(name = "complex-field3", namespace = "http://csrc.nist.gov/ns/metaschema/testing/field/with/flags", required = true)
	@javax.xml.bind.annotation.XmlElementWrapper(name = "complex-fields3", namespace = "http://csrc.nist.gov/ns/metaschema/testing/field/with/flags", required = true)
	private java.util.List<gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField3> complexFields3;

	@com.fasterxml.jackson.annotation.JsonProperty(value = "complex-fields4", required = true)
	@com.fasterxml.jackson.databind.annotation.JsonDeserialize(contentUsing = TopLevel.ComplexFields4Deserializer.class)
	@XmlTransient
	private java.util.LinkedHashMap<java.lang.String, gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField4> complexFields4;

	@JsonIgnore
	@javax.xml.bind.annotation.XmlElement(name = "complex-field4", namespace = "http://csrc.nist.gov/ns/metaschema/testing/field/with/flags", required = true)
	private List<ComplexField4> complexField4_elements;
	
	@SuppressWarnings("unused")
	private void afterUnmarshal(Unmarshaller u, Object parent) {
		complexFields4 = JaxBUtil.listToMap(complexField4_elements, (v) -> v.getId2());
	}
	
	@SuppressWarnings("unused")
	private void beforeMarshal(Marshaller u) {
		complexField4_elements = JaxBUtil.mapToList(complexFields4);
	}

	public TopLevel() {
	}

	public java.lang.String getId() {
		return id;
	}

	public void setId(java.lang.String value) {
		this.id = value;
	}

	public gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField1 getComplexField1() {
		return complexField1;
	}

	public void setComplexField1(gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField1 value) {
		this.complexField1 = value;
	}

	public java.util.List<gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField2> getComplexFields2() {
		return complexFields2;
	}

	public void setComplexFields2(
			java.util.List<gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField2> value) {
		this.complexFields2 = value;
	}

	public java.util.List<gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField3> getComplexFields3() {
		return complexFields3;
	}

	public void setComplexFields3(
			java.util.List<gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField3> value) {
		this.complexFields3 = value;
	}

	public java.util.LinkedHashMap<java.lang.String, gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField4> getComplexFields4() {
		return complexFields4;
	}

	public void setComplexFields4(
			java.util.LinkedHashMap<java.lang.String, gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField4> value) {
		this.complexFields4 = value;
	}

	public java.lang.String toString() {
		return new org.apache.commons.lang3.builder.ReflectionToStringBuilder(this,
				org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE).toString();
	}

	private static class ComplexFields4Deserializer extends
			com.fasterxml.jackson.databind.JsonDeserializer<gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField4> {
		@java.lang.Override
		public gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField4 deserialize(
				com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt)
				throws java.io.IOException, com.fasterxml.jackson.core.JsonProcessingException {
			String id = ctxt.getParser().getCurrentName();
			gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField4 item = p
					.readValueAs(gov.nist.csrc.ns.metaschema.testing.field.with.flags.ComplexField4.class);
			item.setId2(id);
			return item;
		}

	}
}
