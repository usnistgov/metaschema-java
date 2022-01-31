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

package gov.nist.secauto.metaschema.model.common.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class IteratorUtil {
  /**
   * Get an {@link Iterable} for the provided {@link Iterator}.
   * 
   * @param <T>
   *          the type to iterate on
   * @param iterator
   *          the iterator
   * @return the resulting iterable instance
   */
  public static <T> Iterable<T> toIterable(Iterator<T> iterator) {
    Objects.requireNonNull(iterator, "iterator");
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return iterator;
      }
    };
  }

  /**
   * Convert the provided {@link Iterable} to a list of the same generic type.
   * 
   * @param <T>
   *          the collection item's generic type
   * @param iterable
   *          the Iterable to convert to a list
   * @return the list
   */
  public static <T> List<T> toList(Iterable<T> iterable) {
    return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
  }

  /**
   * Convert the provided {@link Iterator} to a list of the same generic type.
   * 
   * @param <T>
   *          the collection item's generic type
   * @param iterator
   *          the Iterator to convert to a list
   * @return the list
   */
  public static <T> List<T> toList(Iterator<T> iterator) {
    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
        .collect(Collectors.toList());
  }

  public static <T> Iterable<T> descendingIterable(List<T> list) {
    return toIterable(descendingIterator(list));
  }

  public static <T> Iterator<T> descendingIterator(List<T> list) {
    Iterator<T> retval;
    if (list instanceof LinkedList) {
      retval = ((LinkedList<T>) list).descendingIterator();
    } else if (list instanceof ArrayList) {
      retval = IntStream.range(0, list.size())
          .map(i -> list.size() - 1 - i)
          .mapToObj(list::get).iterator();
    } else {
      throw new UnsupportedOperationException();
    }
    return retval;
  }
}
