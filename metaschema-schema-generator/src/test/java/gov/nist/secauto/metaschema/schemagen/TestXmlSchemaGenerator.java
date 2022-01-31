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

import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.definition.IDefinition;
import gov.nist.secauto.metaschema.model.tree.UsedDefinitionModelWalker;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;

public class TestXmlSchemaGenerator {
  @Disabled
  @Test
  void test() throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
      MetaschemaException, TemplateException {
    XmlSchemaGenerator generator = new XmlSchemaGenerator();

    MetaschemaLoader loader = new MetaschemaLoader();

    List<IMetaschema> metaschemas = new LinkedList<>();
    IMetaschema metaschema
        = loader.loadXmlMetaschema(new File("../../liboscal-java/oscal/src/metaschema/oscal_catalog_metaschema.xml"));
    metaschemas.add(metaschema);
    metaschema
        = loader.loadXmlMetaschema(new File("../../liboscal-java/oscal/src/metaschema/oscal_profile_metaschema.xml"));
    metaschemas.add(metaschema);
    metaschema
        = loader.loadXmlMetaschema(new File("../../liboscal-java/oscal/src/metaschema/oscal_component_metaschema.xml"));
    metaschemas.add(metaschema);
    metaschema
        = loader.loadXmlMetaschema(new File("../../liboscal-java/oscal/src/metaschema/oscal_ssp_metaschema.xml"));
    metaschemas.add(metaschema);
    metaschema = loader
        .loadXmlMetaschema(new File("../../liboscal-java/oscal/src/metaschema/oscal_assessment-plan_metaschema.xml"));
    metaschemas.add(metaschema);
    metaschema = loader.loadXmlMetaschema(
        new File("../../liboscal-java/oscal/src/metaschema/oscal_assessment-results_metaschema.xml"));
    metaschemas.add(metaschema);
    metaschema
        = loader.loadXmlMetaschema(new File("../../liboscal-java/oscal/src/metaschema/oscal_poam_metaschema.xml"));
    metaschemas.add(metaschema);

    Collection<? extends IDefinition> definitions
        = UsedDefinitionModelWalker.collectUsedDefinitionsFromMetaschema(metaschemas);

    try (FileOutputStream fos = new FileOutputStream("schema.out.xsd")) {
      TeeOutputStream out = new TeeOutputStream(System.out, fos);
      generator.generateFromDefinitions(definitions, new OutputStreamWriter(out));
    }
  }
}
