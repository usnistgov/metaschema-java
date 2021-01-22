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

import gov.nist.secauto.metaschema.model.Metaschema;
import gov.nist.secauto.metaschema.model.MetaschemaException;
import gov.nist.secauto.metaschema.model.MetaschemaLoader;
import gov.nist.secauto.metaschema.model.definitions.InfoElementDefinition;
import gov.nist.secauto.metaschema.model.util.UsedDefinitionModelWalker;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import freemarker.cache.ClassTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;

public class TestGen {
  @Test
  void test() throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException,
      MetaschemaException, TemplateException {

    // Create your Configuration instance, and specify if up to what FreeMarker
    // version (here 2.3.29) do you want to apply the fixes that are not 100%
    // backward-compatible. See the Configuration JavaDoc for details.
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

    // // Specify the source where the template files come from. Here I set a
    // // plain directory for it, but non-file-system sources are possible too:
    // cfg.setDirectoryForTemplateLoading(new File("/where/you/store/templates"));
    ClassTemplateLoader ctl = new ClassTemplateLoader(getClass(), "/templates");
    cfg.setTemplateLoader(ctl);

    // From here we will set the settings recommended for new projects. These
    // aren't the defaults for backward compatibilty.

    // Set the preferred charset template files are stored in. UTF-8 is
    // a good choice in most applications:
    cfg.setDefaultEncoding("UTF-8");

    // Sets how errors will appear.
    // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

    // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
    cfg.setLogTemplateExceptions(false);

    // Wrap unchecked exceptions thrown during template processing into TemplateException-s:
    cfg.setWrapUncheckedExceptions(true);

    // Do not fall back to higher scopes when reading a null loop variable:
    cfg.setFallbackOnNullLoopVariable(false);

    // Create the root hash. We use a Map here, but it could be a JavaBean too.
    Map<String, Object> root = new HashMap<>();

    MetaschemaLoader loader = new MetaschemaLoader();

    Metaschema metaschema
        = loader.loadXmlMetaschema(new File("../../liboscal-java/oscal/src/metaschema/oscal_catalog_metaschema.xml"));
    
    root.put("metaschema", metaschema);

    Collection<InfoElementDefinition> definitions = UsedDefinitionModelWalker.collectUsedDefinitions(metaschema);
    root.put("definitions", definitions);

    Template temp = cfg.getTemplate("test.ftlx");

    Writer out = new OutputStreamWriter(System.out);
    temp.process(root, out);
  }
}
