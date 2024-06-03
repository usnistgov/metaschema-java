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
import gov.nist.secauto.metaschema.core.metapath.ICollectionValue;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.InvalidTypeMetapathException;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.ArrayException;
import gov.nist.secauto.metaschema.core.metapath.item.function.IArrayItem;

import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractLookup implements IExpression {
  @NonNull
  private final IKeySpecifier keySpecifier;

  protected AbstractLookup(@NonNull IKeySpecifier keySpecifier) {
    this.keySpecifier = keySpecifier;
  }

  @NonNull
  public IKeySpecifier getKeySpecifier() {
    return keySpecifier;
  }

  protected interface IKeySpecifier {

    default Stream<? extends ICollectionValue> lookup(
        @NonNull IItem item,
        @NonNull DynamicContext dynamicContext,
        @NonNull ISequence<?> focus) {
      Stream<? extends ICollectionValue> result;
      if (item instanceof IArrayItem) {
        result = lookupInArray((IArrayItem<?>) item, dynamicContext, focus);
      } else {
        throw new InvalidTypeMetapathException(item,
            String.format("Item type '%s' is not an array or map.", item.getClass().getName()));
      }
      return result;
    }

    Stream<? extends ICollectionValue> lookupInArray(
        @NonNull IArrayItem<?> item,
        @NonNull DynamicContext dynamicContext,
        @NonNull ISequence<?> focus);
  }

  protected static class NCNameKeySpecifier implements IKeySpecifier {
    @NonNull
    private final String name;

    public NCNameKeySpecifier(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    @Override
    public Stream<? extends IItem> lookupInArray(
        IArrayItem<?> item,
        DynamicContext dynamicContext,
        ISequence<?> focus) {
      throw new InvalidTypeMetapathException(item,
          String.format("The key name-based lookup '%s' is not appropriate for an array.", getName()));
    }
  }

  protected static class IntegerLiteralKeySpecifier implements IKeySpecifier {
    private final int index;

    public IntegerLiteralKeySpecifier(IIntegerItem literal) {
      index = literal.asInteger().intValueExact() - 1;
    }

    @Override
    public Stream<? extends ICollectionValue> lookupInArray(
        IArrayItem<?> item,
        DynamicContext dynamicContext,
        ISequence<?> focus) {
      try {
        return Stream.of(item.get(index));
      } catch (IndexOutOfBoundsException ex) {
        throw new ArrayException(
            ArrayException.INDEX_OUT_OF_BOUNDS,
            String.format("The index %d is outside the range of values for the array size '%d'.",
                index + 1,
                item.size()),
            ex);
      }
    }
  }

  protected static class WildcardKeySpecifier implements IKeySpecifier {

    @Override
    public Stream<? extends ICollectionValue> lookupInArray(
        IArrayItem<?> item,
        DynamicContext dynamicContext,
        ISequence<?> focus) {
      return item.stream();
    }
  }

  public static class ParenthesizedExprKeySpecifier implements IKeySpecifier {
    @NonNull
    private final IExpression keyExpression;

    public ParenthesizedExprKeySpecifier(@NonNull IExpression keyExpression) {
      this.keyExpression = keyExpression;
    }

    public IExpression getKeyExpression() {
      return keyExpression;
    }

    @Override
    public Stream<? extends ICollectionValue> lookupInArray(
        IArrayItem<?> item,
        DynamicContext dynamicContext,
        ISequence<?> focus) {
      ISequence<IAnyAtomicItem> keys = FnData.fnData(getKeyExpression().accept(dynamicContext, focus));

      return keys.stream()
          .map(key -> {
            if (key instanceof IIntegerItem) {
              int index = ((IIntegerItem) key).asInteger().intValueExact() - 1;
              try {
                return item.get(index);
              } catch (IndexOutOfBoundsException ex) {
                throw new ArrayException(
                    ArrayException.INDEX_OUT_OF_BOUNDS,
                    String.format("The index %d is outside the range of values for the array size '%d'.",
                        index + 1,
                        item.size()),
                    ex);
              }
            }
            throw new InvalidTypeMetapathException(item,
                String.format("The key '%s' of type '%s' is not appropriate for an array lookup.",
                    key.asString(),
                    key.getClass().getName()));

          });
    }
  }

}
