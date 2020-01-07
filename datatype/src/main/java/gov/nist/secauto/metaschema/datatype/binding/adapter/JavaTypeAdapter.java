package gov.nist.secauto.metaschema.datatype.binding.adapter;

import javax.xml.namespace.QName;
import javax.xml.stream.events.XMLEvent;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;
import gov.nist.secauto.metaschema.datatype.parser.xml.XmlParsingContext;

public interface JavaTypeAdapter<TYPE> {
	boolean isParsingStartElement();

//	boolean isParsingEndElement();
	boolean canHandleQName(QName nextQName);

	TYPE parseValue(String value) throws BindingException;

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
	TYPE parseType(XmlParsingContext parsingContext) throws BindingException;
}
