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

package gov.nist.secauto.metaschema.model.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.binding.io.ISerializer;
import gov.nist.secauto.metaschema.codegen.Production;
import gov.nist.secauto.metaschema.codegen.compile.MetaschemaCompilerHelper;
import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.validation.IContentValidator;
import gov.nist.secauto.metaschema.model.common.validation.IValidationFinding;
import gov.nist.secauto.metaschema.model.common.validation.IValidationResult;
import gov.nist.secauto.metaschema.model.common.validation.JsonSchemaContentValidator.JsonValidationFinding;
import gov.nist.secauto.metaschema.model.testing.xmlbeans.CollectionDocument.Collection;
import gov.nist.secauto.metaschema.model.testing.xmlbeans.ContentCaseDocument.ContentCase;
import gov.nist.secauto.metaschema.model.testing.xmlbeans.GenerateDocument.Generate;
import gov.nist.secauto.metaschema.model.testing.xmlbeans.MetaschemaDocument.Metaschema;
import gov.nist.secauto.metaschema.model.testing.xmlbeans.ScenarioDocument.Scenario;
import gov.nist.secauto.metaschema.model.testing.xmlbeans.TestSuiteDocument;

import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.platform.commons.JUnitException;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractTestSuite {
  private static final Logger LOGGER = LogManager.getLogger(AbstractTestSuite.class);
  private static final MetaschemaLoader LOADER = new MetaschemaLoader();

  protected abstract Format getRequiredContentFormat();

  protected abstract URI getTestSuiteURI();

  protected abstract Path getGenerationPath();

  protected abstract BiFunction<IMetaschema, Writer, Void> getGeneratorSupplier();

  protected abstract Supplier<IContentValidator> getSchemaValidatorSupplier();

  protected abstract Function<Path, IContentValidator> getContentValidatorSupplier();

  protected Stream<? extends DynamicNode> testFactory() {
    try {
      return generateTests();
    } catch (XmlException | IOException ex) {
      throw new JUnitException("Unable to generate tests", ex);
    }
  }

  private Stream<? extends DynamicNode> generateTests() throws XmlException, IOException {
    URI testSuiteUri = getTestSuiteURI();
    URL testSuiteUrl = testSuiteUri.toURL();

    XmlOptions options = new XmlOptions();
    options.setBaseURI(null);
    options.setLoadLineNumbers();
    TestSuiteDocument directive = TestSuiteDocument.Factory.parse(testSuiteUrl, options);

    Path generationPath = getGenerationPath();
    return directive.getTestSuite().getCollectionList().stream()
        .flatMap(collection -> Stream.of(generateCollection(collection, testSuiteUri, generationPath)));
  }

  private DynamicContainer generateCollection(@NotNull Collection collection, @NotNull URI testSuiteUri,
      @NotNull Path generationPath) {
    URI collectionUri = testSuiteUri.resolve(collection.getLocation());

    LOGGER.atInfo().log("Collection: " + collectionUri);
    Path collectionGenerationPath;
    try {
      collectionGenerationPath = Files.createTempDirectory(generationPath, "collection-");
    } catch (IOException ex) {
      throw new JUnitException("Unable to create collection temp directory", ex);
    }

    return DynamicContainer.dynamicContainer(
        collection.getName(),
        testSuiteUri,
        collection.getScenarioList().stream()
            .flatMap(scenario -> {
              return Stream.of(generateScenario(scenario, collectionUri, collectionGenerationPath));
            })
            .sequential());
  }

  protected void produceSchema(@NotNull IMetaschema metaschema, @NotNull Path schemaPath) throws IOException {
    try (Writer writer = Files.newBufferedWriter(schemaPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      getGeneratorSupplier().apply(metaschema, writer);
      writer.flush();
    }
  }

  protected DynamicBindingContext produceDynamicBindingContext(@NotNull IMetaschema metaschema,
      @NotNull Path classDirPath) throws IOException {
    Production production = MetaschemaCompilerHelper.compileMetaschema(metaschema, classDirPath);
    return new DynamicBindingContext(production, MetaschemaCompilerHelper.getClassLoader(classDirPath));
  }

  private DynamicContainer generateScenario(@NotNull Scenario scenario, @NotNull URI collectionUri,
      @NotNull Path collectionGenerationPath) {
    Path scenarioGenerationPath;
    try {
      scenarioGenerationPath = Files.createTempDirectory(collectionGenerationPath, "scenario-");
    } catch (IOException ex) {
      throw new JUnitException("Unable to create scenario temp directory", ex);
    }

    try {
      // create the directories the schema will be stored in
      Files.createDirectories(scenarioGenerationPath);
    } catch (IOException ex) {
      throw new JUnitException("Unable to create test directories for path: " + scenarioGenerationPath, ex);
    }

    Generate generate = scenario.getGenerate();
    Metaschema metaschemaDirective = generate.getMetaschema();
    URI metaschemaUri = collectionUri.resolve(metaschemaDirective.getLocation());

    LOGGER.atInfo().log("Metaschema: " + metaschemaUri);

    Path schemaPath;
    // determine what file to use for the schema
    try {
      schemaPath = Files.createTempFile(scenarioGenerationPath, "", "-schema");
    } catch (IOException ex) {
      throw new JUnitException("Unable to create schema temp file", ex);
    }

    IMetaschema metaschema;
    try {
      metaschema = LOADER.loadXmlMetaschema(metaschemaUri.toURL());
      produceSchema(metaschema, schemaPath);
    } catch (IOException | MetaschemaException ex) {
      throw new JUnitException("Unable to generate schema for Metaschema: " + metaschemaUri, ex);
    }

    // build a test container for the generate and validate steps
    DynamicTest validateSchema = DynamicTest.dynamicTest(
        "Validate Schema",
        schemaPath.toUri(),
        () -> validate(getSchemaValidatorSupplier().get(), schemaPath));

    Path classDir;
    try {
      classDir = Files.createTempDirectory(scenarioGenerationPath, "-classes");
    } catch (IOException ex) {
      throw new JUnitException("Unable to create schema temp file", ex);
    }

    DynamicBindingContext context;
    try {
      context = produceDynamicBindingContext(metaschema, classDir);
    } catch (IOException ex) {
      throw new JUnitException("Unable to generate classes", ex);
    }

    Stream<? extends DynamicNode> contentTests;
    {
      IContentValidator schemaValidator = getContentValidatorSupplier().apply(schemaPath);
      contentTests = scenario.getContentCaseList().stream()
          .flatMap(contentCase -> {
            DynamicTest test
                = generateContentCase(contentCase, context, schemaValidator, collectionUri, scenarioGenerationPath);
            return test == null ? Stream.empty() : Stream.of(test);
          }).sequential();
    }

    return DynamicContainer.dynamicContainer(
        scenario.getName(),
        metaschemaUri,
        Stream.concat(Stream.of(validateSchema), contentTests).sequential());
  }

  @SuppressWarnings("unchecked")
  private DynamicTest generateContentCase(@NotNull ContentCase contentCase, @NotNull DynamicBindingContext context,
      @NotNull IContentValidator schemaValidator, URI collectionUri,
      @NotNull Path generationPath) {
    DynamicTest retval;

    URI contentUri = collectionUri.resolve(contentCase.getLocation());

    Format format = contentCase.getSourceFormat();
    if (getRequiredContentFormat().equals(format)) {
      retval = DynamicTest.dynamicTest(
          String.format("Validate %s=%s: %s", format, contentCase.getExpectedValidationResult(),
              contentCase.getLocation()),
          contentUri,
          () -> {
            assertEquals(contentCase.getExpectedValidationResult(), validate(schemaValidator, contentUri.toURL()),
                "validation did not match expectation");
          });
    } else if (contentCase.getExpectedValidationResult()) {
      retval = DynamicTest.dynamicTest(
          String.format("Convert and Validate %s=%s: %s", format, contentCase.getExpectedValidationResult(),
              contentCase.getLocation()),
          contentUri,
          () -> {

            Object object = context.newBoundLoader().load(contentUri.toURL());

            Path convertedContetPath;
            try {
              convertedContetPath = Files.createTempFile(generationPath, "", "-content");
            } catch (IOException ex) {
              throw new JUnitException("Unable to create schema temp file", ex);
            }
            @SuppressWarnings("rawtypes")
            ISerializer serializer = context.newSerializer(getRequiredContentFormat(), object.getClass());
            serializer.serialize(object, convertedContetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            assertEquals(contentCase.getExpectedValidationResult(),
                validate(schemaValidator, convertedContetPath.toUri().toURL()),
                "validation did not match expectation");
          });
    } else {
      retval = null;
    }
    return retval;
  }

  private static boolean validate(@NotNull IContentValidator validator, @NotNull URL target) throws IOException {
    IValidationResult schemaValidationResult;
    try {
      schemaValidationResult = validator.validate(target);
    } catch (URISyntaxException ex) {
      throw new IOException(ex);
    }
    return processValidationResult(schemaValidationResult);
  }

  protected static boolean validate(@NotNull IContentValidator validator, @NotNull Path target) throws IOException {
    IValidationResult schemaValidationResult = validator.validate(target);
    return processValidationResult(schemaValidationResult);
  }

  private static boolean processValidationResult(IValidationResult schemaValidationResult) {
    for (IValidationFinding finding : schemaValidationResult.getFindings()) {
      logFinding(finding);
    }
    return schemaValidationResult.isPassing();
  }

  private static void logFinding(@NotNull IValidationFinding finding) {
    LogBuilder logBuilder;
    switch (finding.getSeverity()) {
    case CRITICAL:
      logBuilder = LOGGER.atFatal();
      break;
    case ERROR:
      logBuilder = LOGGER.atError();
      break;
    case WARNING:
      logBuilder = LOGGER.atWarn();
      break;
    case INFORMATIONAL:
      logBuilder = LOGGER.atInfo();
      break;
    default:
      throw new IllegalArgumentException("Unknown level: " + finding.getSeverity().name());
    }

    if (finding.getCause() != null) {
      logBuilder.withThrowable(finding.getCause());
    }

    if (finding instanceof JsonValidationFinding) {
      JsonValidationFinding jsonFinding = (JsonValidationFinding) finding;
      logBuilder.log("[{}] {}", jsonFinding.getCause().getPointerToViolation(), finding.getMessage());
    } else {
      logBuilder.log("{}", finding.getMessage());
    }
  }
}
