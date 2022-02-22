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
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.io.AbstractDeserializer;
import gov.nist.secauto.metaschema.binding.io.Feature;
import gov.nist.secauto.metaschema.binding.metapath.xdm.IBoundXdmNodeItem;
import gov.nist.secauto.metaschema.binding.metapath.xdm.IXdmFactory;
import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.RootDefinitionAssemblyProperty;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

public class DefaultJsonDeserializer<CLASS>
    extends AbstractDeserializer<CLASS> {
  private JsonFactory jsonFactory;

  public DefaultJsonDeserializer(IBindingContext bindingContext, IAssemblyClassBinding classBinding) {
    super(bindingContext, classBinding);
  }

  protected JsonFactory getJsonFactoryInstance() {
    return JsonFactoryFactory.instance();
  }

  protected JsonFactory getJsonFactory() {
    synchronized (this) {
      if (jsonFactory == null) {
        jsonFactory = getJsonFactoryInstance();
      }
      return jsonFactory;
    }
  }

  protected void setJsonFactory(JsonFactory jsonFactory) {
    synchronized (this) {
      this.jsonFactory = jsonFactory;
    }
  }

  protected JsonParser newJsonParser(Reader reader) throws IOException {
    JsonFactory factory = getJsonFactory();
    JsonParser retval = factory.createParser(reader);
    return retval;
  }

  @Override
  protected IBoundXdmNodeItem deserializeToNodeItemInternal(Reader reader, @Nullable URI documentUri)
      throws IOException {
    try (JsonParser parser = newJsonParser(reader)) {

      DefaultJsonParsingContext parsingContext = new DefaultJsonParsingContext(parser, new DefaultJsonProblemHandler());

      IAssemblyClassBinding classBinding = getClassBinding();
      CLASS retval;
      IBoundXdmNodeItem parsedNodeItem;
      if (classBinding.isRoot() && getConfiguration().isFeatureEnabled(Feature.DESERIALIZE_ROOT)) {
        RootDefinitionAssemblyProperty property = new RootDefinitionAssemblyProperty(classBinding);
        // first read the initial START_OBJECT
        JsonUtil.consumeAndAssert(parser, JsonToken.START_OBJECT);
        JsonUtil.consumeAndAssert(parser, JsonToken.FIELD_NAME);

        @SuppressWarnings("unchecked")
        CLASS value = (CLASS) property.read(parsingContext);
        retval = value;

        // first read the initial START_OBJECT
        JsonUtil.assertAndAdvance(parser, JsonToken.END_OBJECT);
        parsedNodeItem = IXdmFactory.INSTANCE.newDocumentNodeItem(property, retval, documentUri);
      } else {
        @SuppressWarnings("unchecked")
        CLASS value = (CLASS) classBinding.readItem(null, parsingContext);
        retval = value;
        parsedNodeItem = IXdmFactory.INSTANCE.newRelativeAssemblyNodeItem(classBinding, retval, documentUri);
      }
      return parsedNodeItem;
    }
  }

}
