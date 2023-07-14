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
import gov.nist.secauto.metaschema.codegen.binding.DynamicBindingContext;
import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.configuration.DefaultConfiguration;
import gov.nist.secauto.metaschema.model.common.configuration.IConfiguration;
import gov.nist.secauto.metaschema.model.common.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
import gov.nist.secauto.metaschema.model.common.validation.JsonSchemaContentValidator;
import gov.nist.secauto.metaschema.model.common.validation.XmlSchemaContentValidator;
import gov.nist.secauto.metaschema.model.testing.AbstractTestSuite;
import gov.nist.secauto.metaschema.schemagen.json.JsonSchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.xml.XmlSchemaGenerator;

import org.junit.platform.commons.JUnitException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractSchemaGeneratorTestSuite
    extends AbstractTestSuite {
  @NonNull
  protected static final ISchemaGenerator XML_SCHEMA_GENERATOR = new XmlSchemaGenerator();
  @NonNull
  protected static final ISchemaGenerator JSON_SCHEMA_GENERATOR = new JsonSchemaGenerator();
  @NonNull
  protected static final IConfiguration<SchemaGenerationFeature<?>> SCHEMA_GENERATION_CONFIG;
  @NonNull
  protected static final BiFunction<IMetaschema, Writer, Void> XML_SCHEMA_PROVIDER;
  @NonNull
  protected static final BiFunction<IMetaschema, Writer, Void> JSON_SCHEMA_PROVIDER;
  @NonNull
  protected static final JsonSchemaContentValidator JSON_SCHEMA_VALIDATOR;
  @NonNull
  protected static final Function<Path, JsonSchemaContentValidator> JSON_CONTENT_VALIDATOR_PROVIDER;
  @NonNull
  protected static final Function<Path, XmlSchemaContentValidator> XML_CONTENT_VALIDATOR_PROVIDER;

  private static final String UNIT_TEST_CONFIG
      = "../metaschema-model/metaschema/test-suite/schema-generation/unit-tests.xml";

  static {
    IMutableConfiguration<SchemaGenerationFeature<?>> features = new DefaultConfiguration<>();
    features.disableFeature(SchemaGenerationFeature.INLINE_DEFINITIONS);
    SCHEMA_GENERATION_CONFIG = features;

    BiFunction<IMetaschema, Writer, Void> xmlProvider = (metaschema, writer) -> {
      assert metaschema != null;
      assert writer != null;
      try {
        XML_SCHEMA_GENERATOR.generateFromMetaschema(metaschema, writer, SCHEMA_GENERATION_CONFIG);
      } catch (SchemaGenerationException ex) {
        throw new JUnitException("IO error", ex);
      }
      return null;
    };
    XML_SCHEMA_PROVIDER = xmlProvider;

    BiFunction<IMetaschema, Writer, Void> jsonProvider = (metaschema, writer) -> {
      assert metaschema != null;
      assert writer != null;
      try {
        JSON_SCHEMA_GENERATOR.generateFromMetaschema(metaschema, writer, SCHEMA_GENERATION_CONFIG);
      } catch (SchemaGenerationException ex) {
        throw new JUnitException("IO error", ex);
      }
      return null;
    };
    JSON_SCHEMA_PROVIDER = jsonProvider;

    try (InputStream is = MetaschemaLoader.class.getResourceAsStream("/schema/json/json-schema.json")) {
      assert is != null : "unable to get JSON schema resource";
      JsonSchemaContentValidator schemaValidator = new JsonSchemaContentValidator(is);
      JSON_SCHEMA_VALIDATOR = schemaValidator;
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }

    @SuppressWarnings("null")
    @NonNull Function<Path, XmlSchemaContentValidator> xmlContentValidatorProvider = (path) -> {
      try {
        URL schemaResource = path.toUri().toURL();
        @SuppressWarnings("resource") StreamSource source
            = new StreamSource(schemaResource.openStream(), schemaResource.toString());
        List<? extends Source> schemaSources = Collections.singletonList(source);
        return new XmlSchemaContentValidator(schemaSources);
      } catch (IOException | SAXException ex) {
        throw new IllegalStateException(ex);
      }
    };
    XML_CONTENT_VALIDATOR_PROVIDER = xmlContentValidatorProvider;

    @NonNull Function<Path, JsonSchemaContentValidator> jsonContentValidatorProvider = (path) -> {
      try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
        assert is != null;
        return new JsonSchemaContentValidator(is);
      } catch (IOException ex) {
        throw new JUnitException("Failed to create content validator for schema: " + path.toString(), ex);
      }
    };
    JSON_CONTENT_VALIDATOR_PROVIDER = jsonContentValidatorProvider;
  }

  @Override
  protected URI getTestSuiteURI() {
    return ObjectUtils
        .notNull(Paths.get(UNIT_TEST_CONFIG).toUri());
  }

  @Override
  protected Path getGenerationPath() {
    return ObjectUtils.notNull(Paths.get("target/test-schemagen"));
  }

  protected Path produceXmlSchema(@NonNull IMetaschema metaschema, @NonNull Path schemaPath) throws IOException {
    produceSchema(metaschema, schemaPath, XML_SCHEMA_PROVIDER);
    return schemaPath;
  }

  protected Path produceJsonSchema(@NonNull IMetaschema metaschema, @NonNull Path schemaPath) throws IOException {
    produceSchema(metaschema, schemaPath, JSON_SCHEMA_PROVIDER);
    return schemaPath;
  }

  @SuppressWarnings("null")
  protected void doTest(
      @NonNull String collectionName,
      @NonNull String metaschemaName,
      @NonNull String generatedSchemaName,
      @NonNull ContentCase... contentCases) throws IOException, MetaschemaException {
    Path generationDir = getGenerationPath();

    Path testSuite = Paths.get("../metaschema-model/metaschema/test-suite/schema-generation/");
    Path collectionPath = testSuite.resolve(collectionName);

    MetaschemaLoader loader = new MetaschemaLoader();
    loader.allowEntityResolution();
    Path metaschemaPath = collectionPath.resolve(metaschemaName);
    IMetaschema metaschema = loader.load(metaschemaPath);

    Path jsonSchema = produceJsonSchema(metaschema, generationDir.resolve(generatedSchemaName + ".json"));
    assertEquals(true, validate(JSON_SCHEMA_VALIDATOR, jsonSchema),
        String.format("JSON schema '%s' was invalid", jsonSchema.toString()));
    Path xmlSchema = produceXmlSchema(metaschema, generationDir.resolve(generatedSchemaName + ".xsd"));

    Path schemaPath;
    switch (getRequiredContentFormat()) {
    case JSON:
    case YAML:
      schemaPath = jsonSchema;
      break;
    case XML:
      schemaPath = xmlSchema;
      break;
    default:
      throw new IllegalStateException();
    }

    DynamicBindingContext context = DynamicBindingContext.forMetaschema(
        metaschema,
        generationDir);
    for (ContentCase contentCase : contentCases) {
      Path contentPath = collectionPath.resolve(contentCase.getName());

      if (!getRequiredContentFormat().equals(contentCase.getActualFormat())) {
        contentPath = convertContent(contentPath.toUri(), generationDir, context);
      }

      assertEquals(contentCase.isValid(),
          validate(getContentValidatorSupplier().apply(schemaPath), contentPath),
          String.format("validation of '%s' did not match expectation", contentPath));
    }
  }

  @NonNull
  protected ContentCase contentCase(@NonNull Format actualFormat, @NonNull String contentName, boolean valid) {
    return new ContentCase(contentName, actualFormat, valid);
  }

  protected static class ContentCase {
    @NonNull
    private final String name;
    @NonNull
    private final Format actualFormat;
    private final boolean valid;

    public ContentCase(@NonNull String name, @NonNull Format actualFormat, boolean valid) {
      this.name = name;
      this.actualFormat = actualFormat;
      this.valid = valid;
    }

    @NonNull
    public String getName() {
      return name;
    }

    @NonNull
    public Format getActualFormat() {
      return actualFormat;
    }

    public boolean isValid() {
      return valid;
    }
  }
}
