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

package gov.nist.secauto.metaschema.core.metapath.impl;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ICollectionValue;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.core.metapath.function.IArgument;
import gov.nist.secauto.metaschema.core.metapath.function.ISequenceType;
import gov.nist.secauto.metaschema.core.metapath.function.Occurrence;
import gov.nist.secauto.metaschema.core.metapath.function.library.MapGet;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.IMapItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.IMapKey;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractMapItem<VALUE extends ICollectionValue>
    extends ImmutableCollections.AbstractImmutableDelegatedMap<IMapKey, VALUE>
    implements IMapItem<VALUE> {
  @NonNull
  public static final QName QNAME = new QName("map");
  @NonNull
  public static final Set<FunctionProperty> PROPERTIES = ObjectUtils.notNull(
      EnumSet.of(FunctionProperty.DETERMINISTIC));
  @NonNull
  public static final List<IArgument> ARGUMENTS = ObjectUtils.notNull(List.of(
      IArgument.builder().name("key").type(IAnyAtomicItem.class).one().build()));
  @NonNull
  public static final ISequenceType RESULT = ISequenceType.of(IAnyAtomicItem.class, Occurrence.ZERO_OR_ONE);

  @NonNull
  private static final IMapItem<?> EMPTY = new MapItemN<>();

  /**
   * Get an immutable map item that is empty.
   *
   * @param <T>
   *          the item Java type
   * @return the empty map item
   */

  @SuppressWarnings("unchecked")
  @NonNull
  public static <V extends ICollectionValue> IMapItem<V> empty() {
    return (IMapItem<V>) EMPTY;
  }

  @Override
  public ISequence<?> execute(List<ISequence<?>> arguments, DynamicContext dynamicContext,
      ISequence<?> focus) {
    ISequence<? extends IIntegerItem> arg = FunctionUtils.asType(
        ObjectUtils.notNull(arguments.get(0)));

    IAnyAtomicItem key = arg.getFirstItem(true);
    if (key == null) {
      return ISequence.empty(); // NOPMD - readability
    }

    ICollectionValue result = MapGet.get(this, key);
    return result == null ? ISequence.empty() : result.asSequence();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getValue());
  }

  @Override
  public boolean equals(Object other) {
    return other == this
        || other instanceof IMapItem && getValue().equals(((IMapItem<?>) other).getValue());
  }

  @Override
  public String asString() {
    return ObjectUtils.notNull(toString());
  }
}
