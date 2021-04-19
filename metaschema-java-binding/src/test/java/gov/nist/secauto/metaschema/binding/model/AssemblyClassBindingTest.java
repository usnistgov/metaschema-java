/*
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

package gov.nist.secauto.metaschema.binding.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ctc.wstx.stax.WstxInputFactory;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.model.BoundClass.FlaggedField;
import gov.nist.secauto.metaschema.datatypes.adapter.types.StringAdapter;

import org.codehaus.stax2.XMLEventReader2;
import org.jmock.Expectations;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

class AssemblyClassBindingTest {
  @RegisterExtension
  JUnit5Mockery context = new JUnit5Mockery();

  private BindingContext bindingContext = context.mock(BindingContext.class);
  private JsonParsingContext jsonParsingContext = context.mock(JsonParsingContext.class);
  private XmlParsingContext xmlParsingContext = context.mock(XmlParsingContext.class);

  private AssemblyClassBinding getAssemblyClassBinding() {

    context.checking(new Expectations() {
      {
        allowing(bindingContext).getJavaTypeAdapterInstance(StringAdapter.class);
        will(returnValue(new StringAdapter()));
      }
    });

    {
      FieldClassBinding fieldClassBinding
          = DefaultFieldClassBinding.createInstance(BoundClass.FlaggedField.class, bindingContext);

      context.checking(new Expectations() {
        {
          allowing(bindingContext).getClassBinding(BoundClass.FlaggedField.class);
          will(returnValue(fieldClassBinding));
        }
      });
    }

    {
      FieldClassBinding fieldClassBinding
          = DefaultFieldClassBinding.createInstance(BoundClass.KeyedField.class, bindingContext);

      context.checking(new Expectations() {
        {
          allowing(bindingContext).getClassBinding(BoundClass.KeyedField.class);
          will(returnValue(fieldClassBinding));
        }
      });
    }

    {
      AssemblyClassBinding fieldClassBinding
          = DefaultAssemblyClassBinding.createInstance(FlaggedAssemblyClass.class, bindingContext);

      context.checking(new Expectations() {
        {
          allowing(bindingContext).getClassBinding(FlaggedAssemblyClass.class);
          will(returnValue(fieldClassBinding));
        }
      });
    }

    return DefaultAssemblyClassBinding.createInstance(BoundClass.class, bindingContext);
  }

  private XMLEventReader2 newXmlParser(Reader reader) throws XMLStreamException {

    XMLInputFactory factory = WstxInputFactory.newInstance();
    XMLEventReader2 parser = (XMLEventReader2) factory.createXMLEventReader(reader);

    context.checking(new Expectations() {
      {
        allowing(xmlParsingContext).getReader();
        will(returnValue(parser));
      }
    });
    return parser;
  }

  private JsonParser newJsonParser(Reader reader) throws JsonParseException, IOException {
    JsonFactory factory = new JsonFactory();
    JsonParser jsonParser = factory.createParser(reader);

    context.checking(new Expectations() {
      {
        allowing(jsonParsingContext).getReader();
        will(returnValue(jsonParser));
      }
    });
    return jsonParser;
  }

  @Test
  void testSimpleJson() throws JsonParseException, IOException, BindingException {
    File testContent
        = new File(getClass().getClassLoader().getResource("test-content/bound-class-simple.json").getFile());
    JsonParser jsonParser = newJsonParser(new FileReader(testContent));

    AssemblyClassBinding classBinding = getAssemblyClassBinding();

    BoundClass obj = (BoundClass) classBinding.readRoot(jsonParsingContext);

    assertFalse(jsonParser.hasCurrentToken());
    assertSimple(obj);
  }

  @Test
  void testSimpleXml() throws BindingException, XMLStreamException, IOException {
    File testContent
        = new File(getClass().getClassLoader().getResource("test-content/bound-class-simple.xml").getFile());
    XMLEventReader reader = newXmlParser(new FileReader(testContent));

    AssemblyClassBinding classBinding = getAssemblyClassBinding();

    // assertEquals(XMLEvent.START_DOCUMENT, parser.nextEvent().getEventType());

    BoundClass obj = (BoundClass) classBinding.readRoot(xmlParsingContext);

    assertFalse(reader.hasNext());
    assertSimple(obj);
  }

  private void assertSimple(BoundClass obj) {
    assertNotNull(obj);
    assertEquals("idvalue", obj.getId());
    assertNull(obj.getSingleSimpleField());
    assertNotNull(obj.getGroupedListSimpleField());
    assertTrue(obj.getGroupedListSimpleField().isEmpty());
    assertNull(obj.getSingleFlaggedField());
    assertNotNull(obj.getGroupedListField());
    assertTrue(obj.getGroupedListField().isEmpty());
    assertNotNull(obj.getUngroupedListField());
    assertTrue(obj.getUngroupedListField().isEmpty());
    assertNotNull(obj.getMappedField());
    assertTrue(obj.getMappedField().isEmpty());

    context.assertIsSatisfied();
  }

  @Test
  void testComplexJson() throws BindingException, IOException {
    File testContent
        = new File(getClass().getClassLoader().getResource("test-content/bound-class-complex.json").getFile());
    JsonParser jsonParser = newJsonParser(new FileReader(testContent));

    AssemblyClassBinding classBinding = getAssemblyClassBinding();

    BoundClass obj = (BoundClass) classBinding.readRoot(jsonParsingContext);

    assertFalse(jsonParser.hasCurrentToken());
    assertComplex(obj);
  }

  @Test
  void testComplexXml() throws BindingException, XMLStreamException, IOException {
    File testContent
        = new File(getClass().getClassLoader().getResource("test-content/bound-class-complex.xml").getFile());
    XMLEventReader reader = newXmlParser(new FileReader(testContent));

    AssemblyClassBinding classBinding = getAssemblyClassBinding();

    BoundClass obj = (BoundClass) classBinding.readRoot(xmlParsingContext);

    assertFalse(reader.hasNext());
    assertComplex(obj);
  }

  private void assertComplex(BoundClass obj) {
    assertNotNull(obj);
    assertEquals("idvalue", obj.getId());
    assertEquals("single-simple-value", obj.getSingleSimpleField());
    assertNotNull(obj.getGroupedListSimpleField());
    assertIterableEquals(List.of("grouped-list-simple-item-value-1", "grouped-list-simple-item-value-2"), obj.getGroupedListSimpleField());

    assertNotNull(obj.getSingleFlaggedField());
    FlaggedField flaggedField = obj.getSingleFlaggedField();
    assertEquals("single-flagged-id", flaggedField.getId());
    assertEquals("single-flagged-value", flaggedField.getValue());
//    
//    
//    assertNotNull(obj.getGroupedListField());
//    assertTrue(obj.getGroupedListField().isEmpty());
//    assertNotNull(obj.getUngroupedListField());
//    assertTrue(obj.getUngroupedListField().isEmpty());
//    assertNotNull(obj.getMappedField());
//    assertTrue(obj.getMappedField().isEmpty());

    context.assertIsSatisfied();
  }
}
