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

package gov.nist.secauto.metaschema.schemagen.xml;

import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.schemagen.json.ISchemaGenerator;

import org.apache.commons.io.output.TeeOutputStream;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;

public class TestXmlSchemaGenerator {
  @Test
  void test() throws IOException, MetaschemaException {
    ISchemaGenerator generator = new XmlSchemaGenerator();

    MetaschemaLoader loader = new MetaschemaLoader();

    IMetaschema metaschema
        = loader.loadXmlMetaschema(Paths.get("../../OSCAL/src/metaschema/oscal_complete_metaschema.xml"));
    // IMetaschema metaschema
    // = loader.loadXmlMetaschema(new
    // URL("https://raw.githubusercontent.com/usnistgov/OSCAL/main/src/metaschema/oscal_catalog_metaschema.xml"));
    // metaschema
    // = loader.loadXmlMetaschema(new
    // File("../../liboscal-java/oscal/src/metaschema/oscal_profile_metaschema.xml"));
    // metaschemas.add(metaschema);
    // metaschema
    // = loader.loadXmlMetaschema(new
    // File("../../liboscal-java/oscal/src/metaschema/oscal_component_metaschema.xml"));
    // metaschemas.add(metaschema);
    // metaschema
    // = loader.loadXmlMetaschema(new
    // File("../../liboscal-java/oscal/src/metaschema/oscal_ssp_metaschema.xml"));
    // metaschemas.add(metaschema);
    // metaschema = loader
    // .loadXmlMetaschema(new
    // File("../../liboscal-java/oscal/src/metaschema/oscal_assessment-plan_metaschema.xml"));
    // metaschemas.add(metaschema);
    // metaschema = loader.loadXmlMetaschema(
    // new File("../../liboscal-java/oscal/src/metaschema/oscal_assessment-results_metaschema.xml"));
    // metaschemas.add(metaschema);
    // metaschema
    // = loader.loadXmlMetaschema(new
    // File("../../liboscal-java/oscal/src/metaschema/oscal_poam_metaschema.xml"));
    // metaschemas.add(metaschema);

//    IAssemblyDefinition part = metaschema.getExportedAssemblyDefinitionMap().get("metadata");
//    Collection<? extends IModelInstance> model = part.getModelInstances();
//    IModelInstance instance = model.toArray(new IModelInstance[0])[0];
//    IFieldInstance field = (IFieldInstance) instance;

    try (FileOutputStream fos = new FileOutputStream("schema.out.xsd")) {
      TeeOutputStream out = new TeeOutputStream(System.out, fos);
      generator.generateFromMetaschema(metaschema, new OutputStreamWriter(out));
    }
  }
}
