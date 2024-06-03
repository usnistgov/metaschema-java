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

import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * This implementation is inspired by the similar implementation provided by the
 * JDK.
 */
public class ImmutableCollections {
  protected static UnsupportedOperationException unsupported() {
    return new UnsupportedOperationException("method not supported");
  }

  public static abstract class AbstractImmutableCollection<T>
      extends AbstractCollection<T> {

    @Override
    public final boolean add(T e) {
      throw unsupported();
    }

    @Override
    public final boolean addAll(Collection<? extends T> c) {
      throw unsupported();
    }

    @Override
    public final void clear() {
      throw unsupported();
    }

    @Override
    public final boolean remove(Object o) {
      throw unsupported();
    }

    @Override
    public final boolean removeAll(Collection<?> c) {
      throw unsupported();
    }

    @Override
    public final boolean removeIf(Predicate<? super T> filter) {
      throw unsupported();
    }

    @Override
    public final boolean retainAll(Collection<?> c) {
      throw unsupported();
    }
  }

  public static abstract class AbstractImmutableList<T>
      extends AbstractImmutableCollection<T>
      implements List<T> {

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
      throw unsupported();
    }

    @Override
    public T set(int index, T element) {
      throw unsupported();
    }

    @Override
    public void add(int index, T element) {
      throw unsupported();
    }

    @Override
    public T remove(int index) {
      throw unsupported();
    }
  }

  public static abstract class AbstractImmutableDelegatedCollection<T>
      extends AbstractImmutableList<T> {

    @NonNull
    public abstract List<T> getValue();

    @Override
    public T get(int index) {
      return getValue().get(index);
    }

    @Override
    public int indexOf(Object obj) {
      return getValue().indexOf(obj);
    }

    @Override
    public Iterator<T> iterator() {
      return getValue().iterator();
    }

    @Override
    public int lastIndexOf(Object obj) {
      return getValue().lastIndexOf(obj);
    }

    @Override
    public ListIterator<T> listIterator() {
      return getValue().listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
      return getValue().listIterator(index);
    }

    @Override
    public int size() {
      return getValue().size();
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
      return getValue().subList(fromIndex, toIndex);
    }

    @Override
    public Stream<T> stream() {
      return ObjectUtils.notNull(getValue().stream());
    }

    @Override
    public String toString() {
      return getValue().toString();
    }
  }
}
