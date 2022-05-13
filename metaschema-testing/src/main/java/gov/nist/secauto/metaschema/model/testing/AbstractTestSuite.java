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
import gov.nist.secauto.metaschema.codegen.IProduction;
import gov.nist.secauto.metaschema.codegen.MetaschemaCompilerHelper;
import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.validation.IContentValidator;
import gov.nist.secauto.metaschema.model.common.validation.IValidationFinding;
import gov.nist.secauto.metaschema.model.common.validation.IValidationResult;
import gov.nist.secauto.metaschema.model.common.validation.JsonSchemaContentValidator.JsonValidationFinding;
import gov.nist.secauto.metaschema.model.testing.xmlbeans.ContentCaseType;
import gov.nist.secauto.metaschema.model.testing.xmlbeans.GenerateSchemaDocument.GenerateSchema;
import gov.nist.secauto.metaschema.model.testing.xmlbeans.MetaschemaDocument;
import gov.nist.secauto.metaschema.model.testing.xmlbeans.TestCollectionDocument.TestCollection;
import gov.nist.secauto.metaschema.model.testing.xmlbeans.TestScenarioDocument.TestScenario;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractTestSuite {
  private static final Logger LOGGER = LogManager.getLogger(AbstractTestSuite.class);
  private static final MetaschemaLoader LOADER = new MetaschemaLoader();

  @NotNull
  protected abstract Format getRequiredContentFormat();

  @NotNull
  protected abstract URI getTestSuiteURI();

  @NotNull
  protected abstract Path getGenerationPath();

  @NotNull
  protected abstract BiFunction<@NotNull IMetaschema, @NotNull Writer, Void> getGeneratorSupplier();

  protected abstract Supplier<@NotNull ? extends IContentValidator> getSchemaValidatorSupplier();

  @NotNull
  protected abstract Function<Path, @NotNull ? extends IContentValidator> getContentValidatorSupplier();

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
    if (Files.exists(generationPath)) {
      if (!Files.isDirectory(generationPath)) {
        throw new JUnitException(String.format("Generation path '%s' exists and is not a directory", generationPath));
      }
    } else {
      Files.createDirectories(generationPath);
    }
    return directive.getTestSuite().getTestCollectionList().stream()
        .flatMap(collection -> Stream.of(generateCollection(collection, testSuiteUri, generationPath)));
  }

  protected void deleteCollectionOnExit(@NotNull Path path) {
    Runtime.getRuntime().addShutdownHook(new Thread(
        new Runnable() {
          @Override
          public void run() {
            try {
              Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                  Files.delete(file);
                  return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                  if (e == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                  }
                  // directory iteration failed for some reason
                  throw e;
                }
              });
            } catch (IOException ex) {
              throw new IllegalStateException("Failed to delete collection: " + path, ex);
            }
          }
        }));
  }

  private DynamicContainer generateCollection(@NotNull TestCollection collection, @NotNull URI testSuiteUri,
      @NotNull Path generationPath) {
    URI collectionUri = testSuiteUri.resolve(collection.getLocation());

    LOGGER.atInfo().log("Collection: " + collectionUri);
    Path collectionGenerationPath;
    try {
      collectionGenerationPath = Files.createTempDirectory(generationPath, "collection-");
      deleteCollectionOnExit(collectionGenerationPath);
    } catch (IOException ex) {
      throw new JUnitException("Unable to create collection temp directory", ex);
    }

    return DynamicContainer.dynamicContainer(
        collection.getName(),
        testSuiteUri,
        collection.getTestScenarioList().stream()
            .flatMap(scenario -> {
              return Stream.of(generateScenario(scenario, collectionUri, collectionGenerationPath));
            })
            .sequential());
  }

  protected void produceSchema(@NotNull IMetaschema metaschema, @NotNull Path schemaPath) throws IOException {
    produceSchema(metaschema, schemaPath, getGeneratorSupplier());
  }

  protected void produceSchema(@NotNull IMetaschema metaschema, @NotNull Path schemaPath,
      @NotNull BiFunction<@NotNull IMetaschema, @NotNull Writer, Void> schemaProducer) throws IOException {
    Path parentDir = schemaPath.getParent();
    if (!Files.exists(parentDir)) {
      Files.createDirectories(parentDir);
    }

    // try (OutputStream os = Files.newOutputStream(
    // schemaPath,
    // getWriteOpenOptions())) {
    //
    // TeeOutputStream tos = new TeeOutputStream(os, System.out);
    // Writer writer = new OutputStreamWriter(tos, StandardCharsets.UTF_8);

    try (Writer writer = Files.newBufferedWriter(
        schemaPath,
        StandardCharsets.UTF_8,
        getWriteOpenOptions())) {
      schemaProducer.apply(metaschema, writer);
      writer.flush();
    }
  }

  protected OpenOption[] getWriteOpenOptions() {
    return new OpenOption[] {
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING
    };
  }

  protected DynamicBindingContext produceDynamicBindingContext(@NotNull IMetaschema metaschema,
      @NotNull Path generationDirPath) throws IOException {
    Path classDir;
    try {
      classDir = Files.createTempDirectory(generationDirPath, "classes-");
    } catch (IOException ex) {
      throw new JUnitException("Unable to class generation directory", ex);
    }

    IProduction production = MetaschemaCompilerHelper.compileMetaschema(metaschema, classDir);
    return new DynamicBindingContext(production,
        MetaschemaCompilerHelper.getClassLoader(classDir, Thread.currentThread().getContextClassLoader()));
  }

  private DynamicContainer generateScenario(@NotNull TestScenario scenario, @NotNull URI collectionUri,
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

    GenerateSchema generateSchema = scenario.getGenerateSchema();
    MetaschemaDocument.Metaschema metaschemaDirective = generateSchema.getMetaschema();
    URI metaschemaUri = collectionUri.resolve(metaschemaDirective.getLocation());

    ExecutorService executor = Executors.newSingleThreadExecutor(); // NOPMD - intentional use of threads
    Future<@NotNull IMetaschema> loadMetaschemaFuture = executor.submit(() -> {
      IMetaschema metaschema;
      try {
        metaschema = LOADER.loadXmlMetaschema(metaschemaUri.toURL());
      } catch (IOException | MetaschemaException ex) {
        throw new JUnitException("Unable to generate schema for Metaschema: " + metaschemaUri, ex);
      }
      return metaschema;
    });

    Future<@NotNull Path> generateSchemaFuture = executor.submit(() -> {
      IMetaschema metaschema = loadMetaschemaFuture.get();
      Path schemaPath;
      // determine what file to use for the schema
      try {
        schemaPath = Files.createTempFile(scenarioGenerationPath, "", "-schema");
      } catch (IOException ex) {
        throw new JUnitException("Unable to create schema temp file", ex);
      }
      produceSchema(metaschema, schemaPath);
      return schemaPath;
    });

    Future<@NotNull DynamicBindingContext> dynamicBindingContextFuture = executor.submit(() -> {
      IMetaschema metaschema = loadMetaschemaFuture.get();
      DynamicBindingContext context;
      try {
        context = produceDynamicBindingContext(metaschema, scenarioGenerationPath);
      } catch (Throwable ex) {
        throw new JUnitException("Unable to generate classes for metaschema: " + metaschemaUri, ex);
      }
      return context;
    });

    Future<@NotNull IContentValidator> contentValidatorFuture = executor.submit(() -> {
      Path schemaPath = generateSchemaFuture.get();
      return getContentValidatorSupplier().apply(schemaPath);
    });

    // build a test container for the generate and validate steps
    DynamicTest validateSchema = DynamicTest.dynamicTest(
        "Validate Schema",
        () -> {
          Supplier<@NotNull ? extends IContentValidator> supplier = getSchemaValidatorSupplier();
          if (supplier != null) {
            Path schemaPath;
            try {
              schemaPath = generateSchemaFuture.get();
            } catch (ExecutionException ex) {
              throw new JUnitException("failed to generate schema", ex.getCause());
            }
            validate(supplier.get(), schemaPath);
          }
        });

    Stream<? extends DynamicNode> contentTests;
    {
      contentTests = scenario.getValidationCaseList().stream()
          .flatMap(contentCase -> {
            DynamicTest test
                = generateValidationCase(contentCase, dynamicBindingContextFuture, contentValidatorFuture,
                    collectionUri, scenarioGenerationPath);
            return test == null ? Stream.empty() : Stream.of(test);
          }).sequential();
    }

    return DynamicContainer.dynamicContainer(
        scenario.getName(),
        metaschemaUri,
        Stream.concat(Stream.of(validateSchema), contentTests).sequential());
  }

  @SuppressWarnings("unchecked")
  protected Path convertContent(URI contentUri, @NotNull Path generationPath, @NotNull DynamicBindingContext context)
      throws IOException {
    Object object;
    try {
      object = context.newBoundLoader().load(contentUri.toURL());
    } catch (URISyntaxException ex) {
      throw new IOException(ex);
    }

    if (!Files.exists(generationPath)) {
      Files.createDirectories(generationPath);
    }

    Path convertedContetPath;
    try {
      convertedContetPath = Files.createTempFile(generationPath, "", "-content");
    } catch (IOException ex) {
      throw new JUnitException("Unable to create schema temp file", ex);
    }

    @SuppressWarnings("rawtypes")
    ISerializer serializer = context.newSerializer(getRequiredContentFormat(), object.getClass());
    serializer.serialize(object, convertedContetPath, getWriteOpenOptions());

    return convertedContetPath;
  }

  private DynamicTest generateValidationCase(
      @NotNull ContentCaseType contentCase,
      @NotNull Future<@NotNull DynamicBindingContext> contextFuture,
      @NotNull Future<@NotNull IContentValidator> contentValidatorFuture,
      @NotNull URI collectionUri,
      @NotNull Path generationPath) {
    DynamicTest retval;

    URI contentUri = collectionUri.resolve(contentCase.getLocation());

    Format format = contentCase.getSourceFormat();
    if (getRequiredContentFormat().equals(format)) {
      retval = DynamicTest.dynamicTest(
          String.format("Validate %s=%s: %s", format, contentCase.getValidationResult(),
              contentCase.getLocation()),
          contentUri,
          () -> {
            IContentValidator contentValidator;
            try {
              contentValidator = contentValidatorFuture.get();
            } catch (ExecutionException ex) {
              throw new JUnitException("failed to produce the content validator", ex.getCause());
            }

            assertEquals(contentCase.getValidationResult(), validate(contentValidator, contentUri.toURL()),
                "validation did not match expectation");
          });
    } else if (contentCase.getValidationResult()) {
      retval = DynamicTest.dynamicTest(
          String.format("Convert and Validate %s=%s: %s", format, contentCase.getValidationResult(),
              contentCase.getLocation()),
          contentUri,
          () -> {
            DynamicBindingContext context;
            try {
              context = contextFuture.get();
            } catch (ExecutionException ex) {
              throw new JUnitException("failed to produce the content validator", ex.getCause());
            }
            Path convertedContetPath;
            try {
              convertedContetPath = convertContent(contentUri, generationPath, context);
            } catch (Exception ex) {
              throw new JUnitException("failed to convert content: " + contentUri, ex);
            }

            IContentValidator contentValidator;
            try {
              contentValidator = contentValidatorFuture.get();
            } catch (ExecutionException ex) {
              throw new JUnitException("failed to produce the content validator", ex.getCause());
            }
            assertEquals(contentCase.getValidationResult(),
                validate(contentValidator, convertedContetPath.toUri().toURL()),
                String.format("validation of '%s' did not match expectation", convertedContetPath));
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
    if (!schemaValidationResult.isPassing()) {
      LOGGER.atError().log("Schema validation failed for: {}", target);
    }
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

    // if (finding.getCause() != null) {
    // logBuilder.withThrowable(finding.getCause());
    // }

    if (finding instanceof JsonValidationFinding) {
      JsonValidationFinding jsonFinding = (JsonValidationFinding) finding;
      logBuilder.log("[{}] {}", jsonFinding.getCause().getPointerToViolation(), finding.getMessage());
    } else {
      logBuilder.log("{}", finding.getMessage());
    }
  }
}
