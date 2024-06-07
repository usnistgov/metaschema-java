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

package gov.nist.secauto.metaschema.databind.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class BasicMetaschemaTest
    extends AbstractMetaschemaTest {

  @Test
  void testSimpleMetaschema() throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    runTests("simple", "gov.nist.csrc.ns.metaschema.testing.simple.TopLevel", ObjectUtils.notNull(generationDir));
    // runTests("simple", "gov.nist.csrc.ns.metaschema.testing.simple.TopLevel",
    // generationDir, (obj) ->
    // {
    // try {
    // Assertions.assertEquals("test", reflectMethod(obj, "getId"));
    // } catch (NoSuchMethodException | SecurityException e) {
    // Assertions.fail(e);
    // }
    // });
  }

  @Test
  void testSimpleUuidMetaschema()
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    runTests(
        "simple_with_uuid",
        "gov.nist.csrc.ns.metaschema.testing.simple.with.uuid.TopLevel",
        ObjectUtils.notNull(generationDir),
        (obj) -> {
          try {
            assertEquals("5de455cf-2f8d-4da2-9182-323d433e1065", reflectMethod(obj, "getUuid").toString());
          } catch (NoSuchMethodException | SecurityException e) {
            fail(e);
          }
        });
  }

  @Test
  void testSimpleWithFieldMetaschema()
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    runTests(
        "simple_with_field",
        "gov.nist.csrc.ns.metaschema.testing.simple.with.field.TopLevel",
        ObjectUtils.notNull(generationDir));
  }

  private static Object reflectMethod(Object obj, String name) throws NoSuchMethodException {
    return ReflectionUtils.invokeMethod(obj.getClass().getMethod(name), obj);
  }

  @Test
  void testFieldsWithFlagMetaschema()
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    runTests(
        "fields_with_flags",
        "gov.nist.csrc.ns.metaschema.testing.fields.with.flags.TopLevel",
        ObjectUtils.notNull(generationDir),
        (obj) -> {
          try {
            Assertions.assertEquals("test", reflectMethod(obj, "getId"));
            Object field1 = ReflectionUtils.invokeMethod(obj.getClass().getMethod("getComplexField1"), obj);
            Assertions.assertNotNull(field1);
            Assertions.assertEquals("complex-field1", reflectMethod(field1, "getId"));
            Assertions.assertEquals("test-string", reflectMethod(field1, "getValue"));

            @SuppressWarnings("unchecked") List<Object> field2s
                = (List<Object>) ReflectionUtils.invokeMethod(obj.getClass().getMethod("getComplexFields2"),
                    obj);
            Assertions.assertNotNull(field2s);
            Assertions.assertEquals(1, field2s.size());
            Object field2 = field2s.get(0);
            Assertions.assertEquals("complex-field2-1", reflectMethod(field2, "getId"));
            Assertions.assertEquals("test-string2", reflectMethod(field2, "getValue"));

            @SuppressWarnings("unchecked") List<Object> field3s
                = (List<Object>) ReflectionUtils.invokeMethod(obj.getClass().getMethod("getComplexFields3"),
                    obj);
            Assertions.assertEquals(2, field3s.size());
            Assertions.assertAll("ComplexFields4 item", () -> {
              Object item = field3s.get(0);
              assertEquals("complex-field3-1", reflectMethod(item, "getId2"));
              assertEquals("test-string3", reflectMethod(item, "getValue"));
            });
            Assertions.assertAll("ComplexFields4 item", () -> {
              Object item = field3s.get(1);
              assertEquals("complex-field3-2", reflectMethod(item, "getId2"));
              assertEquals("test-string4", reflectMethod(item, "getValue"));
            });

            Assertions.assertAll("ComplexFields4", () -> {
              @SuppressWarnings("unchecked") Map<String, Object> collection
                  = (Map<String, Object>) ReflectionUtils.invokeMethod(obj.getClass().getMethod("getComplexFields4"),
                      obj);
              Assertions.assertNotNull(collection);
              Assertions.assertEquals(2, collection.size());
              Set<Map.Entry<String, Object>> entries = collection.entrySet();
              Iterator<Map.Entry<String, Object>> iter = entries.iterator();

              Assertions.assertAll("ComplexFields4 item", () -> {
                Map.Entry<String, Object> entry = iter.next();
                assertEquals("complex-field4-1", entry.getKey());
                assertEquals("complex-field4-1", reflectMethod(entry.getValue(), "getId2"));
                assertEquals("test-string5", reflectMethod(entry.getValue(), "getValue"));
              });

              Assertions.assertAll("ComplexFields4 item", () -> {
                Map.Entry<String, Object> entry = iter.next();
                assertEquals("complex-field4-2", entry.getKey());
                assertEquals("complex-field4-2", reflectMethod(entry.getValue(), "getId2"));
                assertEquals("test-string6", reflectMethod(entry.getValue(), "getValue"));
              });
            });
          } catch (NoSuchMethodException | SecurityException e) {
            Assertions.fail(e);
          }
        });
  }

  @Test
  void testAssemblyMetaschema()
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    runTests(
        "assembly",
        "gov.nist.itl.metaschema.codegen.xml.example.assembly.TopLevel",
        ObjectUtils.notNull(generationDir),
        (obj) -> {
          try {
            Assertions.assertEquals("test", reflectMethod(obj, "getId"));
          } catch (NoSuchMethodException | SecurityException e) {
            Assertions.fail(e);
          }
        });
  }

  @Test
  void testLocalDefinitionsMetaschema()
      throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    runTests(
        "local-definitions",
        "gov.nist.csrc.ns.metaschema.testing.local.definitions.TopLevel",
        ObjectUtils.notNull(generationDir));
  }

}
