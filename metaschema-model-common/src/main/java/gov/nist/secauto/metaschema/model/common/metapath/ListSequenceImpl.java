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

package gov.nist.secauto.metaschema.model.common.metapath;

import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

class ListSequenceImpl<ITEM_TYPE extends IItem> implements ISequence<ITEM_TYPE> {
  @NonNull
  private final List<ITEM_TYPE> items;

  public ListSequenceImpl(@NonNull Collection<ITEM_TYPE> items) {
    this(new ArrayList<>(items), false);
  }

  public ListSequenceImpl(@NonNull List<ITEM_TYPE> items, boolean copy) {
    this.items = CollectionUtil.unmodifiableList(copy ? new ArrayList<>(items) : items);
  }

  @Override
  public List<ITEM_TYPE> asList() {
    return items;
  }

  @Override
  public Stream<ITEM_TYPE> asStream() {
    return ObjectUtils.notNull(items.stream());
  }

  @Override
  public boolean isEmpty() {
    return items.isEmpty();
  }

  @Override
  public String toString() {
    return asList().toString();
  }

  @Override
  public int size() {
    return items.size();
  }

  @Override
  public void forEach(Consumer<? super ITEM_TYPE> action) {
    items.forEach(action);
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) {
      return true; // NOPMD - readability
    }

    if (!(other instanceof ISequence)) {
      return false; // NOPMD - readability
    }

    return asList().equals(((ISequence<?>) other).asList());
  }

  @Override
  public int hashCode() {
    return asList().hashCode();
  }
}
