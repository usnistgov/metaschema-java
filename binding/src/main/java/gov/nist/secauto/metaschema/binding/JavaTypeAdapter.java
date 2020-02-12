package gov.nist.secauto.metaschema.binding;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.NamedPropertyBindingFilter;

public interface JavaTypeAdapter<TYPE> {
	boolean isParsingStartElement();

//	boolean isParsingEndElement();
	boolean canHandleQName(QName nextQName);

	TYPE parse(String value) throws BindingException;

	/**
	 * This method is expected to parse content starting at the next event. Parsing
	 * will continue until the next event represents content that is not handled by
	 * this method.
	 * <p>
	 * If {@link #isParsingStartElement() is {@code true}, then first event to parse
	 * will be the {@link XMLEvent#START_ELEMENT} for the containing element.
	 * Otherwise, the first event to parse will be the first child of that
	 * {@link XMLEvent#START_ELEMENT}.
	 * <p>
	 * A JavaTypeAdapter is expected to parse until the peeked event is content that
	 * is not handled by this method. This also means that if
	 * {@link #isParsingStartElement() is {@code true}, then this method is expected
	 * to parse the END_ELEMENT event as well.
	 * 
	 * @param parsingContext the XML parser and binding info
	 * @return the parsed value
	 * @throws BindingException if a parsing error occurs
	 */
	TYPE parse(XmlParsingContext parsingContext) throws BindingException;
	TYPE parse(JsonParsingContext parsingContext) throws BindingException;

	void writeXmlElement(Object value, QName valueQName, StartElement parent, XmlWritingContext writingContext)
			throws BindingException;

	void writeJsonFieldValue(Object value, NamedPropertyBindingFilter filter, JsonWritingContext writingContext)
			throws BindingException;

	/**
	 * Gets the default value to use as the JSON (or YAML) field name.
	 * 
	 * @return the default field name
	 */
	String getDefaultJsonFieldName();

	/**
	 * Determines if the data type's value is allowed to be unwrapped in XML when
	 * the value is a field value.
	 * 
	 * @return {@code true} if allowed, or {@code false} otherwise.
	 */
	boolean isUnrappedValueAllowedInXml();
}
