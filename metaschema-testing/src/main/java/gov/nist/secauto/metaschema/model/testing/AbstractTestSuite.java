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

import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.validation.IContentValidator;
import gov.nist.secauto.metaschema.core.model.validation.IValidationFinding;
import gov.nist.secauto.metaschema.core.model.validation.IValidationResult;
import gov.nist.secauto.metaschema.core.model.validation.JsonSchemaContentValidator.JsonValidationFinding;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.DefaultBindingContext;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.ISerializer;
import gov.nist.secauto.metaschema.databind.model.metaschema.BindingModuleLoader;
import gov.nist.secauto.metaschema.model.testing.xml.xmlbeans.ContentCaseType;
import gov.nist.secauto.metaschema.model.testing.xml.xmlbeans.GenerateSchemaDocument.GenerateSchema;
import gov.nist.secauto.metaschema.model.testing.xml.xmlbeans.MetaschemaDocument;
import gov.nist.secauto.metaschema.model.testing.xml.xmlbeans.TestCollectionDocument.TestCollection;
import gov.nist.secauto.metaschema.model.testing.xml.xmlbeans.TestScenarioDocument.TestScenario;
import gov.nist.secauto.metaschema.model.testing.xml.xmlbeans.TestSuiteDocument;

import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

public abstract class AbstractTestSuite {
  private static final Logger LOGGER = LogManager.getLogger(AbstractTestSuite.class);
  private static final BindingModuleLoader LOADER = new BindingModuleLoader();

  private static final boolean DELETE_RESULTS_ON_EXIT = false;

  static {
    LOADER.allowEntityResolution();
  }

  @NonNull
  protected abstract Format getRequiredContentFormat();

  @NonNull
  protected abstract URI getTestSuiteURI();

  @NonNull
  protected abstract Path getGenerationPath();

  @NonNull
  protected abstract BiFunction<IModule, Writer, Void> getGeneratorSupplier();

  @Nullable
  protected abstract Supplier<? extends IContentValidator> getSchemaValidatorSupplier();

  @NonNull
  protected abstract Function<Path, ? extends IContentValidator> getContentValidatorSupplier();

  protected Stream<DynamicNode> testFactory() {
    try {
      return generateTests();
    } catch (XmlException | IOException ex) {
      throw new JUnitException("Unable to generate tests", ex);
    }
  }

  private Stream<DynamicNode> generateTests() throws XmlException, IOException {
    XmlOptions options = new XmlOptions();
    options.setBaseURI(null);
    options.setLoadLineNumbers();

    Path generationPath = getGenerationPath();
    if (Files.exists(generationPath)) {
      if (!Files.isDirectory(generationPath)) {
        throw new JUnitException(String.format("Generation path '%s' exists and is not a directory", generationPath));
      }
    } else {
      Files.createDirectories(generationPath);
    }

    URI testSuiteUri = getTestSuiteURI();
    URL testSuiteUrl = testSuiteUri.toURL();
    TestSuiteDocument directive = TestSuiteDocument.Factory.parse(testSuiteUrl, options);
    return directive.getTestSuite().getTestCollectionList().stream()
        .flatMap(
            collection -> Stream.of(generateCollection(ObjectUtils.notNull(collection), testSuiteUri, generationPath)));
  }

  protected void deleteCollectionOnExit(Path path) {
    if (path != null) {
      Runtime.getRuntime().addShutdownHook(new Thread( // NOPMD - this is not a webapp
          () -> {
            try {
              Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                  Files.delete(file);
                  return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
                  if (ex == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                  }
                  // directory iteration failed for some reason
                  throw ex;
                }
              });
            } catch (IOException ex) {
              throw new JUnitException("Failed to delete collection: " + path, ex);
            }
          }));
    }
  }

  private DynamicContainer generateCollection(@NonNull TestCollection collection, @NonNull URI testSuiteUri,
      @NonNull Path generationPath) {
    URI collectionUri = testSuiteUri.resolve(collection.getLocation());
    assert collectionUri != null;

    LOGGER.atInfo().log("Collection: " + collectionUri);
    Lazy<Path> collectionGenerationPath = ObjectUtils.notNull(Lazy.lazy(() -> {
      Path retval;
      try {
        retval = ObjectUtils.notNull(Files.createTempDirectory(generationPath, "collection-"));
        assert retval != null;
        if (DELETE_RESULTS_ON_EXIT) {
          deleteCollectionOnExit(retval);
        }
      } catch (IOException ex) {
        throw new JUnitException("Unable to create collection temp directory", ex);
      }
      return retval;
    }));

    return DynamicContainer.dynamicContainer(
        collection.getName(),
        testSuiteUri,
        collection.getTestScenarioList().stream()
            .flatMap(scenario -> {
              assert scenario != null;
              return Stream.of(generateScenario(scenario, collectionUri, collectionGenerationPath));
            })
            .sequential());
  }

  protected void produceSchema(@NonNull IModule module, @NonNull Path schemaPath) throws IOException {
    produceSchema(module, schemaPath, getGeneratorSupplier());
  }

  protected void produceSchema(@NonNull IModule module, @NonNull Path schemaPath,
      @NonNull BiFunction<IModule, Writer, Void> schemaProducer) throws IOException {
    Path parentDir = schemaPath.getParent();
    if (parentDir != null && !Files.exists(parentDir)) {
      Files.createDirectories(parentDir);
    }

    try (Writer writer = Files.newBufferedWriter(
        schemaPath,
        StandardCharsets.UTF_8,
        getWriteOpenOptions())) {
      schemaProducer.apply(module, writer);
    }
    LOGGER.atInfo().log("Produced schema '{}' for module '{}'", schemaPath, module.getLocation());
  }

  protected OpenOption[] getWriteOpenOptions() {
    return new OpenOption[] {
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING
    };
  }

  private DynamicContainer generateScenario(
      @NonNull TestScenario scenario,
      @NonNull URI collectionUri,
      @NonNull Lazy<Path> collectionGenerationPath) {
    Lazy<Path> scenarioGenerationPath = Lazy.lazy(() -> {
      Path retval;
      try {
        retval = Files.createTempDirectory(collectionGenerationPath.get(), "scenario-");
      } catch (IOException ex) {
        throw new JUnitException("Unable to create scenario temp directory", ex);
      }
      return retval;
    });

    // try {
    // // create the directories the schema will be stored in
    // Files.createDirectories(scenarioGenerationPath);
    // } catch (IOException ex) {
    // throw new JUnitException("Unable to create test directories for path: " +
    // scenarioGenerationPath, ex);
    // }

    GenerateSchema generateSchema = scenario.getGenerateSchema();
    MetaschemaDocument.Metaschema metaschemaDirective = generateSchema.getMetaschema();
    URI metaschemaUri = collectionUri.resolve(metaschemaDirective.getLocation());

    Lazy<IModule> lazyModule = Lazy.lazy(() -> {
      IModule module;
      try {
        module = LOADER.load(ObjectUtils.notNull(metaschemaUri.toURL()));
      } catch (IOException | MetaschemaException ex) {
        throw new JUnitException("Unable to generate schema for Module: " + metaschemaUri, ex);
      }
      return module;
    });

    Lazy<Path> lazySchema = Lazy.lazy(() -> {
      Path schemaPath;

      String schemaExtension;
      Format requiredContentFormat = getRequiredContentFormat();
      switch (requiredContentFormat) {
      case JSON:
      case YAML:
        schemaExtension = ".json";
        break;
      case XML:
        schemaExtension = ".xsd";
        break;
      default:
        throw new IllegalStateException();
      }

      // determine what file to use for the schema
      try {
        schemaPath = Files.createTempFile(scenarioGenerationPath.get(), "", "-schema" + schemaExtension);
      } catch (IOException ex) {
        throw new JUnitException("Unable to create schema temp file", ex);
      }
      IModule module = lazyModule.get();
      try {
        produceSchema(ObjectUtils.notNull(module), ObjectUtils.notNull(schemaPath));
      } catch (IOException ex) {
        throw new IllegalStateException(ex);
      }
      return schemaPath;
    });

    Lazy<IBindingContext> lazyDynamicBindingContext = Lazy.lazy(() -> {
      IModule module = lazyModule.get();
      IBindingContext context;
      try {
        context = new DefaultBindingContext();
        context.registerModule(
            ObjectUtils.notNull(module),
            ObjectUtils.notNull(scenarioGenerationPath.get()));
      } catch (Exception ex) { // NOPMD - intentional
        throw new JUnitException("Unable to generate classes for metaschema: " + metaschemaUri, ex);
      }
      return context;
    });
    assert lazyDynamicBindingContext != null;

    Lazy<IContentValidator> lazyContentValidator = Lazy.lazy(() -> {
      Path schemaPath = lazySchema.get();
      return getContentValidatorSupplier().apply(schemaPath);
    });
    assert lazyContentValidator != null;

    // build a test container for the generate and validate steps
    DynamicTest validateSchema = DynamicTest.dynamicTest(
        "Validate Schema",
        () -> {
          Supplier<? extends IContentValidator> supplier = getSchemaValidatorSupplier();
          if (supplier != null) {
            Path schemaPath;
            try {
              schemaPath = ObjectUtils.requireNonNull(lazySchema.get());
            } catch (Exception ex) {
              throw new JUnitException( // NOPMD - cause is relevant, exception is not
                  "failed to generate schema", ex.getCause());
            }
            validate(ObjectUtils.requireNonNull(supplier.get()), schemaPath);
          }
        });

    Stream<? extends DynamicNode> contentTests;
    {
      contentTests = scenario.getValidationCaseList().stream()
          .flatMap(contentCase -> {
            assert contentCase != null;
            DynamicTest test
                = generateValidationCase(
                    contentCase,
                    lazyDynamicBindingContext,
                    lazyContentValidator,
                    collectionUri,
                    ObjectUtils.notNull(scenarioGenerationPath));
            return test == null ? Stream.empty() : Stream.of(test);
          }).sequential();
    }

    return DynamicContainer.dynamicContainer(
        scenario.getName(),
        metaschemaUri,
        Stream.concat(Stream.of(validateSchema), contentTests).sequential());
  }

  protected Path convertContent(
      @NonNull URI contentUri,
      @NonNull Path generationPath,
      @NonNull IBindingContext context)
      throws IOException {
    Object object;
    try {
      object = context.newBoundLoader().load(ObjectUtils.notNull(contentUri.toURL()));
    } catch (URISyntaxException ex) {
      throw new IOException(ex);
    }

    if (!Files.exists(generationPath)) {
      Files.createDirectories(generationPath);
    }

    Path convertedContetPath;
    try {
      convertedContetPath = ObjectUtils.notNull(Files.createTempFile(generationPath, "", "-content"));
    } catch (IOException ex) {
      throw new JUnitException("Unable to create schema temp file", ex);
    }

    @SuppressWarnings("rawtypes") ISerializer serializer
        = context.newSerializer(getRequiredContentFormat(), ObjectUtils.asType(object.getClass()));
    serializer.serialize(ObjectUtils.asType(object), convertedContetPath, getWriteOpenOptions());

    return convertedContetPath;
  }

  private DynamicTest generateValidationCase(
      @NonNull ContentCaseType contentCase,
      @NonNull Lazy<IBindingContext> lazyBindingContext,
      @NonNull Lazy<IContentValidator> lazyContentValidator,
      @NonNull URI collectionUri,
      @NonNull Lazy<Path> generationPath) {

    URI contentUri = ObjectUtils.notNull(collectionUri.resolve(contentCase.getLocation()));

    Format format = contentCase.getSourceFormat();
    DynamicTest retval = null;
    if (getRequiredContentFormat().equals(format)) {
      retval = DynamicTest.dynamicTest(
          String.format("Validate %s=%s: %s", format, contentCase.getValidationResult(),
              contentCase.getLocation()),
          contentUri,
          () -> {
            IContentValidator contentValidator;
            try {
              contentValidator = lazyContentValidator.get();
            } catch (Exception ex) {
              throw new JUnitException( // NOPMD - cause is relevant, exception is not
                  "failed to produce the content validator", ex.getCause());
            }

            assertEquals(
                contentCase.getValidationResult(),
                validate(
                    ObjectUtils.notNull(contentValidator), ObjectUtils.notNull(contentUri.toURL())),
                "validation did not match expectation for: " + contentUri.toASCIIString());
          });
    } else if (contentCase.getValidationResult()) {
      retval = DynamicTest.dynamicTest(
          String.format("Convert and Validate %s=%s: %s", format, contentCase.getValidationResult(),
              contentCase.getLocation()),
          contentUri,
          () -> {
            IBindingContext context;
            try {
              context = lazyBindingContext.get();
            } catch (Exception ex) {
              throw new JUnitException( // NOPMD - cause is relevant, exception is not
                  "failed to produce the content validator", ex.getCause());
            }
            assert context != null;

            Path convertedContetPath;
            try {
              convertedContetPath = convertContent(contentUri, ObjectUtils.notNull(generationPath.get()), context);
            } catch (Exception ex) { // NOPMD - intentional
              throw new JUnitException("failed to convert content: " + contentUri, ex);
            }

            IContentValidator contentValidator;
            try {
              contentValidator = lazyContentValidator.get();
            } catch (Exception ex) {
              throw new JUnitException( // NOPMD - cause is relevant, exception is not
                  "failed to produce the content validator",
                  ex.getCause());
            }
            assertEquals(contentCase.getValidationResult(),
                validate(
                    ObjectUtils.notNull(contentValidator),
                    ObjectUtils.notNull(convertedContetPath.toUri().toURL())),
                String.format("validation of '%s' did not match expectation", convertedContetPath));
          });
    }
    return retval;
  }

  private static boolean validate(@NonNull IContentValidator validator, @NonNull URL target) throws IOException {
    IValidationResult schemaValidationResult;
    try {
      schemaValidationResult = validator.validate(target);
    } catch (URISyntaxException ex) {
      throw new IOException(ex);
    }
    return processValidationResult(schemaValidationResult);
  }

  protected static boolean validate(@NonNull IContentValidator validator, @NonNull Path target) throws IOException {
    IValidationResult schemaValidationResult = validator.validate(target);
    if (!schemaValidationResult.isPassing()) {
      LOGGER.atError().log("Schema validation failed for: {}", target);
    }
    return processValidationResult(schemaValidationResult);
  }

  private static boolean processValidationResult(IValidationResult schemaValidationResult) {
    for (IValidationFinding finding : schemaValidationResult.getFindings()) {
      logFinding(ObjectUtils.notNull(finding));
    }
    return schemaValidationResult.isPassing();
  }

  private static void logFinding(@NonNull IValidationFinding finding) {
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
    case DEBUG:
      logBuilder = LOGGER.atDebug();
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
