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

import gov.nist.secauto.metaschema.core.configuration.DefaultConfiguration;
import gov.nist.secauto.metaschema.core.configuration.IMutableConfiguration;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.metaschema.BindingModuleLoader;
import gov.nist.secauto.metaschema.schemagen.json.JsonSchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.xml.XmlSchemaGenerator;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import edu.umd.cs.findbugs.annotations.NonNull;

class MetaschemaModuleTest {
  @NonNull
  private static final Path METASCHEMA_FILE
      = ObjectUtils.notNull(Paths.get("../core/metaschema/schema/metaschema/metaschema-module-metaschema.xml"));

  @Test
  void testGenerateMetaschemaModuleJson() throws MetaschemaException, IOException {
    BindingModuleLoader loader = new BindingModuleLoader();

    IModule module = loader.load(METASCHEMA_FILE);

    IMutableConfiguration<SchemaGenerationFeature<?>> features
        = new DefaultConfiguration<>();
    features.enableFeature(SchemaGenerationFeature.INLINE_DEFINITIONS);
    // features.disableFeature(SchemaGenerationFeature.INLINE_DEFINITIONS);

    try (Writer writer = Files.newBufferedWriter(
        Path.of("target/metaschema-schema.json"),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      assert writer != null;
      ISchemaGenerator schemaGenerator = new JsonSchemaGenerator();
      schemaGenerator.generateFromModule(module, writer, features);
    }
  }

  @Test
  void testGenerateMetaschemaModuleXml() throws MetaschemaException, IOException {
    BindingModuleLoader loader = new BindingModuleLoader();

    IModule module = loader.load(METASCHEMA_FILE);

    IMutableConfiguration<SchemaGenerationFeature<?>> features
        = new DefaultConfiguration<>();
    features.enableFeature(SchemaGenerationFeature.INLINE_DEFINITIONS);
    // features.disableFeature(SchemaGenerationFeature.INLINE_DEFINITIONS);

    try (Writer writer = Files.newBufferedWriter(
        Path.of("target/metaschema-schema.xsd"),
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      assert writer != null;
      ISchemaGenerator schemaGenerator = new XmlSchemaGenerator();
      schemaGenerator.generateFromModule(module, writer, features);
    }
  }
}
