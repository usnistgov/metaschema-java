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

import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.binding.io.ISerializer;
import gov.nist.secauto.metaschema.codegen.Production;
import gov.nist.secauto.metaschema.codegen.compile.MetaschemaCompilerHelper;
import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.validation.IContentValidator;
import gov.nist.secauto.metaschema.model.common.validation.JsonSchemaContentValidator;
import gov.nist.secauto.metaschema.model.testing.AbstractTestSuite;
import gov.nist.secauto.metaschema.model.testing.DynamicBindingContext;
import gov.nist.secauto.metaschema.schemagen.json.ISchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.json.JsonSchemaGenerator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.commons.JUnitException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class JsonSuiteTest
    extends AbstractTestSuite {
  private static final ISchemaGenerator GENERATOR = new JsonSchemaGenerator();
  private static final JsonSchemaContentValidator SCHEMA_VALIDATOR;

  static {
    try (InputStream is = MetaschemaLoader.class.getClassLoader().getResourceAsStream("schema/json/json-schema.json")) {
      SCHEMA_VALIDATOR = new JsonSchemaContentValidator(is);
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }

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
    return () -> SCHEMA_VALIDATOR;
  }

  @Override
  protected Format getRequiredContentFormat() {
    return Format.JSON;
  }
  @Override
  protected Function<Path, IContentValidator> getContentValidatorSupplier() {
    return (path) -> {
      try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
        return new JsonSchemaContentValidator(is);
      } catch (IOException ex) {
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
  @DisplayName("JSON Schema Generation")
  @TestFactory
  public Stream<? extends DynamicNode> generateTests() {
    return testFactory();
  }


//  @Disabled
  @Test
  void test() throws IOException, MetaschemaException {
    Path generationDir = getGenerationPath();

    Path testSuite = Paths.get("../metaschema-model/metaschema/test-suite/schema-generation/");
    MetaschemaLoader loader = new MetaschemaLoader();
    IMetaschema  metaschema = loader.loadXmlMetaschema(testSuite.resolve("datatypes/datatypes-uuid_metaschema.xml"));

    Path schemaPath = generationDir.resolve("test-schema.out");
    produceSchema(metaschema, schemaPath);
    assertEquals(true, validate(getSchemaValidatorSupplier().get(), schemaPath));
    
    Path contentPath = testSuite.resolve("datatypes/datatypes-uuid_test_valid_PASS.json");

    DynamicBindingContext context = produceDynamicBindingContext(metaschema, generationDir); 
    contentPath = convertContent(contentPath.toUri(), generationDir, context);

    assertEquals(true,
        validate(getContentValidatorSupplier().apply(schemaPath), contentPath),
        "validation did not match expectation");

  }
}
