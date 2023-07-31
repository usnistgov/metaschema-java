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

package gov.nist.secauto.metaschema.databind.io.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import gov.nist.secauto.metaschema.core.configuration.IConfiguration;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItemFactory;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.AbstractDeserializer;
import gov.nist.secauto.metaschema.databind.io.DeserializationFeature;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.RootAssemblyDefinition;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DefaultJsonDeserializer<CLASS>
    extends AbstractDeserializer<CLASS> {
  private JsonFactory jsonFactory;

  public DefaultJsonDeserializer(@NonNull IBindingContext bindingContext, @NonNull IAssemblyClassBinding classBinding) {
    super(bindingContext, classBinding);
  }

  @NonNull
  protected JsonFactory getJsonFactoryInstance() {
    return JsonFactoryFactory.instance();
  }

  @NonNull
  protected JsonFactory getJsonFactory() {
    synchronized (this) {
      if (jsonFactory == null) {
        jsonFactory = getJsonFactoryInstance();
      }
      assert jsonFactory != null;
      return jsonFactory;
    }
  }

  protected void setJsonFactory(@NonNull JsonFactory jsonFactory) {
    synchronized (this) {
      this.jsonFactory = jsonFactory;
    }
  }

  @NonNull
  protected JsonParser newJsonParser(@NonNull Reader reader) throws IOException {
    return ObjectUtils.notNull(getJsonFactory().createParser(reader));
  }

  @SuppressWarnings("null")
  @Override
  protected INodeItem deserializeToNodeItemInternal(@NonNull Reader reader, @NonNull URI documentUri)
      throws IOException {
    INodeItem retval;
    try (JsonParser parser = newJsonParser(reader)) {
      DefaultJsonParsingContext parsingContext = new DefaultJsonParsingContext(parser, new DefaultJsonProblemHandler());
      IAssemblyClassBinding classBinding = getClassBinding();
      IConfiguration<DeserializationFeature<?>> configuration = getConfiguration();

      if (classBinding.isRoot()
          && configuration.isFeatureEnabled(DeserializationFeature.DESERIALIZE_JSON_ROOT_PROPERTY)) {

        RootAssemblyDefinition root = new RootAssemblyDefinition(classBinding);
        // now parse the root property
        @SuppressWarnings("unchecked") CLASS value = ObjectUtils.requireNonNull((CLASS) root.readRoot(parsingContext));

        // // we should be at the end object
        // JsonUtil.assertCurrent(parser, JsonToken.END_OBJECT);
        //
        // // advance past the end object
        // JsonToken end = parser.nextToken();

        retval = INodeItemFactory.instance().newDocumentNodeItem(root, documentUri, value);
      } else {
        @SuppressWarnings("unchecked") CLASS value
            = ObjectUtils.requireNonNull((CLASS) classBinding.readObject(parsingContext));
        retval = INodeItemFactory.instance().newAssemblyNodeItem(classBinding, documentUri, value);
      }
      return retval;
    }
  }

  @Override
  public CLASS deserializeToValue(@NonNull Reader reader, @NonNull URI documentUri) throws IOException {
    return INodeItem.toValue(deserializeToNodeItemInternal(reader, documentUri));
  }
}
