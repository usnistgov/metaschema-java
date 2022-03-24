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
import gov.nist.secauto.metaschema.model.common.validation.JsonSchemaContentValidator;
import gov.nist.secauto.metaschema.model.common.validation.XmlSchemaContentValidator;
import gov.nist.secauto.metaschema.model.testing.AbstractTestSuite;
import gov.nist.secauto.metaschema.model.testing.DynamicBindingContext;

import org.jetbrains.annotations.NotNull;
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

public abstract class AbstractSchemaGeneratorTestSuite
    extends AbstractTestSuite {
  @NotNull
  protected static final ISchemaGenerator XML_SCHEMA_GENERATOR = new XmlSchemaGenerator();
  @NotNull
  protected static final ISchemaGenerator JSON_SCHEMA_GENERATOR = new JsonSchemaGenerator();
  @NotNull
  protected static final IConfiguration SCHEMA_GENERATION_CONFIG;
  @NotNull
  protected static final BiFunction<@NotNull IMetaschema, @NotNull Writer, Void> XML_SCHEMA_PROVIDER;
  @NotNull
  protected static final BiFunction<@NotNull IMetaschema, @NotNull Writer, Void> JSON_SCHEMA_PROVIDER;
  @NotNull
  protected static final JsonSchemaContentValidator JSON_SCHEMA_VALIDATOR;
  @NotNull
  protected static final Function<@NotNull Path, @NotNull JsonSchemaContentValidator> JSON_CONTENT_VALIDATOR_PROVIDER;
  @NotNull
  protected static final Function<@NotNull Path, @NotNull XmlSchemaContentValidator> XML_CONTENT_VALIDATOR_PROVIDER;
  
  static {
    SCHEMA_GENERATION_CONFIG = new DefaultMutableConfiguration()
        .enableFeature(Feature.INLINE_DEFINITIONS)
        .disableFeature(Feature.INLINE_CHOICE_DEFINITIONS);

    BiFunction<@NotNull IMetaschema, @NotNull Writer, Void> xmlProvider = (metaschema, writer) -> {
      try {
        XML_SCHEMA_GENERATOR.generateFromMetaschema(metaschema, writer, SCHEMA_GENERATION_CONFIG);
      } catch (IOException ex) {
        throw new JUnitException("IO error", ex);
      }
      return null;
    };
    XML_SCHEMA_PROVIDER = xmlProvider;

    BiFunction<@NotNull IMetaschema, @NotNull Writer, Void> jsonProvider = (metaschema, writer) -> {
      try {
        JSON_SCHEMA_GENERATOR.generateFromMetaschema(metaschema, writer, SCHEMA_GENERATION_CONFIG);
      } catch (IOException ex) {
        throw new JUnitException("IO error", ex);
      }
      return null;
    };
    JSON_SCHEMA_PROVIDER = jsonProvider;

    try (InputStream is = MetaschemaLoader.class.getClassLoader().getResourceAsStream("schema/json/json-schema.json")) {
      @SuppressWarnings("null")
      JsonSchemaContentValidator schemaValidator = new JsonSchemaContentValidator(is);
      JSON_SCHEMA_VALIDATOR = schemaValidator;
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }

    @SuppressWarnings("null")
    @NotNull
    Function<@NotNull Path, @NotNull XmlSchemaContentValidator> xmlContentValidatorProvider = (path) -> {
      try {
        URL schemaResource = path.toUri().toURL();
        List<? extends Source> schemaSources = Collections.singletonList(
            new StreamSource(schemaResource.openStream(), schemaResource.toString()));
        return new XmlSchemaContentValidator(schemaSources);
      } catch (IOException | SAXException ex) {
        throw new IllegalStateException(ex);
      }
    };
    XML_CONTENT_VALIDATOR_PROVIDER = xmlContentValidatorProvider;
    
    @SuppressWarnings("null")
    @NotNull
    Function<@NotNull Path, @NotNull JsonSchemaContentValidator> jsonContentValidatorProvider = (path) -> {
      try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
        return new JsonSchemaContentValidator(is);
      } catch (Exception ex) {
        throw new JUnitException("Failed to create content validator for schema: " + path.toString(), ex);
      }
    };
    JSON_CONTENT_VALIDATOR_PROVIDER = jsonContentValidatorProvider;
  }

  @SuppressWarnings("null")
  @Override
  protected URI getTestSuiteURI() {
    return Paths.get("../metaschema-model/metaschema/test-suite/schema-generation/unit-tests.xml").toUri();
  }

  @SuppressWarnings("null")
  @Override
  protected Path getGenerationPath() {
    return Paths.get("test-schemagen");
  }

  protected Path produceXmlSchema(@NotNull IMetaschema metaschema, @NotNull Path schemaPath) throws IOException {
    produceSchema(metaschema, schemaPath, XML_SCHEMA_PROVIDER);
    return schemaPath;
  }

  protected Path produceJsonSchema(@NotNull IMetaschema metaschema, @NotNull Path schemaPath) throws IOException {
    produceSchema(metaschema, schemaPath, JSON_SCHEMA_PROVIDER);
    return schemaPath;
  }

  @SuppressWarnings("null")
  protected void doTest(
      @NotNull String collectionName,
      @NotNull String metaschemaName,
      @NotNull String generatedSchemaName,
      @NotNull List<@NotNull ContentCase> contentCases) throws IOException, MetaschemaException {
    Path generationDir = getGenerationPath();

    Path testSuite = Paths.get("../metaschema-model/metaschema/test-suite/schema-generation/");
    Path collectionPath = testSuite.resolve(collectionName);

    MetaschemaLoader loader = new MetaschemaLoader();
    Path metaschemaPath = collectionPath.resolve(metaschemaName);
    IMetaschema metaschema = loader.loadXmlMetaschema(metaschemaPath);

    Path jsonSchema = produceJsonSchema(metaschema, generationDir.resolve(generatedSchemaName + ".json"));
    assertEquals(true, validate(JSON_SCHEMA_VALIDATOR, jsonSchema));
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
    
    DynamicBindingContext context = produceDynamicBindingContext(metaschema, generationDir);
    for (ContentCase contentCase : contentCases) {
      Path contentPath = collectionPath.resolve(contentCase.getName());
      
      if (!getRequiredContentFormat().equals(contentCase.getActualFormat())) {
        contentPath = convertContent(contentPath.toUri(), generationDir, context);
      }
      
      assertEquals(contentCase.isValid(),
          validate(getContentValidatorSupplier().apply(schemaPath), contentPath),
          "validation did not match expectation");
    }
  }

  protected ContentCase contentCase(@NotNull Format actualFormat, @NotNull String contentName, boolean valid) {
    return new ContentCase(contentName, actualFormat, valid);
  }

  protected static class ContentCase {
    @NotNull 
    private final String name;
    @NotNull 
    private final Format actualFormat;
    private final boolean valid;

    public ContentCase(@NotNull String name, @NotNull Format actualFormat, boolean valid) {
      this.name = name;
      this.actualFormat = actualFormat;
      this.valid = valid;
    }

    @NotNull 
    public String getName() {
      return name;
    }

    @NotNull 
    public Format getActualFormat() {
      return actualFormat;
    }

    public boolean isValid() {
      return valid;
    }
  }
}
