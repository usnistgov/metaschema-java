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

package gov.nist.secauto.metaschema.codegen.binding.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import gov.nist.secauto.metaschema.model.MetaschemaException;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

class BindingConfigurationLoaderTest {

  @Test
  void testDefault() throws MalformedURLException, IOException, MetaschemaException {
    DefaultBindingConfiguration config = new DefaultBindingConfiguration();

    // test namespaces
    Map<String, String> namespaceToPackageName = new HashMap<>();
    namespaceToPackageName.put("http://csrc.nist.gov/ns/metaschema/testing/assembly",
        "gov.nist.csrc.ns.metaschema.testing.assembly");

    for (Map.Entry<String, String> entry : namespaceToPackageName.entrySet()) {
      assertEquals(entry.getValue(), config.getPackageNameForNamespace(entry.getKey()));
    }
  }

  @Test
  void testConfiguredNamespace() throws MalformedURLException, IOException, MetaschemaException {
    DefaultBindingConfiguration config = new DefaultBindingConfiguration();

    // test namespaces
    Map<String, String> namespaceToPackageName = new HashMap<>();
    namespaceToPackageName.put("http://csrc.nist.gov/ns/metaschema/testing/assembly",
        "gov.nist.secauto.metaschema.testing.assembly");

    for (Map.Entry<String, String> entry : namespaceToPackageName.entrySet()) {
      config.addModelBindingConfig(entry.getKey(), entry.getValue());
    }

    for (Map.Entry<String, String> entry : namespaceToPackageName.entrySet()) {
      assertEquals(entry.getValue(), config.getPackageNameForNamespace(entry.getKey()));
    }
  }

  @Test
  void test() throws MalformedURLException, IOException, MetaschemaException {
    File configFile = new File("src/main/metaschema-bindings/oscal-metaschema-bindings.xml");
    DefaultBindingConfiguration config = new DefaultBindingConfiguration();
    config.load(configFile);
    assertNotNull(config);
  }

}
