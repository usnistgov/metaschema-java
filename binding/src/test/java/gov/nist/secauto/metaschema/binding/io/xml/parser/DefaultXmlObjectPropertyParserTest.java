package gov.nist.secauto.metaschema.binding.io.xml.parser;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
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

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.io.xml.parser.DefaultXmlObjectPropertyParser;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.property.CollectionPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.FieldPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyCollector;
import gov.nist.secauto.metaschema.datatype.markup.MarkupMultiline;

class DefaultXmlObjectPropertyParserTest {
	private static final String NS = "namespace";
	private static final String WRAPPER_LOCAL_NAME = "wrapper";
	private static final QName WRAPPER_QNAME = new QName(NS, WRAPPER_LOCAL_NAME);
	private static final String OBJECT_LOCAL_NAME = "element";
	private static final QName OBJECT_QNAME = new QName(NS, OBJECT_LOCAL_NAME);

	private static final String WRAPPER_START_ELEMENT = "wrapper-start-element";
	private static final String OBJECT_START_ELEMENT = "object-start-element";
	private static final String CHARACTERS = "characters";
	private static final String OBJECT_END_ELEMENT = "object-end-element";
	private static final String WRAPPER_END_ELEMENT = "wrapper-end-element";

	@RegisterExtension
	JUnit5Mockery context = new JUnit5Mockery();

	@Auto
	private States readerState;

	@Mock
	private StartElement WRAPPER_START_ELEMENT_EVENT;
	@Mock
	private EndElement WRAPPER_END_ELEMENT_EVENT;
	@Mock
	private StartElement OBJECT_START_ELEMENT_EVENT;
	@Mock
	private EndElement OBJECT_END_ELEMENT_EVENT;
	@Mock
	private Characters CHARACTERS_EVENT;

	@Mock
	private BindingContext bindingContext;
	@Mock
	private CollectionPropertyInfo propertyInfo;
	@Mock
	private FieldPropertyBinding propertyBinding;
	@Mock
	private PropertyCollector propertyCollector;
	@Mock
	private XmlParsingContext parsingContext;
	@Mock
	private JavaTypeAdapter<String> typeAdapter;
//	@Mock
//	private ParsePlan<XMLEventReader2, Value> plan;
	@Mock
	private XMLEventReader2 reader;
	@Mock
	private Value value;

	@Test
	void testAssemblyGroupedStrings() throws BindingException, XMLStreamException {
		readerState.startsAs(WRAPPER_START_ELEMENT);
		Sequence parseStream = context.sequence("parseStream");

		context.checking(new Expectations() {
			{
				MockXmlSupport.mockElementXMLEvent(this, WRAPPER_START_ELEMENT_EVENT, WRAPPER_END_ELEMENT_EVENT,
						new QName(NS, WRAPPER_LOCAL_NAME));
				MockXmlSupport.mockElementXMLEvent(this, OBJECT_START_ELEMENT_EVENT, OBJECT_END_ELEMENT_EVENT,
						new QName(NS, OBJECT_LOCAL_NAME));
				MockXmlSupport.mockCharactersXMLEvent(this, CHARACTERS_EVENT, CHARACTERS);

				// setup PropertyInfo behavior
				allowing(propertyInfo).getXmlGroupAsBehavior();
				will(returnValue(XmlGroupAsBehavior.GROUPED));
				allowing(propertyInfo).getGroupXmlQName();
				will(returnValue(WRAPPER_QNAME));
				allowing(propertyInfo).getItemType();
				will(returnValue(String.class));

				// setup PropertyBinding behavior
				allowing(propertyBinding).getPropertyInfo();
				will(returnValue(propertyInfo));
				allowing(propertyBinding).isWrappedInXml();
				will(returnValue(true));
				allowing(propertyBinding).getXmlQName();
				will(returnValue(OBJECT_QNAME));

				// setup XmlParser behavior
				oneOf(bindingContext).getJavaTypeAdapter(String.class);
				will(returnValue(typeAdapter));
				allowing(parsingContext).getEventReader();
				will(returnValue(reader));
				allowing(typeAdapter).isParsingStartElement();
				will(returnValue(false));

				// setup reader peeking behavior based on states
				allowing(reader).peek();
				will(returnValue(WRAPPER_START_ELEMENT_EVENT));
				when(readerState.is(WRAPPER_START_ELEMENT));
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
				will(returnValue(WRAPPER_END_ELEMENT_EVENT));
				when(readerState.is(WRAPPER_END_ELEMENT));

				/* 
				 * the parsing sequence
				 */
				oneOf(propertyInfo).newPropertyCollector();
				will(returnValue(propertyCollector));
				inSequence(parseStream);

				// parse wrapper START_ELEMENT
				oneOf(reader).nextEvent();
				will(returnValue(WRAPPER_START_ELEMENT_EVENT));
				then(readerState.is(OBJECT_START_ELEMENT));
				inSequence(parseStream);

				// parsing object START_ELEMENT
				oneOf(reader).nextEvent();
				will(returnValue(OBJECT_START_ELEMENT_EVENT));
				then(readerState.is(CHARACTERS));
				inSequence(parseStream);

				// parse character contents
				// transition to end element state to mimick parsing
				oneOf(typeAdapter).parse(with(same(parsingContext)));
				will(returnValue(CHARACTERS));
				then(readerState.is(OBJECT_END_ELEMENT));
				inSequence(parseStream);
				oneOf(propertyCollector).add(CHARACTERS);
				inSequence(parseStream);

				// parse object END_ELEMENT
				oneOf(reader).nextEvent();
				will(returnValue(OBJECT_END_ELEMENT_EVENT));
				then(readerState.is(OBJECT_START_ELEMENT));
				inSequence(parseStream);

				// parsing object START_ELEMENT
				oneOf(reader).nextEvent();
				will(returnValue(OBJECT_START_ELEMENT_EVENT));
				then(readerState.is(CHARACTERS));
				inSequence(parseStream);

				// parse character contents
				// transition to end element state to mimick parsing
				oneOf(typeAdapter).parse(with(same(parsingContext)));
				will(returnValue(CHARACTERS));
				then(readerState.is(OBJECT_END_ELEMENT));
				inSequence(parseStream);
				oneOf(propertyCollector).add(CHARACTERS);
				inSequence(parseStream);


				// parse object END_ELEMENT
				oneOf(reader).nextEvent();
				will(returnValue(OBJECT_END_ELEMENT_EVENT));
				then(readerState.is(WRAPPER_END_ELEMENT));
				inSequence(parseStream);

				// parse wrapper END_ELEMENT
				oneOf(reader).nextEvent();
				will(returnValue(WRAPPER_END_ELEMENT_EVENT));
				// final state
				then(readerState.is(WRAPPER_END_ELEMENT));
				inSequence(parseStream);

				// apply parsed data to object
				oneOf(propertyCollector).applyCollection(value);
				inSequence(parseStream);
			}
		});

		DefaultXmlObjectPropertyParser propertyParser = new DefaultXmlObjectPropertyParser(propertyBinding, bindingContext);
		propertyParser.parse(value, parsingContext);

		context.assertIsSatisfied();
	}

	@Test
	void testAssemblyUngroupedStrings() throws BindingException, XMLStreamException {
		readerState.startsAs(OBJECT_START_ELEMENT);
		Sequence parseStream = context.sequence("parseStream");

		context.checking(new Expectations() {
			{
//				mockElementXMLEvent(this, WRAPPER_START_ELEMENT_EVENT, WRAPPER_END_ELEMENT_EVENT,
//						new QName(NS, WRAPPER_LOCAL_NAME));
				MockXmlSupport.mockElementXMLEvent(this, OBJECT_START_ELEMENT_EVENT, OBJECT_END_ELEMENT_EVENT,
						new QName(NS, OBJECT_LOCAL_NAME));
				MockXmlSupport.mockCharactersXMLEvent(this, CHARACTERS_EVENT, CHARACTERS);

				// setup PropertyInfo behavior
				allowing(propertyInfo).getXmlGroupAsBehavior();
				will(returnValue(XmlGroupAsBehavior.UNGROUPED));
//				allowing(propertyInfo).getGroupLocalName();
//				will(returnValue(WRAPPER_LOCAL_NAME));
//				allowing(propertyInfo).getGroupNamespace();
//				will(returnValue(NS));
				allowing(propertyInfo).getItemType();
				will(returnValue(String.class));

				// setup PropertyBinding behavior
				allowing(propertyBinding).getPropertyInfo();
				will(returnValue(propertyInfo));
				allowing(propertyBinding).isWrappedInXml();
				will(returnValue(true));
				allowing(propertyBinding).getXmlQName();
				will(returnValue(OBJECT_QNAME));

				// setup XmlParser behavior
				oneOf(bindingContext).getJavaTypeAdapter(String.class);
				will(returnValue(typeAdapter));
				allowing(parsingContext).getEventReader();
				will(returnValue(reader));
				allowing(typeAdapter).isParsingStartElement();
				will(returnValue(false));
				

				// setup reader peeking behavior based on states
//				allowing(reader).peek();
//				will(returnValue(WRAPPER_START_ELEMENT_EVENT));
//				when(readerState.is(WRAPPER_START_ELEMENT));
				allowing(reader).peek();
				will(returnValue(OBJECT_START_ELEMENT_EVENT));
				when(readerState.is(OBJECT_START_ELEMENT));
				allowing(reader).peek();
				will(returnValue(CHARACTERS_EVENT));
				when(readerState.is(CHARACTERS));
				allowing(reader).peek();
				will(returnValue(OBJECT_END_ELEMENT_EVENT));
				when(readerState.is(OBJECT_END_ELEMENT));
//				allowing(reader).peek();
//				will(returnValue(WRAPPER_END_ELEMENT_EVENT));
//				when(readerState.is(WRAPPER_END_ELEMENT));

				/* 
				 * the parsing sequence
				 */
				oneOf(propertyInfo).newPropertyCollector();
				will(returnValue(propertyCollector));
				inSequence(parseStream);

				// parse wrapper START_ELEMENT
//				oneOf(reader).nextEvent();
//				will(returnValue(WRAPPER_START_ELEMENT_EVENT));
//				then(readerState.is(OBJECT_START_ELEMENT));
//				inSequence(parseStream);

				// parsing object START_ELEMENT
				oneOf(reader).nextEvent();
				will(returnValue(OBJECT_START_ELEMENT_EVENT));
				then(readerState.is(CHARACTERS));
				inSequence(parseStream);

				// parse character contents
				// transition to end element state to mimick parsing
				oneOf(typeAdapter).parse(with(same(parsingContext)));
				will(returnValue(CHARACTERS));
				then(readerState.is(OBJECT_END_ELEMENT));
				inSequence(parseStream);
				oneOf(propertyCollector).add(CHARACTERS);
				inSequence(parseStream);

				// parse object END_ELEMENT
				oneOf(reader).nextEvent();
				will(returnValue(OBJECT_END_ELEMENT_EVENT));
				then(readerState.is(OBJECT_START_ELEMENT));
				inSequence(parseStream);

				// parsing object START_ELEMENT
				oneOf(reader).nextEvent();
				will(returnValue(OBJECT_START_ELEMENT_EVENT));
				then(readerState.is(CHARACTERS));
				inSequence(parseStream);

				// parse character contents
				// transition to end element state to mimick parsing
				oneOf(typeAdapter).parse(with(same(parsingContext)));
				will(returnValue(CHARACTERS));
				then(readerState.is(OBJECT_END_ELEMENT));
				inSequence(parseStream);
				oneOf(propertyCollector).add(CHARACTERS);
				inSequence(parseStream);


				// parse object END_ELEMENT
				oneOf(reader).nextEvent();
				will(returnValue(OBJECT_END_ELEMENT_EVENT));
				// final state
//				then(readerState.is(WRAPPER_END_ELEMENT));
				then(readerState.is(OBJECT_END_ELEMENT));
				inSequence(parseStream);

				// parse wrapper END_ELEMENT
//				oneOf(reader).nextEvent();
//				will(returnValue(WRAPPER_END_ELEMENT_EVENT));
//				then(readerState.is("complete"));
//				inSequence(parseStream);

				// apply parsed data to object
				oneOf(propertyCollector).applyCollection(value);
				inSequence(parseStream);
			}
		});

		DefaultXmlObjectPropertyParser propertyParser = new DefaultXmlObjectPropertyParser(propertyBinding, bindingContext);
		propertyParser.parse(value, parsingContext);

		context.assertIsSatisfied();
	}


	@Test
	void testAssemblyMarkupMultilineUnwrapped() throws BindingException, XMLStreamException {
		readerState.startsAs(OBJECT_START_ELEMENT);
		Sequence parseStream = context.sequence("parseStream");

		context.checking(new Expectations() {
			{
				MockXmlSupport.mockElementXMLEvent(this, OBJECT_START_ELEMENT_EVENT, OBJECT_END_ELEMENT_EVENT,
						new QName(NS, OBJECT_LOCAL_NAME));
				MockXmlSupport.mockCharactersXMLEvent(this, CHARACTERS_EVENT, CHARACTERS);

				// setup PropertyInfo behavior
				allowing(propertyInfo).getXmlGroupAsBehavior();
				will(returnValue(XmlGroupAsBehavior.UNGROUPED));
				allowing(propertyInfo).getItemType();
				will(returnValue(MarkupMultiline.class));

				// setup PropertyBinding behavior
				allowing(propertyBinding).getPropertyInfo();
				will(returnValue(propertyInfo));
				allowing(propertyBinding).isWrappedInXml();
				will(returnValue(false));
//				allowing(propertyBinding).getLocalName();
//				will(returnValue(OBJECT_LOCAL_NAME));
//				allowing(propertyBinding).getNamespace();
//				will(returnValue(NS));

				// setup XmlParser behavior
				oneOf(bindingContext).getJavaTypeAdapter(MarkupMultiline.class);
				will(returnValue(typeAdapter));
				allowing(parsingContext).getEventReader();
				will(returnValue(reader));

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

				/* 
				 * the parsing sequence
				 */
				oneOf(propertyInfo).newPropertyCollector();
				will(returnValue(propertyCollector));
				inSequence(parseStream);

				// expect parsing of object START_ELEMENT
//				oneOf(typeAdapter).isParsingStartElement();
//				will(returnValue(false));
//				inSequence(parseStream);

				// parsing object START_ELEMENT
//				oneOf(reader).nextEvent();
//				will(returnValue(OBJECT_START_ELEMENT_EVENT));
//				then(readerState.is(CHARACTERS));
//				inSequence(parseStream);

				// parse character contents
				// transition to end element state to mimick parsing
				oneOf(typeAdapter).parse(with(same(parsingContext)));
				will(returnValue(CHARACTERS));
				then(readerState.is(OBJECT_END_ELEMENT));
				inSequence(parseStream);
				oneOf(propertyCollector).add(CHARACTERS);
				inSequence(parseStream);

				// parse object END_ELEMENT
//				oneOf(reader).nextEvent();
//				will(returnValue(OBJECT_END_ELEMENT_EVENT));
//				then(readerState.is(OBJECT_START_ELEMENT));
//				inSequence(parseStream);
//
//				// expect parsing of object START_ELEMENT
//				oneOf(typeAdapter).isParsingStartElement();
//				will(returnValue(false));
//				inSequence(parseStream);
//
//				// parsing object START_ELEMENT
//				oneOf(reader).nextEvent();
//				will(returnValue(OBJECT_START_ELEMENT_EVENT));
//				then(readerState.is(CHARACTERS));
//				inSequence(parseStream);
//
//				// parse character contents
//				// transition to end element state to mimick parsing
//				oneOf(typeAdapter).parseType(with(reader));
//				will(returnValue(CHARACTERS));
//				then(readerState.is(OBJECT_END_ELEMENT));
//				inSequence(parseStream);
//				oneOf(propertyCollector).add(CHARACTERS);
//				inSequence(parseStream);
//
//
//				// parse object END_ELEMENT
//				oneOf(reader).nextEvent();
//				will(returnValue(OBJECT_END_ELEMENT_EVENT));
//				// final state
////				then(readerState.is(WRAPPER_END_ELEMENT));
//				then(readerState.is(OBJECT_END_ELEMENT));
//				inSequence(parseStream);

				// apply parsed data to object
				oneOf(propertyCollector).applyCollection(value);
				inSequence(parseStream);
			}
		});

		DefaultXmlObjectPropertyParser propertyParser = new DefaultXmlObjectPropertyParser(propertyBinding, bindingContext);
		propertyParser.parse(value, parsingContext);

		context.assertIsSatisfied();
	}

	private interface Value {

	}

}
