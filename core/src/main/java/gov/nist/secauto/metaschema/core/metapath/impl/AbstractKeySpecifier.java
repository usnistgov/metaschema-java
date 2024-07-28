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
import gov.nist.secauto.metaschema.core.metapath.InvalidTypeMetapathException;
import gov.nist.secauto.metaschema.core.metapath.cst.IExpression;
import gov.nist.secauto.metaschema.core.metapath.function.library.ArrayGet;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.core.metapath.function.library.MapGet;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.ArrayException;
import gov.nist.secauto.metaschema.core.metapath.item.function.IArrayItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.IKeySpecifier;
import gov.nist.secauto.metaschema.core.metapath.item.function.IMapItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A common base class for all key specifiers.
 */
public abstract class AbstractKeySpecifier implements IKeySpecifier {
  @Override
  public Stream<? extends ICollectionValue> lookup(
      @NonNull IItem targetItem,
      @NonNull DynamicContext dynamicContext,
      @NonNull ISequence<?> focus) {
    Stream<? extends ICollectionValue> result;
    if (targetItem instanceof IArrayItem) {
      result = lookupInArray((IArrayItem<?>) targetItem, dynamicContext, focus);
    } else if (targetItem instanceof IMapItem) {
      result = lookupInMap((IMapItem<?>) targetItem, dynamicContext, focus);
    } else {
      throw new InvalidTypeMetapathException(targetItem,
          String.format("Item type '%s' is not an array or map.", targetItem.getClass().getName()));
    }
    return result;
  }

  /**
   * A dispatch method intended to handle lookups within an array item.
   *
   * @param targetItem
   *          the item to query
   * @param dynamicContext
   *          the dynamic context to use for expression evaluation
   * @param focus
   *          the focus item for expression evaluation
   * @return a stream of collection values matching this key specifier
   */
  @NonNull
  protected abstract Stream<? extends ICollectionValue> lookupInArray(
      @NonNull IArrayItem<?> targetItem,
      @NonNull DynamicContext dynamicContext,
      @NonNull ISequence<?> focus);

  /**
   * A dispatch method intended to handle lookups within a map item.
   *
   * @param targetItem
   *          the item to query
   * @param dynamicContext
   *          the dynamic context to use for expression evaluation
   * @param focus
   *          the focus item for expression evaluation
   * @return a stream of collection values matching this key specifier
   */
  @NonNull
  protected abstract Stream<? extends ICollectionValue> lookupInMap(
      @NonNull IMapItem<?> targetItem,
      @NonNull DynamicContext dynamicContext,
      @NonNull ISequence<?> focus);

  /**
   * Construct a new key specifier supporting name-based lookups.
   *
   * @param name
   *          the name to use for lookups
   * @return the key specifier
   */
  @NonNull
  public static IKeySpecifier newNameKeySpecifier(@NonNull String name) {
    return new AbstractKeySpecifier.NcNameKeySpecifier(name);
  }

  /**
   * Construct a new key specifier supporting integer-based lookups.
   *
   * @param integer
   *          the integer to use for lookups
   * @return the key specifier
   */
  @NonNull
  public static IKeySpecifier newIntegerLiteralKeySpecifier(@NonNull IIntegerItem integer) {
    return new AbstractKeySpecifier.IntegerLiteralKeySpecifier(integer);
  }

  /**
   * Construct a new key specifier supporting wildcard lookups.
   *
   * @return the key specifier
   */
  @NonNull
  public static IKeySpecifier newWildcardKeySpecifier() {
    return new AbstractKeySpecifier.WildcardKeySpecifier();
  }

  /**
   * Construct a new key specifier supporting key-based lookups.
   *
   * @param keyExpression
   *          the expression used to get a key to use for lookups
   * @return the key specifier
   */
  @NonNull
  public static IKeySpecifier newParenthesizedExprKeySpecifier(@NonNull IExpression keyExpression) {
    return new AbstractKeySpecifier.ParenthesizedExprKeySpecifier(keyExpression);
  }

  private static class NcNameKeySpecifier
      extends AbstractKeySpecifier {
    @NonNull
    private final String name;

    public NcNameKeySpecifier(@NonNull String name) {
      this.name = name;
    }

    @NonNull
    public String getName() {
      return name;
    }

    @Override
    protected Stream<? extends IItem> lookupInArray(
        IArrayItem<?> targetItem,
        DynamicContext dynamicContext,
        ISequence<?> focus) {
      throw new InvalidTypeMetapathException(targetItem,
          String.format("A name-based lookup '%s' is not appropriate for an array.", getName()));
    }

    @Override
    protected Stream<? extends ICollectionValue> lookupInMap(
        IMapItem<?> targetItem,
        DynamicContext dynamicContext,
        ISequence<?> focus) {
      return ObjectUtils.notNull(Stream.ofNullable(MapGet.get(targetItem, IStringItem.valueOf(name))));
    }
  }

  private static class IntegerLiteralKeySpecifier
      extends AbstractKeySpecifier {
    private final int index;

    public IntegerLiteralKeySpecifier(@NonNull IIntegerItem literal) {
      index = literal.asInteger().intValueExact();
    }

    @Override
    protected Stream<? extends ICollectionValue> lookupInArray(
        IArrayItem<?> targetItem,
        DynamicContext dynamicContext,
        ISequence<?> focus) {
      try {
        return ObjectUtils.notNull(Stream.ofNullable(ArrayGet.get(targetItem, index)));
      } catch (IndexOutOfBoundsException ex) {
        throw new ArrayException(
            ArrayException.INDEX_OUT_OF_BOUNDS,
            String.format("The index '%d' is outside the range of values for the array size '%d'.",
                index + 1,
                targetItem.size()),
            ex);
      }
    }

    @Override
    protected Stream<? extends ICollectionValue> lookupInMap(
        IMapItem<?> targetItem,
        DynamicContext dynamicContext,
        ISequence<?> focus) {
      return ObjectUtils.notNull(Stream.ofNullable(MapGet.get(targetItem, IIntegerItem.valueOf(index))));
    }
  }

  private static class WildcardKeySpecifier
      extends AbstractKeySpecifier {

    public WildcardKeySpecifier() {
      // do nothing
    }

    @Override
    protected Stream<? extends ICollectionValue> lookupInArray(
        IArrayItem<?> targetItem,
        DynamicContext dynamicContext,
        ISequence<?> focus) {
      return ObjectUtils.notNull(targetItem.stream());
    }

    @Override
    protected Stream<? extends ICollectionValue> lookupInMap(
        IMapItem<?> targetItem,
        DynamicContext dynamicContext,
        ISequence<?> focus) {
      return ObjectUtils.notNull(targetItem.values().stream());
    }
  }

  private static class ParenthesizedExprKeySpecifier
      extends AbstractKeySpecifier {
    @NonNull
    private final IExpression keyExpression;

    public ParenthesizedExprKeySpecifier(@NonNull IExpression keyExpression) {
      this.keyExpression = keyExpression;
    }

    public IExpression getKeyExpression() {
      return keyExpression;
    }

    @Override
    protected Stream<ICollectionValue> lookupInArray(
        IArrayItem<?> targetItem,
        DynamicContext dynamicContext,
        ISequence<?> focus) {
      ISequence<IAnyAtomicItem> keys = FnData.fnData(getKeyExpression().accept(dynamicContext, focus));

      return ObjectUtils.notNull(keys.stream()
          .flatMap(key -> {
            if (key instanceof IIntegerItem) {
              int index = ((IIntegerItem) key).asInteger().intValueExact();
              try {
                return Stream.ofNullable(ArrayGet.get(targetItem, index));
              } catch (IndexOutOfBoundsException ex) {
                throw new ArrayException(
                    ArrayException.INDEX_OUT_OF_BOUNDS,
                    String.format("The index %d is outside the range of values for the array size '%d'.",
                        index + 1,
                        targetItem.size()),
                    ex);
              }
            }
            throw new InvalidTypeMetapathException(targetItem,
                String.format("The key '%s' of type '%s' is not appropriate for an array lookup.",
                    key.asString(),
                    key.getClass().getName()));

          }));
    }

    @Override
    protected Stream<ICollectionValue> lookupInMap(
        IMapItem<?> targetItem,
        DynamicContext dynamicContext,
        ISequence<?> focus) {
      ISequence<? extends IAnyAtomicItem> keys
          = ObjectUtils.requireNonNull(FnData.fnData(getKeyExpression().accept(dynamicContext, focus)));

      return ObjectUtils.notNull(keys.stream()
          .flatMap(key -> {
            assert key != null;
            return Stream.ofNullable(MapGet.get(targetItem, key));
          }));
    }
  }
}
