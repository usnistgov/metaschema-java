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

package gov.nist.secauto.metaschema.databind;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IConstraintLoader;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.model.xml.ExternalConstraintsModulePostProcessor;
import gov.nist.secauto.metaschema.core.model.xml.XmlConstraintLoader;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundModule;
import gov.nist.secauto.metaschema.databind.model.metaschema.BindingConstraintLoader;
import gov.nist.secauto.metaschema.databind.model.test.TestMetaschema;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.namespace.QName;

class DefaultBindingContextTest {

  @Test
  void testConstraints() throws MetaschemaException, IOException { // NOPMD - intentional
    IConstraintLoader constraintLoader = new XmlConstraintLoader();
    IConstraintSet constraintSet = constraintLoader.load(
        ObjectUtils.notNull(Paths.get("src/test/resources/content/constraints.xml")));

    ExternalConstraintsModulePostProcessor postProcessor
        = new ExternalConstraintsModulePostProcessor(CollectionUtil.singleton(constraintSet));
    IBindingContext bindingContext = new DefaultBindingContext(CollectionUtil.singletonList(postProcessor));
    IBoundModule module = bindingContext.registerModule(TestMetaschema.class);

    IAssemblyDefinition root
        = module.getExportedAssemblyDefinitionByName(new QName("https://csrc.nist.gov/ns/test/xml", "root"));

    assertNotNull(root, "root not found");
    List<? extends IConstraint> constraints = root.getConstraints();
    assertFalse(constraints.isEmpty(), "a constraint was expected");
  }

  @Test
  void testConstraintsUsingBinding() throws MetaschemaException, IOException { // NOPMD - intentional
    IConstraintLoader constraintLoader = new BindingConstraintLoader();
    IConstraintSet constraintSet = constraintLoader.load(
        ObjectUtils.notNull(Paths.get("src/test/resources/content/constraints.xml")));

    ExternalConstraintsModulePostProcessor postProcessor
        = new ExternalConstraintsModulePostProcessor(CollectionUtil.singleton(constraintSet));
    IBindingContext bindingContext = new DefaultBindingContext(CollectionUtil.singletonList(postProcessor));
    IBoundModule module = bindingContext.registerModule(TestMetaschema.class);

    IAssemblyDefinition root
        = module.getExportedAssemblyDefinitionByName(new QName("https://csrc.nist.gov/ns/test/xml", "root"));

    assertNotNull(root, "root not found");
    List<? extends IConstraint> constraints = root.getConstraints();
    assertFalse(constraints.isEmpty(), "a constraint was expected");
  }

}
