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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public interface ISequence<ITEM_TYPE extends IItem> {
  @SuppressWarnings("rawtypes")
  public static final ISequence EMPTY = new EmptyListImpl<>();

  @SuppressWarnings("unchecked")
  @NotNull
  public static <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> empty() {
    return (@NotNull ISequence<ITEM_TYPE>) EMPTY;
  }

  @NotNull
  public static <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> of(@Nullable ITEM_TYPE item) {
    ISequence<ITEM_TYPE> retval;
    if (item == null) {
      retval = empty();
    } else {
      retval = new SingletonSequenceImpl<ITEM_TYPE>(item);
    }
    return retval;
  }

  @NotNull
  public static <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> of(@NotNull List<@NotNull ITEM_TYPE> items) {
    ISequence<ITEM_TYPE> retval;
    if (items.isEmpty()) {
      retval = empty();
    } else {
      retval = new ListSequenceImpl<>(items);
    }
    return retval;
  }

  @NotNull
  public static <ITEM_TYPE extends IItem> ISequence<ITEM_TYPE> of(@NotNull Stream<ITEM_TYPE> items) {
    return new StreamSequenceImpl<ITEM_TYPE>(items);
  }

  @NotNull
  List<@NotNull ITEM_TYPE> asList();

  @NotNull
  Stream<@NotNull ITEM_TYPE> asStream();

  boolean isEmpty();

  int size();

  @NotNull
  public static <ITEM_TYPE extends IItem> Collector<ITEM_TYPE, ?, ISequence<ITEM_TYPE>> toSequence() {

    return new Collector<ITEM_TYPE, List<ITEM_TYPE>, ISequence<ITEM_TYPE>>() {

      @Override
      public Supplier<List<ITEM_TYPE>> supplier() {
        return ArrayList::new;
      }

      @Override
      public BiConsumer<List<ITEM_TYPE>, ITEM_TYPE> accumulator() {
        return (list, value) -> list.add(value);
      }

      @Override
      public BinaryOperator<List<ITEM_TYPE>> combiner() {
        return (list1, list2) -> {
          list1.addAll(list2);
          return list1;
        };
      }

      @SuppressWarnings("null")
      @Override
      public Function<List<ITEM_TYPE>, ISequence<ITEM_TYPE>> finisher() {
        return list -> {
          ISequence<ITEM_TYPE> retval;
          if (list.isEmpty()) {
            retval = ISequence.empty();
          } else if (list.size() == 1) {
            retval = new SingletonSequenceImpl<ITEM_TYPE>(list.iterator().next());
          } else {
            retval = new ListSequenceImpl<ITEM_TYPE>(list, false);
          }
          return retval;
        };
      }

      @Override
      public Set<Characteristics> characteristics() {
        return Collections.emptySet();
      }

    };
  }

}
