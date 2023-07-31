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

package gov.nist.secauto.metaschema.databind.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.databind.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IClassBinding extends IBoundModelDefinition {
  @NonNull
  IBindingContext getBindingContext();

  @NonNull
  <CLASS> CLASS newInstance() throws BindingException;

  /**
   * The class this binding is for.
   *
   * @return the bound class
   */
  @NonNull
  Class<?> getBoundClass();

  void callBeforeDeserialize(
      @NonNull Object targetObject,
      @Nullable Object parentObject) throws BindingException;

  void callAfterDeserialize(
      @NonNull Object targetObject,
      @Nullable Object parentObject) throws BindingException;

  // Provides a compatible return value
  @Override
  IBoundFlagInstance getJsonKeyFlagInstance();

  /**
   * Get the class's properties that match the filter.
   *
   * @param flagFilter
   *          a filter to apply or {@code null} if no filtering is needed
   * @return a collection of properties
   */
  @NonNull
  Map<String, ? extends IBoundNamedInstance>
      getNamedInstances(@Nullable Predicate<IBoundFlagInstance> flagFilter);

  /**
   * Reads a JSON/YAML object storing the associated data in the Java object {@code parentInstance}.
   * <p>
   * When called the current {@link JsonToken} of the {@link JsonParser} is expected to be a
   * {@link JsonToken#START_OBJECT}.
   * <p>
   * After returning the current {@link JsonToken} of the {@link JsonParser} is expected to be the
   * next token after the {@link JsonToken#END_OBJECT} for this class.
   *
   * @param parentInstance
   *          the parent Java object to store the data in, which can be {@code null} if there is no
   *          parent
   * @param requiresJsonKey
   *          when {@code true} indicates that the item will have a JSON key
   * @param context
   *          the parsing context
   * @return the instances or an empty list if no data was parsed
   * @throws IOException
   *           if an error occurred while reading the parsed content
   */
  // TODO: check if a boolean return value is needed
  @NonNull
  List<Object> readItem(@Nullable Object parentInstance, boolean requiresJsonKey,
      @NonNull IJsonParsingContext context)
      throws IOException;

  void writeItem(@NonNull Object item, @NonNull QName parentName, @NonNull IXmlWritingContext context)
      throws IOException, XMLStreamException;

  default void writeItem(@NonNull Object item, boolean writeObjectWrapper, @NonNull IJsonWritingContext context)
      throws IOException {
    writeItems(CollectionUtil.singleton(item), writeObjectWrapper, context);
  }

  // for JSON, the entire value needs to be processed to deal with collapsable fields
  void writeItems(@NonNull Collection<? extends Object> items, boolean writeObjectWrapper,
      @NonNull IJsonWritingContext context)
      throws IOException;

  /**
   * Create a deep copy of the provided bound object.
   *
   * @param item
   *          the bound object to copy
   * @param parentInstance
   *          the new object's parent instance or {@code null}
   * @return the copy
   * @throws BindingException
   *           if an error occurred copying content between java instances
   */
  @NonNull
  Object copyBoundObject(@NonNull Object item, Object parentInstance) throws BindingException;
}
