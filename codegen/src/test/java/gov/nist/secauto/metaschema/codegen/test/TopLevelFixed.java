package gov.nist.secauto.metaschema.codegen.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.persistence.oxm.annotations.XmlReadTransformer;
import org.w3c.dom.Element;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.nist.csrc.ns.metaschema.testing.field.with.flags.TopLevel;
import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.datatype.jackson.JacksonUtil;
import gov.nist.secauto.metaschema.datatype.jackson.MarkupStringDeserializer;
import gov.nist.secauto.metaschema.datatype.jackson.MarkupStringSerializer;
import gov.nist.secauto.metaschema.datatype.jaxb.DomUtil;
import gov.nist.secauto.metaschema.datatype.jaxb.JaxBUtil;
import gov.nist.secauto.metaschema.datatype.jaxb.MarkupStringReadTransformer;

@JsonRootName(value = "top-level", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple")
@JsonIgnoreProperties({ "$schema" })
@XmlRootElement(name = "top-level", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple")
@XmlAccessorType(XmlAccessType.FIELD)
public class TopLevelFixed {
	public static void main(String[] args) throws JAXBException, JsonParseException, IOException {
		Class<TopLevelFixed> clazz = TopLevelFixed.class;
		TopLevelFixed root;
		{
			File jsonFile = new File("src/test/resources/metaschema/field_with_flags/example.json");
			try (FileReader reader = new FileReader(jsonFile)) {
				root = JacksonUtil.parse(reader, clazz);
			}
			System.out.println(String.format("JSON: %s", root.toString()));
		}
		{
			File xmlFile = new File("src/test/resources/metaschema/simple_with_field/example.xml");
			try (FileReader reader = new FileReader(xmlFile)) {
				root = JaxBUtil.parse(reader, clazz);
			}
			System.out.println(String.format("XML: O%s", root.toString()));
		}
	}

	/**
	 * <p>
	 * The document identifier
	 * </p>
	 * 
	 */
	@JsonProperty(value = "id")
	@XmlAttribute
	private String id;
	/**
	 * <p>
	 * A field with no flags.
	 * </p>
	 * 
	 */
	@JsonProperty(value = "field", required = true)
	@XmlElement(name = "field", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple", required = true)
	private String field;
	/**
	 * <p>
	 * A field with no flags.
	 * </p>
	 * 
	 */
	@JsonSerialize(using = MarkupStringSerializer.class)
	@JsonDeserialize(using = MarkupStringDeserializer.class)
	@JsonProperty(value = "field2", required = true)
//	@XmlValue
//	@XmlElement(name = "p", namespace="http://csrc.nist.gov/ns/metaschema/testing/simple", required = true)
	@XmlAnyElement
	private List<Element> field2_Elements;

	@XmlTransient
	private MarkupString field2;

	@SuppressWarnings("unused") // called only by JAXB
	private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		field2 = DomUtil.unmarshalToMarkupString(field2_Elements);
	}

	@SuppressWarnings("unused") // called only by JAXB
	private void beforeMarshal(Marshaller marshaller) {
		field2_Elements = DomUtil.marshalFromMarkupString(field2);
	}

	/**
	 * <p>
	 * A field with no flags.
	 * </p>
	 * 
	 */
	@JsonProperty(value = "field3", required = true)
	@XmlElement(name = "field3", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple", required = true)
	@XmlReadTransformer(transformerClass = MarkupStringReadTransformer.class)
	//	@XmlJavaTypeAdapter(MyAdapter.class)
	private MarkupString field3;

	public TopLevelFixed() {
	}

	public String getId() {
		return id;
	}

	public void setId(String value) {
		this.id = value;
	}

	public String getField() {
		return field;
	}

	public void setField(String value) {
		this.field = value;
	}

	public MarkupString getField2() {
		return field2;
	}

	public void setField2(MarkupString value) {
		this.field2 = value;
	}

	public MarkupString getField3() {
		return field3;
	}

	public void setField3(MarkupString value) {
		this.field3 = value;
	}

	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).toString();
	}

	private static class MyAdapter extends XmlAdapter<Element, MarkupString> {

		@Override
		public MarkupString unmarshal(Element v) throws Exception {
			StringWriter writer = new StringWriter();
			try {
				Transformer trans = TransformerFactory.newInstance().newTransformer();
				trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				trans.transform(new DOMSource(v), new StreamResult(writer));
			} catch (final TransformerConfigurationException ex) {
				throw new IllegalStateException(ex);
			} catch (final TransformerException ex) {
				throw new IllegalArgumentException(ex);
			}
			return MarkupString.fromHTML(writer.toString());
		}

		@Override
		public Element marshal(MarkupString v) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private static class SpecialString {
		private String inner;

		public SpecialString(String textContent) {
			this.inner = textContent;
		}

		@Override
		public String toString() {
			return inner;
		}

	}
}
