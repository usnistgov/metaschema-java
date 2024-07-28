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
import gov.nist.secauto.metaschema.core.metapath.StaticMetapathException;
import gov.nist.secauto.metaschema.core.metapath.function.library.ArrayGet;
import gov.nist.secauto.metaschema.core.metapath.function.library.FnData;
import gov.nist.secauto.metaschema.core.metapath.function.library.MapGet;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyAtomicItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IIntegerItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.IArrayItem;
import gov.nist.secauto.metaschema.core.metapath.item.function.IMapItem;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public class FunctionCallAccessor implements IExpression {
  @NonNull
  private final IExpression base;
  @NonNull
  private final IExpression argument;

  /**
   * Construct a new functional call accessor.
   *
   * @param base
   *          the expression whose result is used as the map or array to perform
   *          the lookup on
   * @param keyOrIndex
   *          the value to find, which will be the key for a map or the index for
   *          an array
   */
  public FunctionCallAccessor(@NonNull IExpression base, @NonNull IExpression keyOrIndex) {
    this.base = base;
    this.argument = keyOrIndex;
  }

  /**
   * Get the base sub-expression.
   *
   * @return the sub-expression
   */
  @NonNull
  public IExpression getBase() {
    return base;
  }

  /**
   * Retrieve the argument to use for the lookup.
   *
   * @return the argument
   */
  @NonNull
  public IExpression getArgument() {
    return argument;
  }

  @SuppressWarnings("null")
  @Override
  public List<? extends IExpression> getChildren() {
    return List.of(getBase(), getArgument());
  }

  @Override
  public ISequence<? extends IItem> accept(DynamicContext dynamicContext, ISequence<?> focus) {
    ISequence<?> target = getBase().accept(dynamicContext, focus);
    IItem collection = target.getFirstItem(true);
    IAnyAtomicItem key = FnData.fnData(getArgument().accept(dynamicContext, focus)).getFirstItem(false);
    if (key == null) {
      throw new StaticMetapathException(StaticMetapathException.NO_FUNCTION_MATCH,
          "No key provided for functional call lookup");
    }

    ICollectionValue retval = null;
    if (collection instanceof IArrayItem) {
      retval = ArrayGet.get((IArrayItem<?>) collection, IIntegerItem.cast(key));
    } else if (collection instanceof IMapItem) {
      retval = MapGet.get((IMapItem<?>) collection, key);
    }

    return retval == null ? ISequence.empty() : retval.asSequence();
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(@NonNull IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitFunctionCallAccessor(this, context);
  }
}
