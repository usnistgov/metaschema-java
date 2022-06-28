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

import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.TypeMetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractExpression implements IExpression {
  /**
   * Get the first data item of the provided {@code sequence} cast to an {@link IAnyAtomicItem}.
   * 
   * @param sequence
   *          the sequence to get the data item from
   * @param requireSingleton
   *          if {@code true} then a {@link TypeMetapathException} is thrown if the sequence contains
   *          more than one item
   * @return {@code null} if the sequence is empty, or the item otherwise
   * @throws TypeMetapathException
   *           if the sequence contains more than one item and requireSingleton is {@code true}, or if
   *           the data item cannot be cast
   */
  @Nullable
  public static IAnyAtomicItem getFirstDataItem(@NotNull ISequence<?> sequence,
      boolean requireSingleton) {
    IItem item = FunctionUtils.getFirstItem(sequence, requireSingleton);

    return item == null ? null : FnData.fnDataItem(item);
  }

  @FunctionalInterface
  public interface ExpressionEvaluator {
    /**
     * Used to evaluate the provided {@code expression}.
     * 
     * @param expression
     *          the expression to evaluate
     * @return the evaluation result
     */
    @NotNull
    ISequence<?> evaluate(@NotNull IExpression expression);
  }

  @Override
  public String toString() {
    return ASTPrinter.instance().visit(this);
  }
}
