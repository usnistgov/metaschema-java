/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.binding.io.xml.parser;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.property.CollectionPropertyInfo;
import gov.nist.secauto.metaschema.binding.model.property.FieldPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyCollector;
import gov.nist.secauto.metaschema.datatypes.markup.MarkupMultiline;

import org.codehaus.stax2.XMLEventReader2;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.auto.Auto;
import org.jmock.auto.Mock;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

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
  // @Mock
  // private ParsePlan<XMLEventReader2, Value> plan;
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

        allowing(bindingContext).getClassBinding(MarkupMultiline.class);
        will(returnValue(null));
        allowing(bindingContext).getClassBinding(String.class);
        will(returnValue(null));

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
        // mockElementXMLEvent(this, WRAPPER_START_ELEMENT_EVENT, WRAPPER_END_ELEMENT_EVENT,
        // new QName(NS, WRAPPER_LOCAL_NAME));
        MockXmlSupport.mockElementXMLEvent(this, OBJECT_START_ELEMENT_EVENT, OBJECT_END_ELEMENT_EVENT,
            new QName(NS, OBJECT_LOCAL_NAME));
        MockXmlSupport.mockCharactersXMLEvent(this, CHARACTERS_EVENT, CHARACTERS);

        allowing(bindingContext).getClassBinding(String.class);
        will(returnValue(null));

        // setup PropertyInfo behavior
        allowing(propertyInfo).getXmlGroupAsBehavior();
        will(returnValue(XmlGroupAsBehavior.UNGROUPED));
        // allowing(propertyInfo).getGroupLocalName();
        // will(returnValue(WRAPPER_LOCAL_NAME));
        // allowing(propertyInfo).getGroupNamespace();
        // will(returnValue(NS));
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
        // allowing(reader).peek();
        // will(returnValue(WRAPPER_START_ELEMENT_EVENT));
        // when(readerState.is(WRAPPER_START_ELEMENT));
        allowing(reader).peek();
        will(returnValue(OBJECT_START_ELEMENT_EVENT));
        when(readerState.is(OBJECT_START_ELEMENT));
        allowing(reader).peek();
        will(returnValue(CHARACTERS_EVENT));
        when(readerState.is(CHARACTERS));
        allowing(reader).peek();
        will(returnValue(OBJECT_END_ELEMENT_EVENT));
        when(readerState.is(OBJECT_END_ELEMENT));
        // allowing(reader).peek();
        // will(returnValue(WRAPPER_END_ELEMENT_EVENT));
        // when(readerState.is(WRAPPER_END_ELEMENT));

        /*
         * the parsing sequence
         */
        oneOf(propertyInfo).newPropertyCollector();
        will(returnValue(propertyCollector));
        inSequence(parseStream);

        // parse wrapper START_ELEMENT
        // oneOf(reader).nextEvent();
        // will(returnValue(WRAPPER_START_ELEMENT_EVENT));
        // then(readerState.is(OBJECT_START_ELEMENT));
        // inSequence(parseStream);

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
        // then(readerState.is(WRAPPER_END_ELEMENT));
        then(readerState.is(OBJECT_END_ELEMENT));
        inSequence(parseStream);

        // parse wrapper END_ELEMENT
        // oneOf(reader).nextEvent();
        // will(returnValue(WRAPPER_END_ELEMENT_EVENT));
        // then(readerState.is("complete"));
        // inSequence(parseStream);

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

        allowing(bindingContext).getClassBinding(MarkupMultiline.class);
        will(returnValue(null));

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
        // allowing(propertyBinding).getLocalName();
        // will(returnValue(OBJECT_LOCAL_NAME));
        // allowing(propertyBinding).getNamespace();
        // will(returnValue(NS));

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
        // oneOf(typeAdapter).isParsingStartElement();
        // will(returnValue(false));
        // inSequence(parseStream);

        // parsing object START_ELEMENT
        // oneOf(reader).nextEvent();
        // will(returnValue(OBJECT_START_ELEMENT_EVENT));
        // then(readerState.is(CHARACTERS));
        // inSequence(parseStream);

        // parse character contents
        // transition to end element state to mimick parsing
        oneOf(typeAdapter).parse(with(same(parsingContext)));
        will(returnValue(CHARACTERS));
        then(readerState.is(OBJECT_END_ELEMENT));
        inSequence(parseStream);
        oneOf(propertyCollector).add(CHARACTERS);
        inSequence(parseStream);

        // parse object END_ELEMENT
        // oneOf(reader).nextEvent();
        // will(returnValue(OBJECT_END_ELEMENT_EVENT));
        // then(readerState.is(OBJECT_START_ELEMENT));
        // inSequence(parseStream);
        //
        // // expect parsing of object START_ELEMENT
        // oneOf(typeAdapter).isParsingStartElement();
        // will(returnValue(false));
        // inSequence(parseStream);
        //
        // // parsing object START_ELEMENT
        // oneOf(reader).nextEvent();
        // will(returnValue(OBJECT_START_ELEMENT_EVENT));
        // then(readerState.is(CHARACTERS));
        // inSequence(parseStream);
        //
        // // parse character contents
        // // transition to end element state to mimick parsing
        // oneOf(typeAdapter).parseType(with(reader));
        // will(returnValue(CHARACTERS));
        // then(readerState.is(OBJECT_END_ELEMENT));
        // inSequence(parseStream);
        // oneOf(propertyCollector).add(CHARACTERS);
        // inSequence(parseStream);
        //
        //
        // // parse object END_ELEMENT
        // oneOf(reader).nextEvent();
        // will(returnValue(OBJECT_END_ELEMENT_EVENT));
        // // final state
        //// then(readerState.is(WRAPPER_END_ELEMENT));
        // then(readerState.is(OBJECT_END_ELEMENT));
        // inSequence(parseStream);

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
