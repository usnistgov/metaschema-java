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

package gov.nist.secauto.metaschema.databind.codegen;

import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.umd.cs.findbugs.annotations.NonNull;

class MetaschemaModuleMetaschemaTest
    extends AbstractMetaschemaTest {
  @NonNull
  private static final Path METASCHEMA_FILE
      = ObjectUtils.notNull(Paths.get("../core/metaschema/schema/metaschema/metaschema-module-metaschema.xml"));

  @Test
  void testMetaschemaMetaschema() throws MetaschemaException, IOException, ClassNotFoundException, BindingException {
    runTests(
        ObjectUtils.notNull(METASCHEMA_FILE),
        ObjectUtils.notNull(
            Paths.get("../databind-metaschema/src/main/metaschema-bindings/metaschema-metaschema-bindings.xml")),
        null,
        "gov.nist.secauto.metaschema.databind.model.metaschema.METASCHEMA",
        ObjectUtils.notNull(generationDir),
        null);
  }

  // @Test
  // void testReadMetaschemaAsXml() throws IOException {
  // IBindingContext context = IBindingContext.instance();
  //
  // IDeserializer<METASCHEMA> deserializer = context.newDeserializer(Format.XML,
  // METASCHEMA.class);
  // METASCHEMA metaschema = deserializer.deserialize(METASCHEMA_FILE);
  //
  // ISerializer<METASCHEMA> serializer = context.newSerializer(Format.YAML,
  // METASCHEMA.class);
  // serializer.serialize(metaschema, Paths.get("target/metaschema.yml"));
  // }
}
