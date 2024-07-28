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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.json.MetaschemaJsonReader;
import gov.nist.secauto.metaschema.databind.model.test.MultiFieldAssembly;
import gov.nist.secauto.metaschema.databind.model.test.SimpleAssembly;

import org.jmock.auto.Mock;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

class DefaultFieldPropertyTest {
  @RegisterExtension
  JUnit5Mockery context = new JUnit5Mockery();

  @Mock
  private IModule module; // NOPMD - it's injected
  @Mock
  private IBoundDefinitionModelAssembly classBinding; // NOPMD - it's injected
  @Mock
  private IBindingContext bindingContext; // NOPMD - it's injected

  @Test
  void testJsonReadFlag()
      throws JsonParseException, IOException {
    String json = "{ \"test\": { \"id\": \"theId\", \"number\": 1 } }";
    JsonFactory factory = new JsonFactory();
    try (JsonParser jsonParser = factory.createParser(json)) {
      assert jsonParser != null;

      IBindingContext bindingContext = IBindingContext.instance();
      IBoundDefinitionModelAssembly classBinding
          = (IBoundDefinitionModelAssembly) bindingContext.getBoundDefinitionForClass(SimpleAssembly.class);
      assert classBinding != null;

      MetaschemaJsonReader parser = new MetaschemaJsonReader(jsonParser);

      SimpleAssembly obj = parser.readObjectRoot(classBinding, classBinding.getRootJsonName());
      assert obj != null;

      assertAll(
          () -> assertEquals("theId", obj.getId()));
    }
  }

  @Test
  void testJsonReadField()
      throws JsonParseException, IOException {

    String json = "{ \"field1\": \"field1value\", \"fields2\": [ \"field2value\" ] }";
    JsonFactory factory = new JsonFactory();
    try (JsonParser jsonParser = factory.createParser(json)) {
      assert jsonParser != null;
      // get first token
      jsonParser.nextToken();

      IBindingContext bindingContext = IBindingContext.instance();
      IBoundDefinitionModelComplex classBinding = bindingContext.getBoundDefinitionForClass(MultiFieldAssembly.class);
      assert classBinding != null;

      MetaschemaJsonReader parser = new MetaschemaJsonReader(jsonParser);

      // read the top-level definition
      MultiFieldAssembly obj = (MultiFieldAssembly) parser.readObject(classBinding);

      assertAll(
          () -> assertEquals("field1value", obj.getField1()),
          () -> assertTrue(obj.getField2() instanceof LinkedList),
          () -> assertIterableEquals(Collections.singleton("field2value"), obj.getField2()));

      // assertEquals(JsonToken.START_OBJECT, jsonParser.nextToken());
      // assertEquals(JsonToken.FIELD_NAME, jsonParser.nextToken());
      // assertEquals("id", jsonParser.currentName());
    }
  }

  @Test
  void testJsonReadMissingFieldValue()
      throws JsonParseException, IOException {
    String json = "{ \"fields2\": [\n"
        + "    \"field2value\"\n"
        + "    ]\n"
        + "}\n";
    JsonFactory factory = new JsonFactory();
    try (JsonParser jsonParser = factory.createParser(json)) {
      assert jsonParser != null;
      // get first token
      jsonParser.nextToken();

      IBindingContext bindingContext = IBindingContext.instance();
      IBoundDefinitionModelComplex classBinding = bindingContext.getBoundDefinitionForClass(MultiFieldAssembly.class);
      assert classBinding != null;

      MetaschemaJsonReader parser = new MetaschemaJsonReader(jsonParser);

      // read the top-level definition
      MultiFieldAssembly obj = (MultiFieldAssembly) parser.readObject(classBinding);

      assertAll(
          () -> assertNull(obj.getField1()),
          () -> assertTrue(obj.getField2() instanceof LinkedList),
          () -> assertIterableEquals(Collections.singleton("field2value"), obj.getField2()));
    }
  }

  @Test
  void testJsonReadFieldValueKey()
      throws JsonParseException, IOException {
    String json = "{ \"field-value-key\": { \"a-value\": \"theValue\" } }";
    JsonFactory factory = new JsonFactory();
    try (JsonParser jsonParser = factory.createParser(json)) {
      assert jsonParser != null;
      // get first token
      jsonParser.nextToken();

      IBindingContext bindingContext = IBindingContext.instance();
      IBoundDefinitionModelComplex classBinding = bindingContext.getBoundDefinitionForClass(MultiFieldAssembly.class);
      assert classBinding != null;

      MetaschemaJsonReader parser = new MetaschemaJsonReader(jsonParser);

      // read the top-level definition
      MultiFieldAssembly obj = (MultiFieldAssembly) parser.readObject(classBinding);

      assertAll(
          () -> assertEquals("theValue", obj.getField3().getValue()));
    }
  }

  @Test
  void testJsonReadFieldDefaultValueKey()
      throws JsonParseException, IOException {
    String json = "{ \"field-default-value-key\": { \"STRVALUE\": \"theValue\" } }";
    JsonFactory factory = new JsonFactory();
    try (JsonParser jsonParser = factory.createParser(json)) {
      assert jsonParser != null;
      // get first token
      jsonParser.nextToken();

      IBindingContext bindingContext = IBindingContext.instance();
      IBoundDefinitionModelComplex classBinding = bindingContext.getBoundDefinitionForClass(MultiFieldAssembly.class);
      assert classBinding != null;

      MetaschemaJsonReader parser = new MetaschemaJsonReader(jsonParser);

      // read the top-level definition
      MultiFieldAssembly obj = (MultiFieldAssembly) parser.readObject(classBinding);

      assertAll(
          () -> assertEquals("theValue", obj.getField4().getValue()));
    }
  }
}
