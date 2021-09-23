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

package gov.nist.secauto.metaschema.model.common.metapath.item;

import gov.nist.secauto.metaschema.model.common.metapath.item.ext.IItem;

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

public interface ISequence extends IMetapathResult {
  public static final ISequence EMPTY = new ISequence() {

    @Override
    public List<? extends IItem> asList() {
      return Collections.emptyList();
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public ISequence asSequence() {
      return this;
    }

    @Override
    public Stream<? extends IItem> asStream() {
      return Stream.empty();
    }
  };

  public static ISequence of(IItem item) {
    return new SingletonSequence(item);
  }

  public static ISequence of(List<? extends IItem> items) {
    ISequence retval;
    if (items.isEmpty()) {
      retval = EMPTY;
    } else {
      retval = new ListSequence(items);
    }
    return retval;
  }

  public static ISequence of(Stream<? extends IItem> items) {
    return new StreamSequence(items);
  }

  List<? extends IItem> asList();

  Stream<? extends IItem> asStream();

  boolean isEmpty();

  @Override
  default ISequence asSequence() {
    return this;
  }

  public static Collector<IItem, ?, ISequence> toSequence() {

    return new Collector<IItem, List<IItem>, ISequence>() {

      @Override
      public Supplier<List<IItem>> supplier() {
        return ArrayList::new;
      }

      @Override
      public BiConsumer<List<IItem>, IItem> accumulator() {
        return (list, value) -> list.add(value);
      }

      @Override
      public BinaryOperator<List<IItem>> combiner() {
        return (list1, list2) -> {
          list1.addAll(list2);
          return list1;
        };
      }

      @Override
      public Function<List<IItem>, ISequence> finisher() {
        return list -> {
          ISequence retval;
          if (list.isEmpty()) {
            retval = ISequence.EMPTY;
          } else if (list.size() == 1) {
            retval = new SingletonSequence(list.iterator().next());
          } else {
            retval = new ListSequence(list, false);
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
