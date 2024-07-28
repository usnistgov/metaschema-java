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

import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A Metapath sequence supporting an unbounded number of items.
 *
 * @param <ITEM>
 *          the Java type of the items
 */
public class SequenceN<ITEM extends IItem>
    extends AbstractSequence<ITEM> {
  @NonNull
  private final List<ITEM> items;

  /**
   * Construct a new sequence with the provided items.
   *
   * @param items
   *          a collection containing the items to add to the sequence
   * @param copy
   *          if {@code true} make a defensive copy of the list or {@code false}
   *          otherwise
   */
  public SequenceN(@NonNull List<ITEM> items, boolean copy) {
    this.items = CollectionUtil.unmodifiableList(copy ? new ArrayList<>(items) : items);
  }

  /**
   * Construct a new sequence with the provided items.
   *
   * @param items
   *          the items to add to the sequence
   */
  @SafeVarargs
  public SequenceN(@NonNull ITEM... items) {
    this(ObjectUtils.notNull(List.of(items)), false);
  }

  /**
   * Construct a new sequence with the provided items.
   *
   * @param items
   *          a collection containing the items to add to the sequence
   */
  public SequenceN(@NonNull Collection<ITEM> items) {
    this(new ArrayList<>(items), false);
  }

  /**
   * Construct a new sequence with the provided items.
   *
   * @param items
   *          a list containing the items to add to the sequence
   */
  public SequenceN(@NonNull List<ITEM> items) {
    this(items, false);
  }

  @Override
  public List<ITEM> getValue() {
    return items;
  }
}
