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

package gov.nist.secauto.metaschema.schemagen.json;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

class JsonSchemaGeneratorTest
    extends JsonSchemaTestSupport {
  private static final Path UNIT_TEST_PATH = Paths.get("../metaschema-model/metaschema/test-suite/schema-generation");

  // @TempDir
  // Path schemaGenerationPath;
  Path schemaGenerationPath = Paths.get("test-schemagen");

  @Override
  protected Path getSchemaGenerationPath() {
    return schemaGenerationPath;
  }

  @Test
  void test() throws IOException, MetaschemaException {
    MetaschemaLoader loader = new MetaschemaLoader();

    // IMetaschema metaschema
    // = loader.loadXmlMetaschema(new URL(
    // "https://raw.githubusercontent.com/usnistgov/OSCAL/main/src/metaschema/oscal_complete_metaschema.xml"));
    IMetaschema metaschema
        = loader.loadXmlMetaschema(Paths.get("../../OSCAL/src/metaschema/oscal_complete_metaschema.xml"));

    // IAssemblyDefinition part = metaschema.getExportedAssemblyDefinitionMap().get("parent");
    // Collection<? extends IModelInstance> model = part.getModelInstances();
    // IModelInstance instance = model.toArray(new IModelInstance[0])[0];
    // IFieldInstance field = (IFieldInstance) instance;

    ISchemaGenerator generator = new JsonSchemaGenerator();

    Path schemaPath = getSchemaGenerationPath().resolve("test-schema.json");
    Files.createDirectories(schemaPath.getParent());
    
    try (OutputStream os = Files.newOutputStream(schemaPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      TeeOutputStream out = new TeeOutputStream(System.out, os);
      generator.generateFromMetaschema(metaschema, new OutputStreamWriter(out));
    }
  }

  @Test
  void testAllowedValues() throws IOException, MetaschemaException {
    Path metaschemaPath = UNIT_TEST_PATH.resolve("allowed-values/allowed-values-basic_metaschema.xml");

    Path schema = generateSchema(metaschemaPath, "allowed-values/allowed-values-basic_schema.json");

    assertTrue(validateSchema(schema));
    // TODO: diff schema
    assertFalse(
        validateData(UNIT_TEST_PATH.resolve("allowed-values/allowed-values-basic_test_baddates_FAIL.json"), schema));
    assertFalse(
        validateData(UNIT_TEST_PATH.resolve("allowed-values/allowed-values-basic_test_badvalues_FAIL.json"), schema));
    assertTrue(
        validateData(UNIT_TEST_PATH.resolve("allowed-values/allowed-values-basic_test_valid_PASS.json"), schema));
  }

  @Test
  void testCharacterHandling() throws IOException, MetaschemaException {
    Path metaschemaPath = UNIT_TEST_PATH.resolve("char-handling/charstrings_metaschema.xml");

    Path schema = generateSchema(metaschemaPath, "char-handling/charstrings_schema.json");

    assertTrue(validateSchema(schema));
    // TODO: diff schema
    assertTrue(
        validateData(UNIT_TEST_PATH.resolve("char-handling/charstrings_test_okay_PASS.json"), schema));
  }

  @Nested
  class Choice {
    @Test
    void multipleChoice() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("choice/choice-multiple_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "choice/choice-multiple_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("choice/choice-multiple_test_multiple_PASS.json"), schema));
    }
  }

  @Nested
  class Collapse {
    @Test
    void testCollapsibleNoOp() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("collapsible/collapsible-no-op_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "collapsible/collapsible-no-op_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
    }

    @Test
    void testCollapsible() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("collapsible/collapsible_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "collapsible/collapsible_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("collapsible/collapsible_test_multiple_PASS.json"), schema));
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("collapsible/collapsible_test_singleton_PASS.json"), schema));
    }
  }

  @Nested
  class Datatypes {
    @Test
    void testDataTypeDate() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("datatypes/datatypes-date_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "datatypes/datatypes-date_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
      assertFalse(
          validateData(UNIT_TEST_PATH.resolve("datatypes/datatypes-date_test_tricky_FAIL.json"), schema));
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("datatypes/datatypes-date_test_valid_PASS.json"), schema));
    }

    @Test
    void testDataTypeDateTime() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("datatypes/datatypes-datetime-no-tz_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "datatypes/datatypes-datetime-no-tz_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("datatypes/datatypes-datetime-no-tz_test_valid_PASS.json"), schema));
    }

    @Test
    void testDataTypeDateTimeWithTimezone() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("datatypes/datatypes-datetime_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "datatypes/datatypes-datetime_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("datatypes/datatypes-datetime_test_valid_PASS.json"), schema));
    }

    @Test
    void testDataTypeProse() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("datatypes/datatypes-prose_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "datatypes/datatypes-prose_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("datatypes/datatypes-prose_test_valid_PASS.json"), schema));
      assertFalse(
          validateData(UNIT_TEST_PATH.resolve("datatypes/datatypes-prose_test_bad-date_FAIL.json"), schema));
    }

    @Test
    void testDataTypeToken() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("datatypes/datatypes-token_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "datatypes/datatypes-token_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("datatypes/datatypes-token_test_valid_PASS.json"), schema));
      assertFalse(
          validateData(UNIT_TEST_PATH.resolve("datatypes/datatypes-token_test_invalid_FAIL.json"), schema));
    }

    @Test
    void testDataTypeUri() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("datatypes/datatypes-uri_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "datatypes/datatypes-uri_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("datatypes/datatypes-uri_test_valid_PASS.json"), schema));
      assertFalse(
          validateData(UNIT_TEST_PATH.resolve("datatypes/datatypes-uri_test_broken_FAIL.json"), schema));
    }

    @Test
    void testDataTypeUuid() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("datatypes/datatypes-uuid_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "datatypes/datatypes-uuid_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("datatypes/datatypes-uuid_test_valid_PASS.json"), schema));
      assertFalse(
          validateData(UNIT_TEST_PATH.resolve("datatypes/datatypes-uuid_test_version-1-invalid_FAIL.json"), schema));
    }
  }

  @Nested
  class Flag {
    @Test
    void basic() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("flag/flag-basic_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "flag/flag-basic_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("flag/flag-basic_test_simple_PASS.json"), schema));
      assertFalse(
          validateData(UNIT_TEST_PATH.resolve("flag/flag-basic_test_datatype_FAIL.json"), schema));
    }

    @Test
    void override() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("flag/flag-override_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "flag/flag-override_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
    }
  }

  @Nested
  class GroupAs {
    @Test
    void misc() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("group-as/group-as-misc_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "group-as/group-as-misc_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
    }

    @Nested
    class Array {
      @Test
      void basic() throws IOException, MetaschemaException {
        Path metaschemaPath = UNIT_TEST_PATH.resolve("group-as/group-as-array_metaschema.xml");

        Path schema = generateSchema(metaschemaPath, "group-as/group-as-array_schema.json");

        assertTrue(validateSchema(schema));
        // TODO: diff schema
        assertTrue(
            validateData(UNIT_TEST_PATH.resolve("group-as/group-as-array_test_valid_PASS.json"), schema));
        assertFalse(
            validateData(UNIT_TEST_PATH.resolve("group-as/group-as-array_test_singleton_FAIL.json"), schema));
      }

      @Test
      void bounded() throws IOException, MetaschemaException {
        Path metaschemaPath = UNIT_TEST_PATH.resolve("group-as/group-as-array-bounded_metaschema.xml");

        Path schema = generateSchema(metaschemaPath, "group-as/group-as-array-bounded_schema.json");

        assertTrue(validateSchema(schema));
        // TODO: diff schema
        assertTrue(
            validateData(UNIT_TEST_PATH.resolve("group-as/group-as-array-bounded_test_array-inside_PASS.json"),
                schema));
        assertFalse(
            validateData(UNIT_TEST_PATH.resolve("group-as/group-as-array-bounded_test_array-singleton_FAIL.json"),
                schema));
        assertFalse(
            validateData(UNIT_TEST_PATH.resolve("group-as/group-as-array-bounded_test_array-upper_FAIL.json"), schema));
      }

      // TODO: invalid max

      @Test
      void optional() throws IOException, MetaschemaException {
        Path metaschemaPath = UNIT_TEST_PATH.resolve("group-as/group-as-array-optional_metaschema.xml");

        Path schema = generateSchema(metaschemaPath, "group-as/group-as-array-optional_schema.json");

        assertTrue(validateSchema(schema));
        // TODO: diff schema
        assertTrue(
            validateData(UNIT_TEST_PATH.resolve("group-as/group-as-array-optional_test_valid_PASS.json"), schema));
        assertFalse(
            validateData(UNIT_TEST_PATH.resolve("group-as/group-as-array-optional_test_empty_FAIL.json"), schema));
      }
    }

    @Nested
    class SingletonOrArray {
      @Test
      void basic() throws IOException, MetaschemaException {
        Path metaschemaPath = UNIT_TEST_PATH.resolve("group-as/group-as-singleton-or-array_metaschema.xml");

        Path schema = generateSchema(metaschemaPath, "group-as/group-as-singleton-or-array_schema.json");

        assertTrue(validateSchema(schema));
        // TODO: diff schema
        assertTrue(
            validateData(UNIT_TEST_PATH.resolve("group-as/group-as-singleton-or-array_test_singleton_PASS.json"),
                schema));
        assertTrue(
            validateData(UNIT_TEST_PATH.resolve("group-as/group-as-singleton-or-array_test_valid-array_PASS.json"),
                schema));
      }

      @Test
      void optional() throws IOException, MetaschemaException {
        Path metaschemaPath = UNIT_TEST_PATH.resolve("group-as/group-as-singleton-or-array-optional_metaschema.xml");

        Path schema = generateSchema(metaschemaPath, "group-as/group-as-singleton-or-array-optional_schema.json");

        assertTrue(validateSchema(schema));
        // TODO: diff schema

        assertTrue(
            validateData(UNIT_TEST_PATH
                .resolve("group-as/group-as-singleton-or-array-optional_test_valid-singleton_PASS.json"), schema));
        assertTrue(
            validateData(
                UNIT_TEST_PATH.resolve("group-as/group-as-singleton-or-array-optional_test_valid-array_PASS.json"),
                schema));
        assertFalse(
            validateData(
                UNIT_TEST_PATH.resolve(
                    "group-as/group-as-singleton-or-array-optional_test_invalid-array-singleton_FAIL.json"),
                schema));
        assertFalse(
            validateData(
                UNIT_TEST_PATH.resolve("group-as/group-as-singleton-or-array-optional_test_empty_FAIL.json"),
                schema));
      }
    }

    @Nested
    class ByKey {
      @Test
      void basic() throws IOException, MetaschemaException {
        Path metaschemaPath = UNIT_TEST_PATH.resolve("group-as/group-as-by-key_metaschema.xml");

        Path schema = generateSchema(metaschemaPath, "group-as/group-as-by-key_schema.json");

        assertTrue(validateSchema(schema));
        // TODO: diff schema
        assertTrue(
            validateData(UNIT_TEST_PATH.resolve("group-as/group-as-by-key_test_valid_PASS.json"), schema));
        assertFalse(
            validateData(UNIT_TEST_PATH.resolve("group-as/group-as-by-key_test_invalid-child_FAIL.json"), schema));
      }
    }
  }

  @Nested
  class ValueKey {
    @Test
    void flag() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("json-value-key/json-value-key-field_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "json-value-key/json-value-key-field_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("json-value-key/json-value-key-field_test_valid_PASS.json"), schema));
    }

    @Test
    void label() throws IOException, MetaschemaException {
      Path metaschemaPath = UNIT_TEST_PATH.resolve("json-value-key/json-value-key-label_metaschema.xml");

      Path schema = generateSchema(metaschemaPath, "json-value-key/json-value-key-label_schema.json");

      assertTrue(validateSchema(schema));
      // TODO: diff schema
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("json-value-key/json-value-key-label_test_valid_PASS.json"), schema));
      assertTrue(
          validateData(UNIT_TEST_PATH.resolve("json-value-key/json-value-key-label_test_valid2_PASS.json"), schema));
    }
  }
}
