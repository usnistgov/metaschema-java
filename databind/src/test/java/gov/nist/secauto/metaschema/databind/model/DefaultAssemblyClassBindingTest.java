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

package gov.nist.secauto.metaschema.databind.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.databind.io.json.MetaschemaJsonReader;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class DefaultAssemblyClassBindingTest
    extends AbstractBoundModelTestSupport {
  @Test
  void testMinimalJsonParse() throws JsonParseException, IOException {
    Path testContent = Paths.get("src/test/resources/content/minimal.json");
    try (BufferedReader reader = Files.newBufferedReader(testContent)) {
      assert reader != null;

      IBoundDefinitionModelAssembly classBinding = getRootAssemblyClassBinding();

      try (JsonParser parser = newJsonParser(reader)) {
        Object value = new MetaschemaJsonReader(parser).readObjectRoot(classBinding, classBinding.getRootJsonName());
        assertNotNull(value, "root was null");
      }
    }
  }

  @Test
  void testModule() {
    IBoundDefinitionModelAssembly definition = getRootAssemblyClassBinding();
    IModule module = definition.getContainingModule();
    assertNotNull(module, "metaschema was null");
  }

  // @Test
  // void testSimpleJson() throws JsonParseException, IOException,
  // BindingException {
  // File testContent
  // = new
  // File(getClass().getClassLoader().getResource("test-content/bound-class-simple.json").getFile());
  // try (BufferedReader reader = Files.newBufferedReader(testContent.toPath())) {
  // JsonParser jsonParser = newJsonParser(reader);
  //
  // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
  // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());
  //
  // IAssemblyClassBinding classBinding = getAssemblyClassBinding();
  // IBoundAssemblyInstance root = new RootAssemblyDefinition(classBinding);
  // BoundClass obj = (BoundClass) root.read(jsonParsingContext);
  //
  // assertEquals(JsonToken.END_OBJECT, jsonParser.currentToken());
  // assertSimple(obj);
  // }
  // }
  //
  // @Test
  // void testSimpleXml() throws BindingException, XMLStreamException, IOException
  // {
  // File testContent
  // = new
  // File(getClass().getClassLoader().getResource("test-content/bound-class-simple.xml").getFile());
  // try (BufferedReader reader = Files.newBufferedReader(testContent.toPath())) {
  // XMLEventReader eventReader = newXmlParser(reader);
  //
  // IAssemblyClassBinding classBinding = getAssemblyClassBinding();
  //
  // // assertEquals(XMLEvent.START_DOCUMENT, parser.nextEvent().getEventType());
  //
  // BoundClass obj = (BoundClass) classBinding.readRoot(xmlParsingContext);
  //
  // assertEquals(XMLEvent.END_DOCUMENT, eventReader.peek().getEventType());
  // assertSimple(obj);
  // }
  // }
  //
  // private void assertSimple(BoundClass obj) {
  // assertNotNull(obj);
  // assertEquals("idvalue", obj.getId());
  // assertNull(obj.getSingleSimpleField());
  // assertNotNull(obj.getGroupedListSimpleField());
  // assertTrue(obj.getGroupedListSimpleField().isEmpty());
  // assertNull(obj.getSingleFlaggedField());
  // assertNotNull(obj.getGroupedListField());
  // assertTrue(obj.getGroupedListField().isEmpty());
  // assertNotNull(obj.getUngroupedListField());
  // assertTrue(obj.getUngroupedListField().isEmpty());
  // assertNotNull(obj.getMappedField());
  // assertTrue(obj.getMappedField().isEmpty());
  //
  // context.assertIsSatisfied();
  // }
  //
  // @Test
  // void testComplexJson() throws BindingException, IOException {
  // File testContent
  // = new
  // File(getClass().getClassLoader().getResource("test-content/bound-class-complex.json").getFile());
  // try (BufferedReader reader = Files.newBufferedReader(testContent.toPath())) {
  // JsonParser jsonParser = newJsonParser(reader);
  //
  // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
  // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());
  //
  // IAssemblyClassBinding classBinding = getAssemblyClassBinding();
  // IBoundAssemblyInstance root = new RootAssemblyDefinition(classBinding);
  // BoundClass obj = (BoundClass) root.read(jsonParsingContext);
  //
  // assertEquals(JsonToken.END_OBJECT, jsonParser.currentToken());
  // assertComplex(obj);
  // }
  // }
  //
  // @Test
  // void testComplexXml() throws BindingException, XMLStreamException,
  // IOException {
  // File testContent
  // = new
  // File(getClass().getClassLoader().getResource("test-content/bound-class-complex.xml").getFile());
  // try (BufferedReader reader = Files.newBufferedReader(testContent.toPath())) {
  // XMLEventReader eventReader = newXmlParser(reader);
  //
  // IAssemblyClassBinding classBinding = getAssemblyClassBinding();
  //
  // BoundClass obj = (BoundClass) classBinding.readRoot(xmlParsingContext);
  //
  // assertEquals(XMLEvent.END_DOCUMENT, eventReader.peek().getEventType());
  // assertComplex(obj);
  // }
  // }
  //
  // private void assertComplex(BoundClass obj) {
  // assertNotNull(obj);
  // assertEquals("idvalue", obj.getId());
  // assertEquals("single-simple-value", obj.getSingleSimpleField());
  // assertNotNull(obj.getGroupedListSimpleField());
  // assertIterableEquals(List.of("grouped-list-simple-item-value-1",
  // "grouped-list-simple-item-value-2"),
  // obj.getGroupedListSimpleField());
  //
  // assertNotNull(obj.getSingleFlaggedField());
  // FlaggedField flaggedField = obj.getSingleFlaggedField();
  // assertEquals("single-flagged-id", flaggedField.getId());
  // assertEquals("single-flagged-value", flaggedField.getValue());
  // //
  // //
  // // assertNotNull(obj.getGroupedListField());
  // // assertTrue(obj.getGroupedListField().isEmpty());
  // // assertNotNull(obj.getUngroupedListField());
  // // assertTrue(obj.getUngroupedListField().isEmpty());
  // // assertNotNull(obj.getMappedField());
  // // assertTrue(obj.getMappedField().isEmpty());
  //
  // context.assertIsSatisfied();
  // }

}
