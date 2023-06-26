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

package gov.nist.secauto.metaschema.model.common.metapath.function.library;

import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.IArgument;
import gov.nist.secauto.metaschema.model.common.metapath.function.IFunction;
import gov.nist.secauto.metaschema.model.common.metapath.function.IFunctionExecutor;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyAtomicItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class CastFunction<ITEM extends IAnyAtomicItem> implements IFunctionExecutor {
  @NonNull
  private final ICastExecutor<ITEM> castExecutor;

  @NonNull
  static <ITEM extends IAnyAtomicItem> IFunction signature(@NonNull String name,
      @NonNull Class<ITEM> resulingAtomicType, @NonNull ICastExecutor<ITEM> executor) {
    return IFunction.builder()
        .name(name)
        .argument(IArgument.newBuilder()
            .name("arg1")
            .type(IAnyAtomicItem.class)
            .one()
            .build())
        .returnType(resulingAtomicType)
        .returnZeroOrOne()
        .functionHandler(newCastExecutor(executor))
        .build();
  }

  @NonNull
  private static <ITEM extends IAnyAtomicItem> CastFunction<ITEM>
      newCastExecutor(@NonNull ICastExecutor<ITEM> executor) {
    return new CastFunction<>(executor);
  }

  private CastFunction(@NonNull ICastExecutor<ITEM> castExecutor) {
    this.castExecutor = castExecutor;
  }

  @Override
  public ISequence<ITEM> execute(@NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      INodeItem focus) {

    ISequence<? extends IAnyAtomicItem> arg = FunctionUtils.asType(
        ObjectUtils.notNull(arguments.get(0)));

    IAnyAtomicItem item = FunctionUtils.getFirstItem(arg, true);
    if (item == null) {
      return ISequence.empty(); // NOPMD - readability
    }

    ITEM castItem = castExecutor.cast(item);
    return ISequence.of(castItem);
  }

  @FunctionalInterface
  public interface ICastExecutor<ITEM extends IAnyAtomicItem> {
    /**
     * Cast the provided {@code item}.
     *
     * @param item
     *          the item to cast
     * @return the item cast to the appropriate type
     */
    @NonNull
    ITEM cast(@NonNull IAnyAtomicItem item);
  }
}
