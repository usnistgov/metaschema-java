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

import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.validation.IContentValidator;
import gov.nist.secauto.metaschema.model.common.validation.JsonSchemaContentValidator;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class JsonSuiteTest
    extends AbstractSchemaGeneratorTestSuite {

  @Override
  protected Supplier<@NotNull IContentValidator> getSchemaValidatorSupplier() {
    return () -> JSON_SCHEMA_VALIDATOR;
  }

  @Override
  protected Format getRequiredContentFormat() {
    return Format.JSON;
  }

  @Override
  protected Function<@NotNull Path, @NotNull JsonSchemaContentValidator> getContentValidatorSupplier() {
    return JSON_CONTENT_VALIDATOR_PROVIDER;
  }

  @Override
  protected BiFunction<@NotNull IMetaschema, @NotNull Writer, Void> getGeneratorSupplier() {
    return JSON_SCHEMA_PROVIDER;
  }

  @Execution(ExecutionMode.SAME_THREAD)
  @DisplayName("JSON Schema Generation")
  @TestFactory
  public Stream<? extends DynamicNode> generateTests() {
    return testFactory();
  }

  @Disabled
  @Test
  void testDatatypeUuid() throws IOException, MetaschemaException {
    doTest(
        "datatypes/",
        "datatypes-uuid_metaschema.xml",
        "test-schema",
        contentCase(Format.JSON, "datatypes-uuid_test_valid_PASS.json", true));
  }

  @Disabled
  @Test
  void testChoice() throws IOException, MetaschemaException {
    doTest(
        "choice/",
        "choice-multiple_metaschema.xml",
        "test-choice-schema",
        contentCase(Format.JSON, "choice-multiple_test_multiple_PASS.json", true));
  }

  @Test
  void testDatatypeCharStrings() throws IOException, MetaschemaException {
    doTest(
        "datatypes/",
        "charstrings_metaschema.xml",
        "datatypes-charstrings-schema",
        contentCase(Format.JSON, "charstrings_test_okay_PASS.json", true),
        contentCase(Format.XML, "charstrings_test_okay_PASS.xml", true));
  }
}
