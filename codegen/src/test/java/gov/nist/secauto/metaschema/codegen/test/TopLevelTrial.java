package gov.nist.secauto.metaschema.codegen.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.w3c.dom.Element;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.nist.secauto.metaschema.datatype.MarkupString;
import gov.nist.secauto.metaschema.datatype.jackson.MarkupStringDeserializer;
import gov.nist.secauto.metaschema.datatype.jackson.MarkupStringSerializer;

@JsonRootName(value = "top-level", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple")
@JsonIgnoreProperties({ "$schema" })
@XmlRootElement(name = "top-level", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple")
@XmlAccessorType(XmlAccessType.FIELD)
public class TopLevelTrial {
	public static void main(String[] args) throws JAXBException, FileNotFoundException {
		JAXBContext jaxbContext = org.eclipse.persistence.jaxb.JAXBContext.newInstance(TopLevelTrial.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		TopLevelTrial result = (TopLevelTrial) jaxbUnmarshaller
				.unmarshal(new FileInputStream(new File("src/test/resources/metaschema/simple_with_field/example.xml").getAbsoluteFile()));
		System.out.println(String.format("XML: Object: %s", result.toString()));
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
//	@XmlElement(name = "p", namespace="http://csrc.nist.gov/ns/metaschema/testing/simple", required = true)
	@XmlJavaTypeAdapter(FieldAdapter.class)
//	@XmlAnyElement(lax=true, value=MyDomHandler.class)
	@XmlAnyElement(lax=true)
	private MarkupString field2;
//
//	@SuppressWarnings("unused") // called only by JAXB
//	private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
//		StringWriter writer = new StringWriter();
//		try {
//			Transformer trans = TransformerFactory.newInstance().newTransformer();
//			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//			for (Element element : field2_Elements) {
//				trans.transform(new DOMSource(element), new StreamResult(writer));
//			}
//		} catch (final TransformerConfigurationException ex) {
//			throw new IllegalStateException(ex);
//		} catch (final TransformerException ex) {
//			throw new IllegalArgumentException(ex);
//		}
//		field2 = MarkupString.fromHTML(writer.toString());
//	}

	/**
	 * <p>
	 * A field with no flags.
	 * </p>
	 * 
	 */
	@JsonProperty(value = "field3", required = true)
	@XmlElement(name = "field3", namespace = "http://csrc.nist.gov/ns/metaschema/testing/simple", required = true)
	private String field3;

	public TopLevelTrial() {
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

	public String getField3() {
		return field3;
	}

	public void setField3(String value) {
		this.field3 = value;
	}

	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).toString();
	}

	private static class FieldAdapter extends XmlAdapter<List<Element>, MarkupString> {
		@Override
		public MarkupString unmarshal(List<Element> v) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Element> marshal(MarkupString v) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private static class MyDomHandler implements DomHandler<MarkupString, StreamResult> {
		private StringWriter xmlWriter = new StringWriter();

		@Override
		public StreamResult createUnmarshaller(ValidationEventHandler errorHandler) {
			return new StreamResult(xmlWriter);
		}

		@Override
		public MarkupString getElement(StreamResult rt) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Source marshal(MarkupString n, ValidationEventHandler errorHandler) {
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
