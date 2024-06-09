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

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.info.IItemReadHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemWriteHandler;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents a field definition bound to a Java class.
 * <p>
 * This definition is considered "complex", since it is bound to a Java class.
 */
public interface IBoundDefinitionModelFieldComplex
    extends IBoundDefinitionModelField, IBoundDefinitionModelComplex {

  // Complex Field Definition Features
  // =================================

  @Override
  @NonNull
  default IBoundDefinitionModelFieldComplex getDefinition() {
    return this;
  }

  @Override
  default Object getDefaultValue() {
    Object retval = null;
    IBoundDefinitionModelFieldComplex definition = getDefinition();
    IBoundFieldValue fieldValue = definition.getFieldValue();

    Object fieldValueDefault = fieldValue.getDefaultValue();
    if (fieldValueDefault != null) {
      retval = definition.newInstance();
      fieldValue.setValue(retval, fieldValueDefault);

      // since the field value is non-null, populate the flags
      for (IBoundInstanceFlag flag : definition.getFlagInstances()) {
        Object flagDefault = flag.getResolvedDefaultValue();
        if (flagDefault != null) {
          flag.setValue(retval, flagDefault);
        }
      }
    }
    return retval;
  }

  /**
   * Get the bound field value associated with this field.
   *
   * @return the field's value binding
   */
  @NonNull
  IBoundFieldValue getFieldValue();

  @Override
  @NonNull
  default Object getFieldValue(@NonNull Object item) {
    return ObjectUtils.requireNonNull(getFieldValue().getValue(item));
  }

  @Override
  @NonNull
  default String getJsonValueKeyName() {
    return getFieldValue().getJsonValueKeyName();
  }

  @Override
  @NonNull
  default IDataTypeAdapter<?> getJavaTypeAdapter() {
    return getFieldValue().getJavaTypeAdapter();
  }

  @Override
  @NonNull
  default Map<String, IBoundProperty> getJsonProperties(@Nullable Predicate<IBoundInstanceFlag> flagFilter) {
    Predicate<IBoundInstanceFlag> actualFlagFilter = flagFilter;

    IBoundFieldValue fieldValue = getFieldValue();
    IBoundInstanceFlag jsonValueKey = getDefinition().getJsonValueKeyFlagInstance();
    if (jsonValueKey != null) {
      Predicate<IBoundInstanceFlag> jsonValueKeyFilter = flag -> !flag.equals(jsonValueKey);
      actualFlagFilter = actualFlagFilter == null ? jsonValueKeyFilter : actualFlagFilter.and(jsonValueKeyFilter);
      // ensure the field value is omitted too!
      fieldValue = null;
    }

    Stream<? extends IBoundInstanceFlag> flagStream = getFlagInstances().stream();
    if (actualFlagFilter != null) {
      flagStream = flagStream.filter(actualFlagFilter);
    }

    if (fieldValue != null) {
      // determine if we use the field value or not
      Collection<? extends IBoundInstanceFlag> flagInstances = flagStream
          .collect(Collectors.toList());

      if (flagInstances.isEmpty()) {
        // no relevant flags, so this field should expect a scalar value
        fieldValue = null;
      }
      flagStream = flagInstances.stream();
    }

    Stream<? extends IBoundProperty> resultStream = fieldValue == null
        ? flagStream
        : Stream.concat(flagStream, Stream.of(getFieldValue()));

    return ObjectUtils.notNull(resultStream
        .collect(Collectors.toUnmodifiableMap(IBoundProperty::getJsonName, Function.identity())));
  }

  @Override
  @NonNull
  default Object readItem(Object parent, IItemReadHandler handler) throws IOException {
    return handler.readItemField(parent, this);
  }

  @Override
  default void writeItem(Object item, IItemWriteHandler handler) throws IOException {
    handler.writeItemField(item, this);
  }

  @Override
  default boolean canHandleXmlQName(QName qname) {
    // not handled, since not root
    return false;
  }
}
