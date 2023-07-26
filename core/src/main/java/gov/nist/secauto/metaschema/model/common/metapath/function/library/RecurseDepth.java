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
import gov.nist.secauto.metaschema.model.common.metapath.DynamicMetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.IArgument;
import gov.nist.secauto.metaschema.model.common.metapath.function.IFunction;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.List;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class RecurseDepth {
  // private static final Logger logger = LogManager.getLogger(FnDoc.class);

  @NonNull
  static final IFunction SIGNATURE_ONE_ARG = IFunction.builder()
      .name("recurse-depth")
      .deterministic()
      .contextDependent()
      .focusDependent()
      .argument(IArgument.newBuilder()
          .name("recursePath")
          .type(IStringItem.class)
          .one()
          .build())
      .returnType(INodeItem.class)
      .returnZeroOrMore()
      .functionHandler(RecurseDepth::executeOneArg)
      .build();

  @NonNull
  static final IFunction SIGNATURE_TWO_ARG = IFunction.builder()
      .name("recurse-depth")
      .deterministic()
      .contextDependent()
      .focusIndependent()
      .argument(IArgument.newBuilder()
          .name("context")
          .type(INodeItem.class)
          .zeroOrMore()
          .build())
      .argument(IArgument.newBuilder()
          .name("recursePath")
          .type(IStringItem.class)
          .one()
          .build())
      .returnType(INodeItem.class)
      .returnZeroOrMore()
      .functionHandler(RecurseDepth::executeTwoArg)
      .build();

  private RecurseDepth() {
    // disable construction
  }

  @SuppressWarnings("unused")
  @NonNull
  private static ISequence<INodeItem> executeOneArg(
      @NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      INodeItem focus) {

    ISequence<INodeItem> initalContext = ISequence.of(ObjectUtils.requireNonNull(focus));

    ISequence<? extends IStringItem> arg = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(0)));
    IStringItem recursionPath = FunctionUtils.requireFirstItem(arg, true);

    return recurseDepth(initalContext, recursionPath, dynamicContext);
  }

  @SuppressWarnings("unused")
  @NonNull
  private static ISequence<INodeItem> executeTwoArg(
      @NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      INodeItem focus) {

    ISequence<INodeItem> initalContext = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(0)));

    ISequence<? extends IStringItem> arg = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(1)));
    IStringItem recursionPath = FunctionUtils.requireFirstItem(arg, true);

    return recurseDepth(initalContext, recursionPath, dynamicContext);
  }

  @NonNull
  private static ISequence<INodeItem> recurseDepth(
      @NonNull ISequence<INodeItem> initialContext,
      @NonNull IStringItem recursionPath,
      @NonNull DynamicContext dynamicContext) {

    MetapathExpression recursionMetapath;
    try {
      recursionMetapath = MetapathExpression.compile(recursionPath.asString());
    } catch (MetapathException ex) {
      throw new DynamicMetapathException(DynamicMetapathException.INVALID_PATH_GRAMMAR, ex.getMessage(), ex);
    }

    return recurseDepth(initialContext, recursionMetapath, dynamicContext);
  }

  @NonNull
  public static ISequence<INodeItem> recurseDepth(
      @NonNull ISequence<INodeItem> initialContext,
      @NonNull MetapathExpression recursionMetapath,
      @NonNull DynamicContext dynamicContext) {

    return ISequence.of(ObjectUtils.notNull(initialContext.asStream()
        .flatMap(item -> {
          @NonNull ISequence<INodeItem> metapathResult
              = recursionMetapath.evaluate(ObjectUtils.requireNonNull(item), dynamicContext);
          ISequence<INodeItem> result = recurseDepth(metapathResult, recursionMetapath, dynamicContext);
          return Stream.concat(result.asStream(), Stream.of(item));
        })));
  }
}
