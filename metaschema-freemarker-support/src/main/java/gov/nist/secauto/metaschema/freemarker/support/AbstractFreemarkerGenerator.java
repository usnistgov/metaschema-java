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

package gov.nist.secauto.metaschema.freemarker.support;

import gov.nist.secauto.metaschema.model.common.IMetaschema;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import freemarker.cache.ClassTemplateLoader;
import freemarker.core.ParseException;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateNotFoundException;
import freemarker.template.Version;

public abstract class AbstractFreemarkerGenerator implements IFreemarkerGenerator {
  private static final Version CONFIG_VERSION = Configuration.VERSION_2_3_30;
  private static final boolean DEBUG = false;

  protected Configuration newConfiguration() {
    // Create your Configuration instance, and specify if up to what FreeMarker
    // version (here 2.3.29) do you want to apply the fixes that are not 100%
    // backward-compatible. See the Configuration JavaDoc for details.
    Configuration cfg = new Configuration(CONFIG_VERSION);

    // // Specify the source where the template files come from. Here I set a
    // // plain directory for it, but non-file-system sources are possible too:
    // cfg.setDirectoryForTemplateLoading(new File("/where/you/store/templates"));
    ClassTemplateLoader ctl = new ClassTemplateLoader(getClass(), "/templates");
    cfg.setTemplateLoader(ctl);

    if (DEBUG) {
      cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);
    } else {
      cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
    cfg.setLogTemplateExceptions(false);

    // Wrap unchecked exceptions thrown during template processing into TemplateException-s:
    cfg.setWrapUncheckedExceptions(true);

    // Do not fall back to higher scopes when reading a null loop variable:
    cfg.setFallbackOnNullLoopVariable(false);

    return cfg;
  }

  @Override
  public void generateFromMetaschema(@NotNull IMetaschema metaschema, Writer out)
      throws TemplateNotFoundException, MalformedTemplateNameException, TemplateException, ParseException, IOException {

    Configuration cfg = newConfiguration();

    // add directives
    cfg.setSharedVariable("toCamelCase", new ToCamelCaseMethod());
    cfg.setSharedVariable("markupToHTML", new MarkupToHtmlMethod());
    cfg.setSharedVariable("markupToMarkdown", new MarkupToMarkdownMethod());

    // add constants
    BeansWrapper wrapper = new BeansWrapperBuilder(CONFIG_VERSION).build();
    TemplateHashModel staticModels = wrapper.getStaticModels();

    // add static values
    cfg.setSharedVariable("statics", staticModels);

    // Create the root hash. We use a Map here, but it could be a JavaBean too.
    Map<String, Object> root = new HashMap<>(); // NOPMD - Freemarker templates run in a single thread

    Template template = getTemplate(cfg);

    // add metaschema model
    buildModel(cfg, root, metaschema);

    template.process(root, out);
  }

  protected abstract Template getTemplate(Configuration cfg)
      throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException;

  protected abstract void buildModel(@NotNull Configuration cfg, @NotNull Map<String, Object> root, @NotNull IMetaschema metaschema) throws IOException, TemplateException;
}
