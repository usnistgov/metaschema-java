package gov.nist.secauto.metaschema.datatype.parser.xml;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import org.codehaus.stax2.XMLEventReader2;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.auto.Auto;
import org.jmock.auto.Mock;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import gov.nist.secauto.metaschema.datatype.parser.BindingException;

class XmlFieldParsePlanTest {
	private static final String NS = "namespace";
	private static final QName OBJECT_QNAME = new QName(NS, "object");
	private static final String ATTRIBUTE_A_LOCAL_NAME = "a";
	private static final String ATTRIBUTE_B_LOCAL_NAME = "b";

	private static final String OBJECT_START_ELEMENT = OBJECT_QNAME.getLocalPart()+"-start-element";
	private static final String CHARACTERS = "characters";
	private static final String OBJECT_END_ELEMENT = OBJECT_QNAME.getLocalPart()+"-end-element";
	private static final String END_DOCUMENT = "end-document";
//	private static final String ATTRIBUTE_A = "attribute-a";
//	private static final String ATTRIBUTE_B = "attribute-b";

	@Mock
	private StartElement OBJECT_START_ELEMENT_EVENT;
	@Mock
	private EndElement OBJECT_END_ELEMENT_EVENT;
	@Mock
	private Characters CHARACTERS_EVENT;
	@Mock
	private EndDocument END_DOCUMENT_EVENT;

	@RegisterExtension
	JUnit5Mockery context = new JUnit5Mockery();
	@Auto
	private States readerState;

	@Mock
	private XmlParser xmlParser;
	@Mock
	private XMLEventReader2 reader;

	@Mock
	FieldValueXmlPropertyParser fieldValueXmlPropertyParser;

	@Test
	void testStringValueWithAttributes() throws BindingException, XMLStreamException {

		Attribute attributeA = context.mock(Attribute.class, "attributeA");
		Attribute attributeB = context.mock(Attribute.class, "attributeB");
		List<Attribute> attributes = new LinkedList<>();
		attributes.add(attributeA);
		attributes.add(attributeB);

		XmlAttributePropertyParser attributeParserA = context.mock(XmlAttributePropertyParser.class,
				"XmlAttributePropertyParserA");
		XmlAttributePropertyParser attributeParserB = context.mock(XmlAttributePropertyParser.class,
				"XmlAttributePropertyParserB");
		
		readerState.startsAs(OBJECT_START_ELEMENT);
		Sequence parseStream = context.sequence("parseStream");

		context.checking(new Expectations() {
			{
				MockXmlSupport.mockEndDocument(this, END_DOCUMENT_EVENT);
				MockXmlSupport.mockElementXMLEvent(this, OBJECT_START_ELEMENT_EVENT, OBJECT_END_ELEMENT_EVENT,
						OBJECT_QNAME);

				MockXmlSupport.mockAttributeXMLEvent(this, attributeA, new QName(ATTRIBUTE_A_LOCAL_NAME), CHARACTERS);
				MockXmlSupport.mockAttributeXMLEvent(this, attributeB, new QName(ATTRIBUTE_B_LOCAL_NAME), CHARACTERS);
				MockXmlSupport.mockCharactersXMLEvent(this, CHARACTERS_EVENT, CHARACTERS);

				oneOf(OBJECT_START_ELEMENT_EVENT).getAttributes();
				will(returnValue(attributes.iterator()));

				// setup reader peeking behavior based on states
				allowing(reader).peek();
				will(returnValue(OBJECT_START_ELEMENT_EVENT));
				when(readerState.is(OBJECT_START_ELEMENT));
				allowing(reader).peek();
				will(returnValue(CHARACTERS_EVENT));
				when(readerState.is(CHARACTERS));
				allowing(reader).peek();
				will(returnValue(OBJECT_END_ELEMENT_EVENT));
				when(readerState.is(OBJECT_END_ELEMENT));
				allowing(reader).peek();
				will(returnValue(END_DOCUMENT_EVENT));
				when(readerState.is(END_DOCUMENT));

				/*
				 * the parsing sequence
				 */
//				// parsing object START_ELEMENT
				oneOf(reader).nextEvent();
				will(returnValue(OBJECT_START_ELEMENT_EVENT));
				then(readerState.is(CHARACTERS));
				inSequence(parseStream);

				// parse the attributes
				oneOf(attributeParserA).parse(with(any(Value.class)), with(same(attributeA)));
				inSequence(parseStream);
				oneOf(attributeParserB).parse(with(any(Value.class)), with(same(attributeB)));
				inSequence(parseStream);

				// parse the field value
				oneOf(fieldValueXmlPropertyParser).parse(with(any(Value.class)), with(same(reader)));
				inSequence(parseStream);
				// this simulates parsing
				then(readerState.is(OBJECT_END_ELEMENT));

				// advance past the object's END_ELEMENT
				oneOf(reader).nextEvent();
				will(returnValue(END_DOCUMENT_EVENT));
				then(readerState.is(END_DOCUMENT));
				inSequence(parseStream);
			}
		});

		Map<QName, XmlAttributePropertyParser> attributeParsers = new LinkedHashMap<>();
		attributeParsers.put(attributeA.getName(), attributeParserA);
		attributeParsers.put(attributeB.getName(), attributeParserB);

		FieldXmlParsePlan<Value> parsePlan = new FieldXmlParsePlan<Value>(xmlParser, Value.class,
				attributeParsers, fieldValueXmlPropertyParser);

		parsePlan.parse(reader);

		context.assertIsSatisfied();
	}

	public static class Value {
		public Value() {

		}
	}
}
