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
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIntegerItem;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The CST node for a Metapath
 * <a href="https://www.w3.org/TR/xpath-31/#doc-xpath31-RangeExpr">range
 * expression</a>.
 */
public class Range
    extends AbstractBinaryExpression<IExpression, IExpression> {

  /**
   * Construct a new range expression.
   *
   * @param start
   *          the expressions representing the start of the range
   * @param end
   *          the expressions representing the end of the range
   *
   */
  public Range(@NonNull IExpression start, @NonNull IExpression end) {
    super(start, end);
  }

  @Override
  public Class<IIntegerItem> getBaseResultType() {
    return IIntegerItem.class;
  }

  @Override
  public ISequence<IIntegerItem> accept(DynamicContext dynamicContext, ISequence<?> focus) {
    IAnyAtomicItem leftItem = getFirstDataItem(getLeft().accept(dynamicContext, focus), true);
    IAnyAtomicItem rightItem = getFirstDataItem(getRight().accept(dynamicContext, focus), true);

    IIntegerItem left = leftItem == null ? null : IIntegerItem.cast(leftItem);
    IIntegerItem right = rightItem == null ? null : IIntegerItem.cast(rightItem);

    ISequence<IIntegerItem> retval;
    if (left == null || right == null || left.compareTo(right) > 0) {
      retval = ISequence.empty();
    } else {

      BigInteger min = right.asInteger();
      BigInteger max = right.asInteger();

      List<IIntegerItem> range = new ArrayList<>(max.subtract(min).add(BigInteger.ONE).intValueExact());
      for (BigInteger val = left.asInteger(); val.compareTo(max) <= 0; val = val.add(BigInteger.ONE)) {
        range.add(IIntegerItem.valueOf(val));
      }

      retval = ISequence.ofCollection(range);
    }
    return retval;
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitRange(this, context);
  }
}
