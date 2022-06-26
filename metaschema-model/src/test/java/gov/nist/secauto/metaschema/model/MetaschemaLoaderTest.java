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

package gov.nist.secauto.metaschema.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

class MetaschemaLoaderTest {

  @Test
  void testUrl() throws MetaschemaException, IOException { // NOPMD - intentional
    MetaschemaLoader loader = new MetaschemaLoader();
    URI metaschemaUri = ObjectUtils.notNull(URI.create(
        "https://raw.githubusercontent.com/usnistgov/OSCAL/v1.0.0/src/metaschema/oscal_complete_metaschema.xml"));
    IMetaschema metaschema = loader.load(metaschemaUri);

    IMetaschema oscalCatalogMetaschema = metaschema.getImportedMetaschemaByShortName("oscal-catalog");
    assertNotNull(oscalCatalogMetaschema, "catalog metaschema not found");
    IMetaschema metadataMetaschema = oscalCatalogMetaschema.getImportedMetaschemaByShortName("oscal-metadata");
    assertNotNull(metadataMetaschema, "metadata metaschema not found");
    IFlagDefinition flag = metadataMetaschema.getScopedFlagDefinitionByName("location-type");
    assertNotNull(flag, "flag not found");
    List<? extends IConstraint> constraints = flag.getConstraints();
    assertFalse(constraints.isEmpty(), "a constraint was expected");
  }

  @Test
  void testFile() throws MetaschemaException, IOException {
    MetaschemaLoader loader = new MetaschemaLoader();
    URI metaschemaUri = ObjectUtils.notNull(
        Paths.get("metaschema/test-suite/docs-models/models_metaschema.xml").toUri());
    IMetaschema metaschema
        = loader.load(metaschemaUri);
    assertFalse(metaschema.getRootAssemblyDefinitions().isEmpty(), "no roots found");
  }
  
  @Test
  void testConstraints() throws MetaschemaException, IOException { // NOPMD - intentional
    ConstraintLoader constraintLoader = new ConstraintLoader();
    IConstraintSet constraintSet = constraintLoader.load(Paths.get("src/test/resources/content/oscal-constraints.xml"));

    MetaschemaLoader loader = new MetaschemaLoader(CollectionUtil.singleton(constraintSet));
    URI metaschemaUri = ObjectUtils.notNull(URI.create(
        "https://raw.githubusercontent.com/usnistgov/OSCAL/v1.0.0/src/metaschema/oscal_complete_metaschema.xml"));
    IMetaschema metaschema = loader.load(metaschemaUri);
    IAssemblyDefinition catalog = metaschema.getExportedAssemblyDefinitionByName("catalog");

    assertNotNull(catalog, "catalog not found");
    List<? extends IConstraint> constraints = catalog.getConstraints();
    assertFalse(constraints.isEmpty(), "a constraint was expected");
  }
}
