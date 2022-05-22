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

package gov.nist.secauto.metaschema.binding.io.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.AbstractDeserializer;
import gov.nist.secauto.metaschema.binding.io.DeserializationFeature;
import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.RootAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.configuration.IConfiguration;
import gov.nist.secauto.metaschema.model.common.metapath.item.DefaultNodeItemFactory;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

public class DefaultJsonDeserializer<CLASS>
    extends AbstractDeserializer<CLASS> {
  private JsonFactory jsonFactory;

  public DefaultJsonDeserializer(@NotNull IBindingContext bindingContext, @NotNull IAssemblyClassBinding classBinding) {
    super(bindingContext, classBinding);
  }

  @SuppressWarnings("null")
  @NotNull
  protected JsonFactory getJsonFactoryInstance() {
    return JsonFactoryFactory.instance();
  }

  @SuppressWarnings("null")
  @NotNull
  protected JsonFactory getJsonFactory() {
    synchronized (this) {
      if (jsonFactory == null) {
        jsonFactory = getJsonFactoryInstance();
      }
      return jsonFactory;
    }
  }

  protected void setJsonFactory(@NotNull JsonFactory jsonFactory) {
    synchronized (this) {
      this.jsonFactory = jsonFactory;
    }
  }

  @NotNull
  protected JsonParser newJsonParser(@NotNull Reader reader) throws IOException {
    JsonParser retval = getJsonFactory().createParser(reader);
    retval.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
    return retval;

  }

  @SuppressWarnings("null")
  @Override
  protected INodeItem deserializeToNodeItemInternal(@NotNull Reader reader, @NotNull URI documentUri)
      throws IOException {
    INodeItem retval;
    try (JsonParser parser = newJsonParser(reader)) {
      DefaultJsonParsingContext parsingContext = new DefaultJsonParsingContext(parser, new DefaultJsonProblemHandler());
      IAssemblyClassBinding classBinding = getClassBinding();
      IConfiguration<DeserializationFeature> configuration = getConfiguration();

      if (classBinding.isRoot()
          && configuration.isFeatureEnabled(DeserializationFeature.DESERIALIZE_JSON_ROOT_PROPERTY)) {

        RootAssemblyDefinition root = new RootAssemblyDefinition(classBinding);
        // now parse the root property
        @SuppressWarnings("unchecked")
        CLASS value = ObjectUtils.requireNonNull((CLASS) root.readRoot(parsingContext));

        // // we should be at the end object
        // JsonUtil.assertCurrent(parser, JsonToken.END_OBJECT);
        //
        // // advance past the end object
        // JsonToken end = parser.nextToken();

        retval = DefaultNodeItemFactory.instance().newDocumentNodeItem(root, value, documentUri);
      } else {
        @SuppressWarnings("unchecked")
        CLASS value = ObjectUtils.requireNonNull((CLASS) classBinding.readObject(parsingContext));
        retval = DefaultNodeItemFactory.instance().newAssemblyNodeItem(classBinding, value, documentUri);
      }
      return retval;
    }
  }
}
