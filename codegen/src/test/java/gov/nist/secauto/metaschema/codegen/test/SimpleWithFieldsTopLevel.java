package gov.nist.secauto.metaschema.codegen.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
/*
@javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
@com.fasterxml.jackson.annotation.JsonRootName(value="top-level", namespace="http://csrc.nist.gov/ns/metaschema/testing/simple/with/field")
@javax.xml.bind.annotation.XmlRootElement(name="top-level", namespace="http://csrc.nist.gov/ns/metaschema/testing/simple/with/field")
public class SimpleWithFieldsTopLevel {
	public static void main(String[] args) throws JsonParseException, FileNotFoundException, IOException, JAXBException {
//		{
//			SimpleWithFieldsTopLevel root = JacksonUtil.parseJson(new FileReader(new File("src/test/resources/metaschema/simple_with_field/example.json")), SimpleWithFieldsTopLevel.class);
//			System.out.println(String.format("JSON: %s", root.toString()));
//		}
		{
			SimpleWithFieldsTopLevel root = JacksonUtil.parseXml(new FileReader(new File("src/test/resources/metaschema/simple_with_field/example.xml")), SimpleWithFieldsTopLevel.class);
			System.out.println(String.format("XML: %s", root.toString()));
		}
	}

    @com.fasterxml.jackson.annotation.JsonProperty(value = "id")
    @javax.xml.bind.annotation.XmlAttribute(name = "id")
    private java.lang.String id;

    @com.fasterxml.jackson.annotation.JsonProperty(value = "field", required = true)
    @javax.xml.bind.annotation.XmlElement(name = "field", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple/with/field", required = true)
    private java.lang.String field;

    @com.fasterxml.jackson.annotation.JsonProperty(value = "field2", required = true)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = gov.nist.secauto.metaschema.datatype.jackson.MarkupStringSerializer.class)
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = gov.nist.secauto.metaschema.datatype.jackson.MarkupStringDeserializer.class)
    @javax.xml.bind.annotation.XmlElement(name = "field2", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple/with/field", required = true)
    @XmlJavaTypeAdapter(MarkupStringXmlAdapter.class)
    private gov.nist.secauto.metaschema.markup.MarkupString field2;

    @com.fasterxml.jackson.annotation.JsonProperty(value = "field3", required = true)
    @javax.xml.bind.annotation.XmlElement(name = "field3", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple/with/field", required = true)
    private java.lang.String field3;

    @com.fasterxml.jackson.annotation.JsonProperty(value = "field4s", required = true)
    @com.fasterxml.jackson.databind.annotation.JsonSerialize(contentUsing = gov.nist.secauto.metaschema.datatype.jackson.MarkupStringSerializer.class)
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize(contentUsing = gov.nist.secauto.metaschema.datatype.jackson.MarkupStringDeserializer.class)
    @com.fasterxml.jackson.annotation.JsonFormat(with = {com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED})
//    @javax.xml.bind.annotation.XmlElement(name = "field4s", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple/with/field", required = true)
//    @javax.xml.bind.annotation.XmlElement(name = "field4", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple/with/field", required = true)
//    @javax.xml.bind.annotation.XmlElementWrapper(name = "field4s", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple/with/field", required = true)
//    @XmlJavaTypeAdapter(MarkupStringListXmlAdapter.class)
//    @XmlJavaTypeAdapter(value = MarkupStringObjectXmlAdapter.class)
//    @XmlReadTransformer(transformerClass = MarkupStringReadTransformer.class)
    @JacksonXmlElementWrapper(localName = "field4s", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple/with/field")
    @JacksonXmlProperty(localName = "field4", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple/with/field")
    private java.util.List<gov.nist.secauto.metaschema.markup.MarkupString> field4S;

    @com.fasterxml.jackson.annotation.JsonProperty(value = "field5s", required = true)
    @com.fasterxml.jackson.annotation.JsonFormat(with = {com.fasterxml.jackson.annotation.JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, com.fasterxml.jackson.annotation.JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED})
    @javax.xml.bind.annotation.XmlElement(name = "field5", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple/with/field", required = false)
    @JacksonXmlProperty(localName = "field5", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple/with/field")
    @JacksonXmlElementWrapper(useWrapping = false)
    private java.util.List<java.lang.String> field5S;

    public SimpleWithFieldsTopLevel() {
    }

    public java.lang.String getId() {
        return id;
    }

    public void setId(java.lang.String value) {
        this.id = value;
    }

    public java.lang.String getField() {
        return field;
    }

    public void setField(java.lang.String value) {
        this.field = value;
    }

    public gov.nist.secauto.metaschema.markup.MarkupString getField2() {
        return field2;
    }

    public void setField2(gov.nist.secauto.metaschema.markup.MarkupString value) {
        this.field2 = value;
    }

    public java.lang.String getField3() {
        return field3;
    }

    public void setField3(java.lang.String value) {
        this.field3 = value;
    }

    public java.util.List<gov.nist.secauto.metaschema.markup.MarkupString> getField4S() {
        return field4S;
    }

    public void setField4S(java.util.List<gov.nist.secauto.metaschema.markup.MarkupString> value) {
        this.field4S = value;
    }

    public java.util.List<java.lang.String> getField5S() {
        return field5S;
    }

    public void setField5S(java.util.List<java.lang.String> value) {
        this.field5S = value;
    }

    public java.lang.String toString() {
        return new org.apache.commons.lang3.builder.ReflectionToStringBuilder(this, org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE).toString();
    }

}
*/