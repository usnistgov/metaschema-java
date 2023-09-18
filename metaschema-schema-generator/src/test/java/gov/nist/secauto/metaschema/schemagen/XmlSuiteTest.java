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

import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.configuration.DefaultConfiguration;
import gov.nist.secauto.metaschema.model.common.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.model.common.validation.IContentValidator;
import gov.nist.secauto.metaschema.model.common.validation.XmlSchemaContentValidator;
import gov.nist.secauto.metaschema.schemagen.xml.XmlSchemaGenerator;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.StAXEventBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

class XmlSuiteTest
    extends AbstractSchemaGeneratorTestSuite {
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
  protected Supplier<IContentValidator> getSchemaValidatorSupplier() {
    return null;
    // return () -> SCHEMA_VALIDATOR;
  }

  @Override
  protected Format getRequiredContentFormat() {
    return Format.XML;
  }

  @Override
  protected Function<Path, XmlSchemaContentValidator> getContentValidatorSupplier() {
    return XML_CONTENT_VALIDATOR_PROVIDER;
  }

  @Override
  protected BiFunction<IMetaschema, Writer, Void> getGeneratorSupplier() {
    return XML_SCHEMA_PROVIDER;
  }

  @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
  @Execution(ExecutionMode.SAME_THREAD)
  @DisplayName("XML Schema Generation")
  @TestFactory
  Stream<? extends DynamicNode> generateTests() {
    return testFactory();
  }

  @Disabled
  @Test
  void testChoiceMultiple() throws IOException, MetaschemaException { // NOPMD - delegated to doTest
    doTest(
        "choice/",
        "choice-multiple_metaschema.xml",
        "choice-schema",
        contentCase(Format.JSON, "choice-multiple_test_multiple_PASS.json", true));
  }

  @Disabled
  @Test
  void testCollapsibleMultiple() throws IOException, MetaschemaException { // NOPMD - delegated to doTest
    doTest(
        "collapsible/",
        "collapsible_metaschema.xml",
        "collapsible-schema",
        contentCase(Format.JSON, "collapsible_test_multiple_PASS.json", true),
        contentCase(Format.JSON, "collapsible_test_singleton_PASS.json", true));
  }

  @Disabled
  @Test
  void testJsonValueKeyField() throws IOException, MetaschemaException { // NOPMD - delegated to doTest
    doTest(
        "json-value-key/",
        "json-value-key-field_metaschema.xml",
        "json-value-key-field-schema",
        contentCase(Format.JSON, "json-value-key-field_test_valid_PASS.json", true));
  }

  @Disabled
  @Test
  void testJsonValueKeyLabel() throws IOException, MetaschemaException { // NOPMD - delegated to doTest
    doTest(
        "json-value-key/",
        "json-value-key-field_metaschema.xml",
        "json-value-key-field-schema",
        contentCase(Format.JSON, "json-value-key-field_test_valid_PASS.json", true));
  }

  @Disabled
  @Test
  void testByKey() throws IOException, MetaschemaException { // NOPMD - delegated to doTest
    doTest(
        "group-as/",
        "group-as-by-key_metaschema.xml",
        "group-as-by-key-schema",
        contentCase(Format.JSON, "group-as-by-key_test_valid_PASS.json", true));
  }

  @Disabled
  @Test
  void testAllowedValues() throws IOException, MetaschemaException { // NOPMD - delegated to doTest
    doTest(
        "allowed-values",
        "allowed-values-basic_metaschema.xml",
        "allowed-values-basic-schema",
        // contentCase(Format.JSON, "allowed-values-basic_test_baddates_FAIL.json", false),
        // contentCase(Format.JSON, "allowed-values-basic_test_badvalues_FAIL.json", false),
        contentCase(Format.XML, "allowed-values-basic_test_valid_FAIL.xml", false),
        // contentCase(Format.JSON, "allowed-values-basic_test_valid_PASS.json", true),
        contentCase(Format.XML, "allowed-values-basic_test_valid_PASS.xml", true));
  }

  @Disabled
  @Test
  void testOSCALComplete() throws IOException, MetaschemaException { // NOPMD - delegated to doTest
    MetaschemaLoader loader = new MetaschemaLoader();
    loader.allowEntityResolution();

    IMetaschema metaschema = loader.load(new URL(
        // "https://raw.githubusercontent.com/usnistgov/OSCAL/develop/src/metaschema/oscal_complete_metaschema.xml"));
        "https://raw.githubusercontent.com/usnistgov/OSCAL/develop/src/metaschema/oscal_complete_metaschema.xml"));
    ISchemaGenerator schemaGenerator = new XmlSchemaGenerator();
    IMutableConfiguration<SchemaGenerationFeature<?>> features = new DefaultConfiguration<>();
    features.disableFeature(SchemaGenerationFeature.INLINE_DEFINITIONS);
    try (Writer writer = Files.newBufferedWriter(
        Path.of("target/oscal-complete_schema.xsd"),
        StandardCharsets.UTF_8,
        getWriteOpenOptions())) {
      assert writer != null;
      schemaGenerator.generateFromMetaschema(metaschema, writer, features);
    }
  }

  @Test
  void testLiboscalJavaIssue181() throws IOException, MetaschemaException, XMLStreamException, JDOMException {
    MetaschemaLoader loader = new MetaschemaLoader();
    loader.allowEntityResolution();

    IMetaschema module = loader.load(new URL(
        "https://raw.githubusercontent.com/usnistgov/OSCAL/v1.1.1/src/metaschema/oscal_catalog_metaschema.xml"));
    ISchemaGenerator schemaGenerator = new XmlSchemaGenerator();
    IMutableConfiguration<SchemaGenerationFeature<?>> features = new DefaultConfiguration<>();
    features.enableFeature(SchemaGenerationFeature.INLINE_DEFINITIONS);
    features.disableFeature(SchemaGenerationFeature.INLINE_CHOICE_DEFINITIONS);

    Path schemaPath = Path.of("target/oscal-catalog_schema.xsd");
    try (Writer writer = Files.newBufferedWriter(schemaPath, StandardCharsets.UTF_8, getWriteOpenOptions())) {
      assert writer != null;
      schemaGenerator.generateFromMetaschema(module, writer, features);
    }

    // check for missing attribute types per liboscal-java#181
    XMLInputFactory factory = XMLInputFactory.newFactory();
    try (Reader fileReader = new FileReader(schemaPath.toFile())) {
      XMLEventReader reader = factory.createXMLEventReader(fileReader);
      StAXEventBuilder builder = new StAXEventBuilder();
      Document document = document = builder.build(reader);

      XPathExpression<Element> xpath = XPathFactory.instance()
          .compile("//xs:attribute[not(@type or xs:simpleType)]",
              Filters.element(),
              null,
              Namespace.getNamespace("xs", "http://www.w3.org/2001/XMLSchema"));
      List<Element> result = xpath.evaluate(document);

      assertTrue(result.isEmpty());
    }
  }
}
