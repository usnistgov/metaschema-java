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

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.info.IItemReadHandler;
import gov.nist.secauto.metaschema.databind.model.info.IItemWriteHandler;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents an assembly definition bound to a Java class.
 */
public interface IBoundDefinitionModelAssembly
    extends IBoundDefinitionModelComplex, IBoundContainerModelAssembly, IAssemblyDefinition {

  // Assembly Definition Features
  // ============================
  @Override
  @NonNull
  default IBoundDefinitionModelAssembly getOwningDefinition() {
    return this;
  }

  @Override
  @NonNull
  default IBoundDefinitionModelAssembly getDefinition() {
    return this;
  }

  @Override
  @Nullable
  default IBoundInstanceModelAssembly getInlineInstance() {
    // never inline
    return null;
  }
  //
  // @Override
  // @NonNull
  // default QName getXmlQName() {
  // return ObjectUtils.requireNonNull(getRootXmlQName());
  // }

  @Override
  @NonNull
  default Map<String, IBoundProperty> getJsonProperties(@Nullable Predicate<IBoundInstanceFlag> flagFilter) {
    Stream<? extends IBoundInstanceFlag> flagStream = getFlagInstances().stream();

    if (flagFilter != null) {
      flagStream = flagStream.filter(flagFilter);
    }

    return ObjectUtils.notNull(Stream.concat(flagStream, getModelInstances().stream())
        .collect(Collectors.toUnmodifiableMap(IBoundProperty::getJsonName, Function.identity())));
  }

  @Override
  @NonNull
  default Object readItem(@Nullable Object parent, @NonNull IItemReadHandler handler) throws IOException {
    return handler.readItemAssembly(parent, this);
  }

  @Override
  default void writeItem(Object item, IItemWriteHandler handler) throws IOException {
    handler.writeItemAssembly(item, this);
  }

  @Override
  default boolean canHandleXmlQName(@NonNull QName qname) {
    return qname.equals(getRootXmlQName());
  }
}
