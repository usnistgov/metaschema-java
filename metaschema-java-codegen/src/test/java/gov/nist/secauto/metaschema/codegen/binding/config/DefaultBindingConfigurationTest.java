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

import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.IFlagContainer;
import gov.nist.secauto.metaschema.model.common.ModelType;

import org.jmock.Expectations;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

class DefaultBindingConfigurationTest {
  private static final URI METASCHEMA_LOCATION
      = new File("src/test/resources/metaschema/metaschema.xml").getAbsoluteFile().toURI();
  private static final String DEFINITION_NAME = "grandchild";
  private static final ModelType DEFINITION_MODEL_TYPE = ModelType.ASSEMBLY;
  private static final String DEFINITION__CLASS_NAME = "TheChild";

  @RegisterExtension
  JUnit5Mockery context = new JUnit5Mockery();
  private final IFlagContainer definition = context.mock(IFlagContainer.class);
  private final IMetaschema metaschema = context.mock(IMetaschema.class);

  @Test
  void testLoader() throws MalformedURLException, IOException {
    File bindingConfigFile = new File("src/test/resources/metaschema/binding-config.xml");

    DefaultBindingConfiguration config = new DefaultBindingConfiguration();
    config.load(bindingConfigFile);

    assertEquals("gov.nist.itl.metaschema.codegen.xml.example.assembly",
        config.getPackageNameForNamespace("http://csrc.nist.gov/ns/metaschema/testing/assembly"));

    context.checking(new Expectations() {
      { // NOPMD - intentional
        oneOf(metaschema).getLocation();
        will(returnValue(METASCHEMA_LOCATION));
        allowing(definition).getContainingMetaschema();
        will(returnValue(metaschema));
        allowing(definition).getModelType();
        will(returnValue(DEFINITION_MODEL_TYPE));
        allowing(definition).getName();
        will(returnValue(DEFINITION_NAME));
      }
    });
    IDefinitionBindingConfiguration defConfig = config.getBindingConfigurationForDefinition(definition);
    assertNotNull(defConfig);
    assertEquals(DEFINITION__CLASS_NAME, defConfig.getClassName());
  }

}
