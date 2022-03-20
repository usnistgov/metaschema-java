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

package gov.nist.secauto.metaschema.schemagen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.validation.IContentValidator;
import gov.nist.secauto.metaschema.model.common.validation.XmlSchemaContentValidator;
import gov.nist.secauto.metaschema.model.testing.AbstractTestSuite;
import gov.nist.secauto.metaschema.model.testing.DynamicBindingContext;
import gov.nist.secauto.metaschema.schemagen.ISchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.JsonSchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.XmlSchemaGenerator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.commons.JUnitException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class XmlSuiteTest
    extends AbstractTestSuite {
  private static final ISchemaGenerator GENERATOR = new XmlSchemaGenerator();
  // private static final XmlSchemaContentValidator SCHEMA_VALIDATOR;

  // static {
  // URL schemaResource = MetaschemaLoader.class.getResource("/schema/xml/XMLSchema.xsd");
  // try {
  // List<? extends Source> schemaSources = Collections.singletonList(
  // new StreamSource(schemaResource.openStream(), schemaResource.toString()));
  // SCHEMA_VALIDATOR = new XmlSchemaContentValidator(schemaSources);
  // } catch (SAXException | IOException ex) {
  // throw new IllegalStateException(ex);
  // }
  // }

  @Override
  protected URI getTestSuiteURI() {
    return Paths.get("../metaschema-model/metaschema/test-suite/schema-generation/unit-tests.xml").toUri();
  }

  @Override
  protected Path getGenerationPath() {
    return Paths.get("test-schemagen");
  }

  @Override
  protected Supplier<IContentValidator> getSchemaValidatorSupplier() {
    return null;
    // return () -> SCHEMA_VALIDATOR;
  }

  @Override
  protected Format getRequiredContentFormat() {
    return Format.XML;
  }

  @Override
  protected Function<Path, IContentValidator> getContentValidatorSupplier() {
    return (path) -> {
      try {
        URL schemaResource = path.toUri().toURL();
        List<? extends Source> schemaSources = Collections.singletonList(
            new StreamSource(schemaResource.openStream(), schemaResource.toString()));
        return new XmlSchemaContentValidator(schemaSources);
      } catch (IOException | SAXException ex) {
        throw new IllegalStateException(ex);
      }
    };
  }

  @Override
  protected BiFunction<IMetaschema, Writer, Void> getGeneratorSupplier() {
    // TODO Auto-generated method stub
    return (metaschema, writer) -> {
      try {
        GENERATOR.generateFromMetaschema(metaschema, writer);
      } catch (IOException ex) {
        throw new JUnitException("IO error", ex);
      }
      return null;
    };
  }

  @Execution(ExecutionMode.CONCURRENT)
  @DisplayName("XML Schema Generation")
  @TestFactory
  public Stream<? extends DynamicNode> generateTests() {
    return testFactory();
  }

  @Disabled
  @Test
  void testAllowedValuesBasic() throws IOException, MetaschemaException {
    Path generationDir = getGenerationPath();

    Path testSuite = Paths.get("../metaschema-model/metaschema/test-suite/schema-generation/");
    MetaschemaLoader loader = new MetaschemaLoader();
    // Path metaschemaPath = Paths.get("../../OSCAL/src/metaschema/oscal_catalog_metaschema.xml");
    Path metaschemaPath = testSuite.resolve("allowed-values/allowed-values-basic_metaschema.xml");
    IMetaschema metaschema = loader.loadXmlMetaschema(metaschemaPath);

    Path schemaPath = generationDir.resolve("test-schema.out");
    produceSchema(metaschema, schemaPath);

    Path contentPath = testSuite.resolve("allowed-values/allowed-values-basic_test_valid_PASS.json");

    DynamicBindingContext context = produceDynamicBindingContext(metaschema, generationDir);
    contentPath = convertContent(contentPath.toUri(), generationDir, context);

    assertEquals(true,
        validate(getContentValidatorSupplier().apply(schemaPath), contentPath),
        "validation did not match expectation");

  }

  @Disabled
  @Test
  void testChoiceMultiple() throws IOException, MetaschemaException {
    Path generationDir = getGenerationPath();

    Path testSuite = Paths.get("../metaschema-model/metaschema/test-suite/schema-generation/");
    MetaschemaLoader loader = new MetaschemaLoader();
    // Path metaschemaPath = Paths.get("../../OSCAL/src/metaschema/oscal_catalog_metaschema.xml");
    Path metaschemaPath = testSuite.resolve("choice/choice-multiple_metaschema.xml");
    IMetaschema metaschema = loader.loadXmlMetaschema(metaschemaPath);

    Path schemaPath = generationDir.resolve("choice-schema.xsd");
    produceSchema(metaschema, schemaPath);
    produceSchema(metaschema, generationDir.resolve("choice-schema.json"), (ms, writer) -> {
      try {
        new JsonSchemaGenerator().generateFromMetaschema(ms, writer);
      } catch (IOException ex) {
        throw new JUnitException("IO error", ex);
      }
      return null;
    });

    Path contentPath = testSuite.resolve("choice/choice-multiple_test_multiple_PASS.json");

    DynamicBindingContext context = produceDynamicBindingContext(metaschema, generationDir);
    contentPath = convertContent(contentPath.toUri(), generationDir, context);

    assertEquals(true,
        validate(getContentValidatorSupplier().apply(schemaPath), contentPath),
        "validation did not match expectation");

  }

  @Disabled
  @Test
  void testCollapsibleMultiple() throws IOException, MetaschemaException {
    Path generationDir = getGenerationPath();

    Path testSuite = Paths.get("../metaschema-model/metaschema/test-suite/schema-generation/");
    MetaschemaLoader loader = new MetaschemaLoader();
    // Path metaschemaPath = Paths.get("../../OSCAL/src/metaschema/oscal_catalog_metaschema.xml");
    Path metaschemaPath = testSuite.resolve("collapsible/collapsible_metaschema.xml");
    IMetaschema metaschema = loader.loadXmlMetaschema(metaschemaPath);

    Path schemaPath = generationDir.resolve("collapsible-schema.xsd");
    produceSchema(metaschema, schemaPath);
    produceSchema(metaschema, generationDir.resolve("collapsible-schema.json"), (ms, writer) -> {
      try {
        new JsonSchemaGenerator().generateFromMetaschema(ms, writer);
      } catch (IOException ex) {
        throw new JUnitException("IO error", ex);
      }
      return null;
    });

    Path contentPath = testSuite.resolve("collapsible/collapsible_test_multiple_PASS.json");

    DynamicBindingContext context = produceDynamicBindingContext(metaschema, generationDir);
    contentPath = convertContent(contentPath.toUri(), generationDir, context);

    assertEquals(true,
        validate(getContentValidatorSupplier().apply(schemaPath), contentPath),
        String.format("validation of '%s' did not match expectation", contentPath));

  }

  @Disabled
  @Test
  void testDatatypeProse() throws IOException, MetaschemaException {
    Path generationDir = getGenerationPath();

    Path testSuite = Paths.get("../metaschema-model/metaschema/test-suite/schema-generation/");
    MetaschemaLoader loader = new MetaschemaLoader();
    // Path metaschemaPath = Paths.get("../../OSCAL/src/metaschema/oscal_catalog_metaschema.xml");
    Path metaschemaPath = testSuite.resolve("datatypes/datatypes-prose_metaschema.xml");
    IMetaschema metaschema = loader.loadXmlMetaschema(metaschemaPath);

    Path schemaPath = generationDir.resolve("datatypes-prose-schema.xsd");
    produceSchema(metaschema, schemaPath);
    produceSchema(metaschema, generationDir.resolve("datatypes-prose-schema.json"), (ms, writer) -> {
      try {
        new JsonSchemaGenerator().generateFromMetaschema(ms, writer);
      } catch (IOException ex) {
        throw new JUnitException("IO error", ex);
      }
      return null;
    });

    Path contentPath = testSuite.resolve("datatypes/datatypes-prose_test_valid_PASS.json");

    DynamicBindingContext context = produceDynamicBindingContext(metaschema, generationDir);
    contentPath = convertContent(contentPath.toUri(), generationDir, context);

    assertEquals(true,
        validate(getContentValidatorSupplier().apply(schemaPath), contentPath),
        String.format("validation of '%s' did not match expectation", contentPath));

  }

  @Disabled
  @Test
  void testDatatypeDateTime() throws IOException, MetaschemaException {
    Path generationDir = getGenerationPath();

    Path testSuite = Paths.get("../metaschema-model/metaschema/test-suite/schema-generation/");
    MetaschemaLoader loader = new MetaschemaLoader();
    // Path metaschemaPath = Paths.get("../../OSCAL/src/metaschema/oscal_catalog_metaschema.xml");
    Path metaschemaPath = testSuite.resolve("datatypes/datatypes-datetime-no-tz_metaschema.xml");
    IMetaschema metaschema = loader.loadXmlMetaschema(metaschemaPath);

    Path schemaPath = generationDir.resolve("datatypes-datetime-no-tz-schema.xsd");
    produceSchema(metaschema, schemaPath);
    produceSchema(metaschema, generationDir.resolve("datatypes-datetime-no-tz-schema.json"), (ms, writer) -> {
      try {
        new JsonSchemaGenerator().generateFromMetaschema(ms, writer);
      } catch (IOException ex) {
        throw new JUnitException("IO error", ex);
      }
      return null;
    });

    Path contentPath = testSuite.resolve("datatypes/datatypes-datetime-no-tz_test_valid_PASS.json");

    DynamicBindingContext context = produceDynamicBindingContext(metaschema, generationDir);
    contentPath = convertContent(contentPath.toUri(), generationDir, context);

    assertEquals(true,
        validate(getContentValidatorSupplier().apply(schemaPath), contentPath),
        String.format("validation of '%s' did not match expectation", contentPath));

  }

  @Disabled
  @Test
  void testGroupAsByKey() throws IOException, MetaschemaException {
    Path generationDir = getGenerationPath();

    Path testSuite = Paths.get("../metaschema-model/metaschema/test-suite/schema-generation/");
    MetaschemaLoader loader = new MetaschemaLoader();
    // Path metaschemaPath = Paths.get("../../OSCAL/src/metaschema/oscal_catalog_metaschema.xml");
    Path metaschemaPath = testSuite.resolve("group-as/group-as-by-key_metaschema.xml");
    IMetaschema metaschema = loader.loadXmlMetaschema(metaschemaPath);

    Path schemaPath = generationDir.resolve("group-as-by-key-schema.xsd");
    produceSchema(metaschema, schemaPath);
    produceSchema(metaschema, generationDir.resolve("group-as-by-key-schema.json"), (ms, writer) -> {
      try {
        new JsonSchemaGenerator().generateFromMetaschema(ms, writer);
      } catch (IOException ex) {
        throw new JUnitException("IO error", ex);
      }
      return null;
    });

    Path contentPath = testSuite.resolve("group-as/group-as-by-key_test_valid_PASS.json");

    DynamicBindingContext context = produceDynamicBindingContext(metaschema, generationDir);
    contentPath = convertContent(contentPath.toUri(), generationDir, context);

    assertEquals(true,
        validate(getContentValidatorSupplier().apply(schemaPath), contentPath),
        String.format("validation of '%s' did not match expectation", contentPath));

  }

  @Test
  void testJsonValueKeyField() throws IOException, MetaschemaException {
    Path generationDir = getGenerationPath();

    Path testSuite = Paths.get("../metaschema-model/metaschema/test-suite/schema-generation/");
    MetaschemaLoader loader = new MetaschemaLoader();
    // Path metaschemaPath = Paths.get("../../OSCAL/src/metaschema/oscal_catalog_metaschema.xml");
    Path metaschemaPath = testSuite.resolve("json-value-key/json-value-key-field_metaschema.xml");
    IMetaschema metaschema = loader.loadXmlMetaschema(metaschemaPath);

    Path schemaPath = generationDir.resolve("json-value-key-field-schema.xsd");
    produceSchema(metaschema, schemaPath);
    produceSchema(metaschema, generationDir.resolve("json-value-key-field-schema.json"), (ms, writer) -> {
      try {
        new JsonSchemaGenerator().generateFromMetaschema(ms, writer);
      } catch (IOException ex) {
        throw new JUnitException("IO error", ex);
      }
      return null;
    });

    Path contentPath = testSuite.resolve("json-value-key/json-value-key-field_test_valid_PASS.json");

    DynamicBindingContext context = produceDynamicBindingContext(metaschema, generationDir);
    contentPath = convertContent(contentPath.toUri(), generationDir, context);

    assertEquals(true,
        validate(getContentValidatorSupplier().apply(schemaPath), contentPath),
        String.format("validation of '%s' did not match expectation", contentPath));

  }

}
