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
import gov.nist.secauto.metaschema.databind.model.info.IDataTypeHandler;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;

public class DefaultJsonDeserializer<CLASS>
    extends AbstractDeserializer<CLASS> {
  private JsonFactory jsonFactory;

  /**
   * Construct a new JSON deserializer that will parse the bound class identified
   * by the {@code classBinding}.
   *
   * @param bindingContext
   *          the binding context used to supply bound Java classes while writing
   * @param classBinding
   *          the bound class information for the Java type this deserializer is
   *          operating on
   */
  public DefaultJsonDeserializer(
      @NonNull IBindingContext bindingContext,
      @NonNull IAssemblyClassBinding classBinding) {
    super(bindingContext, classBinding);
  }

  /**
   * Get a JSON factory instance.
   * <p>
   * This method can be used by sub-classes to create a customized factory
   * instance.
   *
   * @return the factory
   */
  @NonNull
  protected JsonFactory newJsonFactoryInstance() {
    return JsonFactoryFactory.instance();
  }

  /**
   * Get the parser factory associated with this deserializer.
   *
   * @return the factory instance
   */
  @NonNull
  protected JsonFactory getJsonFactory() {
    synchronized (this) {
      if (jsonFactory == null) {
        jsonFactory = newJsonFactoryInstance();
      }
      assert jsonFactory != null;
      return jsonFactory;
    }
  }

  /**
   * Using the managed JSON factory, create a new JSON parser instance using the
   * provided reader.
   *
   * @param reader
   *          the reader for the parser to read data from
   * @return the new parser
   * @throws IOException
   *           if an error occurred while creating the parser
   */
  @SuppressWarnings("resource") // reader resource not owned
  @NonNull
  protected final JsonParser newJsonParser(@NonNull Reader reader) throws IOException {
    return ObjectUtils.notNull(getJsonFactory().createParser(reader));
  }

  @SuppressWarnings("null")
  @Override
  protected INodeItem deserializeToNodeItemInternal(@NonNull Reader reader, @NonNull URI documentUri)
      throws IOException {
    INodeItem retval;
    try (JsonParser jsonParser = newJsonParser(reader)) {
      MetaschemaJsonReader parser = new MetaschemaJsonReader(jsonParser);
      IAssemblyClassBinding classBinding = getClassBinding();
      IConfiguration<DeserializationFeature<?>> configuration = getConfiguration();

      if (classBinding.isRoot()
          && configuration.isFeatureEnabled(DeserializationFeature.DESERIALIZE_JSON_ROOT_PROPERTY)) {

        // now parse the root property
        CLASS value = ObjectUtils.requireNonNull(parser.read(classBinding));

        // // we should be at the end object
        // JsonUtil.assertCurrent(parser, JsonToken.END_OBJECT);
        //
        // // advance past the end object
        // JsonToken end = parser.nextToken();

        retval = INodeItemFactory.instance().newDocumentNodeItem(classBinding, documentUri, value);
      } else {
        // Make a temporary data type handler for the top-level definition
        IDataTypeHandler dataTypeHandler = IDataTypeHandler.newDataTypeHandler(classBinding);

        // read the top-level definition
        CLASS value = dataTypeHandler.readItem(null, parser);

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
