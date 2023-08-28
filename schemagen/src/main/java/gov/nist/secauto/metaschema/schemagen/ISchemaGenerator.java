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

import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.model.IMetaschema;
import gov.nist.secauto.metaschema.schemagen.json.JsonSchemaGenerator;
import gov.nist.secauto.metaschema.schemagen.xml.XmlSchemaGenerator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface ISchemaGenerator {
  /**
   * Generate and write a schema for the provided {@code metaschema} to the
   * {@link Writer} provided by {@code writer} using the provided
   * {@code configuration}.
   *
   * @param metaschema
   *          the Metaschema to generate the schema for
   * @param writer
   *          the writer to use to write the schema
   * @param configuration
   *          the schema generation configuration
   * @throws SchemaGenerationException
   *           if an error occurred while writing the schema
   */
  void generateFromMetaschema(
      @NonNull IMetaschema metaschema,
      @NonNull Writer writer,
      @NonNull IConfiguration<SchemaGenerationFeature<?>> configuration);

  static void generateSchema(
      @NonNull IMetaschema metaschema,
      @NonNull Path destination,
      @NonNull SchemaFormat asFormat,
      @NonNull IConfiguration<SchemaGenerationFeature<?>> configuration)
      throws IOException {
    ISchemaGenerator schemaGenerator = asFormat.getSchemaGenerator();

    try (Writer writer = Files.newBufferedWriter(
        destination,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.TRUNCATE_EXISTING)) {
      assert writer != null;
      schemaGenerator.generateFromMetaschema(metaschema, writer, configuration);
      writer.flush();
    }
  }

  static void generateSchema(
      @NonNull IMetaschema metaschema,
      @NonNull OutputStream os,
      @NonNull SchemaFormat asFormat,
      @NonNull IConfiguration<SchemaGenerationFeature<?>> configuration)
      throws IOException {
    ISchemaGenerator schemaGenerator = asFormat.getSchemaGenerator();

    Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
    schemaGenerator.generateFromMetaschema(metaschema, writer, configuration);
    writer.flush();
    // we don't want to close os, since we do not own it
  }

  /**
   * Identifies the supported schema generation formats.
   */
  enum SchemaFormat {
    /**
     * a JSON Schema.
     */
    JSON(new JsonSchemaGenerator()),
    /**
     * an XML Schema.
     */
    XML(new XmlSchemaGenerator());

    private final ISchemaGenerator schemaGenerator;

    SchemaFormat(@NonNull ISchemaGenerator schemaGenerator) {
      this.schemaGenerator = schemaGenerator;
    }

    public ISchemaGenerator getSchemaGenerator() {
      return schemaGenerator;
    }
  }
}
