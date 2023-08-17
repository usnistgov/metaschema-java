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

package gov.nist.secauto.metaschema.core.metapath;

import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

abstract class AbstractBasicArithmeticExpression
    extends AbstractArithmeticExpression<IAnyAtomicItem> {

  /**
   * An expression that represents a basic arithmetic operation on two values.
   *
   * @param left
   *          the first item
   * @param right
   *          the second item
   */
  public AbstractBasicArithmeticExpression(@NonNull IExpression left, @NonNull IExpression right) {
    super(left, right, IAnyAtomicItem.class);
  }

  @Override
  public Class<IAnyAtomicItem> getBaseResultType() {
    return IAnyAtomicItem.class;
  }

  @Override
  public ISequence<? extends IAnyAtomicItem> accept(DynamicContext dynamicContext, ISequence<?> focus) {
    IAnyAtomicItem leftItem = getFirstDataItem(getLeft().accept(dynamicContext, focus), true);
    IAnyAtomicItem rightItem = getFirstDataItem(getRight().accept(dynamicContext, focus), true);

    return resultOrEmpty(leftItem, rightItem);
  }

  /**
   * Setup the operation on two atomic items.
   *
   * @param leftItem
   *          the first item
   * @param rightItem
   *          the second item
   * @return the result of the operation or an empty {@link ISequence} if either
   *         item is {@code null}
   */
  @NonNull
  protected ISequence<? extends IAnyAtomicItem> resultOrEmpty(
      @Nullable IAnyAtomicItem leftItem,
      @Nullable IAnyAtomicItem rightItem) {
    ISequence<? extends IAnyAtomicItem> retval;
    if (leftItem == null || rightItem == null) {
      retval = ISequence.empty();
    } else {
      IAnyAtomicItem result = operation(leftItem, rightItem);
      retval = ISequence.of(result);
    }
    return retval;
  }

  /**
   * Performs the arithmetic operation using the two provided values.
   *
   * @param left
   *          the first item
   * @param right
   *          the second item
   * @return the result of the operation
   */
  @NonNull
  protected abstract IAnyAtomicItem operation(@NonNull IAnyAtomicItem left, @NonNull IAnyAtomicItem right);
}
