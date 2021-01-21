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

import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;

import org.codehaus.stax2.XMLEventReader2;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.States;
import org.jmock.auto.Auto;
import org.jmock.auto.Mock;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

class AbstractXmlParsePlanTest {
  private static final String OBJECT_LOCAL_NAME = "object";
  private static final String ATTRIBUTE_A_LOCAL_NAME = "a";
  private static final String ATTRIBUTE_B_LOCAL_NAME = "b";
  private static final String NS = "namespace";

  private static final String OBJECT_START_ELEMENT = "object-start-element";
  private static final String CHARACTERS = "characters";
  private static final String OBJECT_END_ELEMENT = "object-end-element";
  // private static final String ATTRIBUTE_A = "attribute-a";
  // private static final String ATTRIBUTE_B = "attribute-b";

  @RegisterExtension
  JUnit5Mockery context = new JUnit5Mockery();

  @Mock
  private StartElement OBJECT_START_ELEMENT_EVENT;
  @Mock
  private EndElement OBJECT_END_ELEMENT_EVENT;
  @Mock
  private Characters CHARACTERS_EVENT;
  @Auto
  private States readerState;

  @Mock
  private ClassBinding<Value> classBinding;
  @Mock
  private XmlParsingContext parsingContext;
  @Mock
  private XMLEventReader2 reader;

  @Test
  void testEmpty() throws BindingException, XMLStreamException {

    readerState.startsAs(OBJECT_START_ELEMENT);
    Sequence parseStream = context.sequence("parseStream");

    context.checking(new Expectations() {
      {
        allowing(parsingContext).getEventReader();
        will(returnValue(reader));

        MockXmlSupport.mockElementXMLEvent(this, OBJECT_START_ELEMENT_EVENT, OBJECT_END_ELEMENT_EVENT,
            new QName(NS, OBJECT_LOCAL_NAME));

        Value value = new Value();
        allowing(classBinding).newInstance();
        will(returnValue(value));
        allowing(classBinding).callBeforeDeserialize(with(any(Value.class)), with.is(anything()));
        allowing(classBinding).callAfterDeserialize(with(any(Value.class)), with.is(anything()));

        oneOf(OBJECT_START_ELEMENT_EVENT).getAttributes();
        will(returnValue(Collections.emptyList().iterator()));

        // setup reader peeking behavior based on states
        allowing(reader).peek();
        will(returnValue(OBJECT_START_ELEMENT_EVENT));
        when(readerState.is(OBJECT_START_ELEMENT));
        allowing(reader).peek();
        // will(returnValue(CHARACTERS_EVENT));
        // when(readerState.is(CHARACTERS));
        // allowing(reader).peek();
        will(returnValue(OBJECT_END_ELEMENT_EVENT));
        when(readerState.is(OBJECT_END_ELEMENT));
        allowing(reader).peek();

        /*
         * the parsing sequence
         */
        // // parsing object START_ELEMENT
        oneOf(reader).nextEvent();
        will(returnValue(OBJECT_START_ELEMENT_EVENT));
        // then(readerState.is(CHARACTERS));
        then(readerState.is(OBJECT_END_ELEMENT));
        inSequence(parseStream);

        // end of document
        oneOf(reader).nextEvent();
        inSequence(parseStream);
      }
    });

    AbstractXmlParsePlan<Value, ClassBinding<Value>> parsePlan
        = new AbstractXmlParsePlan<>(classBinding, Collections.emptyMap()) {

          @Override
          protected void parseBody(Value obj, XmlParsingContext parsingContext, StartElement start)
              throws BindingException {
          }

        };

    parsePlan.parse(null, parsingContext);

    context.assertIsSatisfied();
  }

  @Test
  void testAttributes() throws BindingException, XMLStreamException {

    Attribute attributeA = context.mock(Attribute.class, "attributeA");
    Attribute attributeB = context.mock(Attribute.class, "attributeB");
    List<Attribute> attributes = new LinkedList<>();
    attributes.add(attributeA);
    attributes.add(attributeB);

    XmlAttributePropertyParser attributeParserA
        = context.mock(XmlAttributePropertyParser.class, "XmlAttributePropertyParserA");
    XmlAttributePropertyParser attributeParserB
        = context.mock(XmlAttributePropertyParser.class, "XmlAttributePropertyParserB");

    readerState.startsAs(OBJECT_START_ELEMENT);
    Sequence parseStream = context.sequence("parseStream");

    context.checking(new Expectations() {
      {
        allowing(classBinding).getClazz();
        will(returnValue(Value.class));
        Value value = new Value();
        allowing(classBinding).newInstance();
        will(returnValue(value));
        allowing(classBinding).callBeforeDeserialize(with(any(Value.class)), with.is(anything()));
        allowing(classBinding).callAfterDeserialize(with(any(Value.class)), with.is(anything()));
        allowing(parsingContext).getEventReader();
        will(returnValue(reader));

        MockXmlSupport.mockElementXMLEvent(this, OBJECT_START_ELEMENT_EVENT, OBJECT_END_ELEMENT_EVENT,
            new QName(NS, OBJECT_LOCAL_NAME));

        MockXmlSupport.mockAttributeXMLEvent(this, attributeA, new QName(ATTRIBUTE_A_LOCAL_NAME), CHARACTERS);
        MockXmlSupport.mockAttributeXMLEvent(this, attributeB, new QName(ATTRIBUTE_B_LOCAL_NAME), CHARACTERS);

        oneOf(OBJECT_START_ELEMENT_EVENT).getAttributes();
        will(returnValue(attributes.iterator()));

        // setup reader peeking behavior based on states
        allowing(reader).peek();
        will(returnValue(OBJECT_START_ELEMENT_EVENT));
        when(readerState.is(OBJECT_START_ELEMENT));
        allowing(reader).peek();
        will(returnValue(OBJECT_END_ELEMENT_EVENT));
        when(readerState.is(OBJECT_END_ELEMENT));
        allowing(reader).peek();

        /*
         * the parsing sequence
         */
        // // parsing object START_ELEMENT
        oneOf(reader).nextEvent();
        will(returnValue(OBJECT_START_ELEMENT_EVENT));
        then(readerState.is(OBJECT_END_ELEMENT));
        inSequence(parseStream);

        // parse the attributes
        oneOf(attributeParserA).parse(with(any(Value.class)), with(same(parsingContext)), with(same(attributeA)));
        inSequence(parseStream);
        oneOf(attributeParserB).parse(with(any(Value.class)), with(same(parsingContext)), with(same(attributeB)));
        inSequence(parseStream);

        // end of document
        oneOf(reader).nextEvent();
        inSequence(parseStream);
      }
    });

    Map<QName, XmlAttributePropertyParser> attributeParsers = new LinkedHashMap<>();
    attributeParsers.put(attributeA.getName(), attributeParserA);
    attributeParsers.put(attributeB.getName(), attributeParserB);

    AbstractXmlParsePlan<Value, ClassBinding<Value>> parsePlan
        = new AbstractXmlParsePlan<>(classBinding, attributeParsers) {

          @Override
          protected void parseBody(Value obj, XmlParsingContext parsingContext, StartElement start)
              throws BindingException {
          }

        };

    parsePlan.parse(null, parsingContext);

    context.assertIsSatisfied();
  }

  public static class Value {
    public Value() {

    }
  }
}
