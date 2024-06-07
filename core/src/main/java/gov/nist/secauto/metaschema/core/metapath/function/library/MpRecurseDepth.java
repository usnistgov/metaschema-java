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

package gov.nist.secauto.metaschema.core.metapath.function.library;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathConstants;
import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.StaticMetapathException;
import gov.nist.secauto.metaschema.core.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.core.metapath.function.IArgument;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.List;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides functions that evaluate a Metapath recursively over sequences.
 */
public final class MpRecurseDepth {
  // private static final Logger logger = LogManager.getLogger(FnDoc.class);

  @NonNull
  static final IFunction SIGNATURE_ONE_ARG = IFunction.builder()
      .name("recurse-depth")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS_EXTENDED)
      .deterministic()
      .contextDependent()
      .focusDependent()
      .argument(IArgument.builder()
          .name("recursePath")
          .type(IStringItem.class)
          .one()
          .build())
      .returnType(INodeItem.class)
      .returnZeroOrMore()
      .functionHandler(MpRecurseDepth::executeOneArg)
      .build();

  @NonNull
  static final IFunction SIGNATURE_TWO_ARG = IFunction.builder()
      .name("recurse-depth")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS_EXTENDED)
      .deterministic()
      .contextDependent()
      .focusIndependent()
      .argument(IArgument.builder()
          .name("context")
          .type(INodeItem.class)
          .zeroOrMore()
          .build())
      .argument(IArgument.builder()
          .name("recursePath")
          .type(IStringItem.class)
          .one()
          .build())
      .returnType(INodeItem.class)
      .returnZeroOrMore()
      .functionHandler(MpRecurseDepth::executeTwoArg)
      .build();

  private MpRecurseDepth() {
    // disable construction
  }

  @SuppressWarnings("unused")
  @NonNull
  private static ISequence<INodeItem> executeOneArg(
      @NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {

    ISequence<INodeItem> initalContext = ISequence.of(FunctionUtils.requireType(INodeItem.class, focus));

    ISequence<? extends IStringItem> arg = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(0)));
    IStringItem recursionPath = ObjectUtils.requireNonNull(arg.getFirstItem(true));

    return recurseDepth(initalContext, recursionPath, dynamicContext);
  }

  @SuppressWarnings("unused")
  @NonNull
  private static ISequence<INodeItem> executeTwoArg(
      @NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {

    ISequence<INodeItem> initalContext = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(0)));

    ISequence<? extends IStringItem> arg = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(1)));
    IStringItem recursionPath = ObjectUtils.requireNonNull(arg.getFirstItem(true));

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
      throw new StaticMetapathException(StaticMetapathException.INVALID_PATH_GRAMMAR, ex.getMessage(), ex);
    }

    return recurseDepth(initialContext, recursionMetapath, dynamicContext);
  }

  /**
   * Applies the {@code recursionMetapath} starting with the the items in the
   * {@code initialContext} and also recursively to the resulting items returned
   * by evaluating this path.
   *
   * @param initialContext
   *          the sequence containing the initial node items to evaluate the
   *          Metapath against
   * @param recursionMetapath
   *          the Metapath expression to use for recursive evaluation
   * @param dynamicContext
   *          the dynamic context supporting evaluation
   * @return the Metapath node items resulting from the Metapath execution
   */
  @NonNull
  public static ISequence<INodeItem> recurseDepth(
      @NonNull ISequence<INodeItem> initialContext,
      @NonNull MetapathExpression recursionMetapath,
      @NonNull DynamicContext dynamicContext) {

    return ISequence.of(ObjectUtils.notNull(initialContext.stream()
        .flatMap(item -> {
          @NonNull ISequence<INodeItem> metapathResult
              = recursionMetapath.evaluate(item, dynamicContext);
          ISequence<INodeItem> result = recurseDepth(metapathResult, recursionMetapath, dynamicContext);
          return ObjectUtils.notNull(Stream.concat(result.stream(), Stream.of(item)));
        })));
  }
}
