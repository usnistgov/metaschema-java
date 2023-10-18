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

package gov.nist.secauto.metaschema.core.metapath.cst;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnBoolean;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IBooleanItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import edu.umd.cs.findbugs.annotations.NonNull;

public class Quantified
    extends AbstractExpression {
  private static final Logger LOGGER = LogManager.getLogger(Quantified.class);

  public enum Quantifier {
    SOME,
    EVERY;
  }

  @NonNull
  private final Quantifier quantifier;
  @NonNull
  private final Map<String, IExpression> inClauses;
  @NonNull
  private final IExpression satisfies;

  public Quantified(
      @NonNull Quantifier quantifier,
      @NonNull Map<String, IExpression> inClauses,
      @NonNull IExpression satisfies) {
    this.quantifier = quantifier;
    this.inClauses = inClauses;
    this.satisfies = satisfies;
  }

  public Quantifier getQuantifier() {
    return quantifier;
  }

  public Map<String, IExpression> getInClauses() {
    return inClauses;
  }

  public IExpression getSatisfies() {
    return satisfies;
  }

  @Override
  public List<? extends IExpression> getChildren() {
    return ObjectUtils.notNull(Stream.concat(inClauses.values().stream(), Stream.of(satisfies))
        .collect(Collectors.toList()));
  }

  @SuppressWarnings("PMD.SystemPrintln")
  @Override
  public ISequence<? extends IItem> accept(DynamicContext dynamicContext, ISequence<?> focus) {
    Map<String, ISequence<? extends IItem>> clauses = getInClauses().entrySet().stream()
        .map(entry -> Map.entry(
            entry.getKey(),
            entry.getValue().accept(dynamicContext, focus)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

    List<String> clauseKeys = new ArrayList<>(clauses.keySet());
    List<? extends Collection<? extends IItem>> clauseValues = new ArrayList<>(clauses.values());

    boolean retval = true;
    for (List<IItem> product : new CartesianProduct<>(clauseValues)) {
      DynamicContext subDynamicContext = dynamicContext.subContext();
      for (int idx = 0; idx < product.size(); idx++) {
        String var = clauseKeys.get(idx);
        IItem item = product.get(idx);

        subDynamicContext.setVariableValue(var, ISequence.of(item));
      }
      boolean result = FnBoolean.fnBooleanAsPrimitive(getSatisfies().accept(subDynamicContext, focus));
      if (Quantifier.EVERY.equals(quantifier) && !result) {
        // fail on first false
        retval = false;
        break;
      } else if (Quantifier.SOME.equals(quantifier)) {
        if (result) {
          // pass on first true
          retval = true;
          break;
        }
        // store (false) result
        retval = false;
      }
    }

    return ISequence.of(IBooleanItem.valueOf(retval));
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitQuantified(this, context);
  }

  public static <T extends IItem> Iterable<List<T>>
      cartesianProduct(final List<? extends Collection<? extends T>> axes) {
    return new CartesianProduct<>(axes);
  }

  // based on https://gist.github.com/jhorstmann/a7aba9947bc4926a75f6de8f69560c6e
  private static class CartesianProductIterator<T extends IItem> implements Iterator<List<T>> {
    private final Object[][] dimensions;
    private final int length;
    private final int[] indizes;
    private boolean reachedMax;

    @SuppressWarnings("PMD.UseVarargs")
    CartesianProductIterator(final Object[][] dimensions) {
      this.dimensions = dimensions;
      this.length = dimensions.length;
      this.indizes = new int[length];
    }

    private void increment(final int index) {
      if (index >= length) {
        reachedMax = true;
      } else {
        indizes[index]++;
        if (indizes[index] == dimensions[index].length) {
          indizes[index] = 0;
          increment(index + 1);
        }
      }
    }

    private void increment() {
      increment(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> next() {
      if (reachedMax) {
        throw new NoSuchElementException();
      }

      List<T> list = new ArrayList<>();
      for (int i = 0; i < length; i++) {
        list.add((T) dimensions[i][indizes[i]]);
      }

      increment();

      return Collections.unmodifiableList(list);
    }

    @Override
    public boolean hasNext() {
      return !reachedMax;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove not supported");
    }
  }

  // based on https://gist.github.com/jhorstmann/a7aba9947bc4926a75f6de8f69560c6e
  private static final class CartesianProduct<T extends IItem> implements Iterable<List<T>> {
    private final Object[][] dimensions;
    private final long size;

    private CartesianProduct(final List<? extends Collection<? extends T>> axes) {
      Object[][] dimensions = new Object[axes.size()][];
      long size = dimensions.length == 0 ? 0 : 1;
      for (int i = 0; i < axes.size(); i++) {
        dimensions[i] = axes.get(i).toArray();
        size *= dimensions[i].length;
      }
      this.dimensions = dimensions;
      this.size = size;
    }

    @Override
    public Iterator<List<T>> iterator() {
      if (size == 0) {
        return Collections.emptyListIterator();
      }
      return new CartesianProductIterator<T>(dimensions);
    }

    public Stream<List<T>> stream() {
      int characteristics = Spliterator.ORDERED | Spliterator.SIZED | Spliterator.IMMUTABLE;
      return StreamSupport.stream(Spliterators.spliterator(iterator(), size, characteristics), false);
    }
  }
}
