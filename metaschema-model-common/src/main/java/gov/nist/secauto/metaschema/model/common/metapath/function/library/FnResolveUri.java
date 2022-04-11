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
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.IArgument;
import gov.nist.secauto.metaschema.model.common.metapath.function.IFunction;
import gov.nist.secauto.metaschema.model.common.metapath.function.InvalidArgumentFunctionMetapathException;
import gov.nist.secauto.metaschema.model.common.metapath.function.UriFunctionException;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAnyUriItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.List;

public class FnResolveUri {
  @NotNull
  static final IFunction SIGNATURE_ONE_ARG = IFunction.builder()
      .name("resolve-uri")
      .deterministic()
      .contextDependent()
      .focusIndependent()
      .argument(IArgument.newBuilder()
          .name("relative")
          .type(IStringItem.class)
          .zeroOrOne()
          .build())
      .returnType(IAnyUriItem.class)
      .returnZeroOrOne()
      .functionHandler(FnResolveUri::executeOneArg)
      .build();

  @NotNull
  static final IFunction SIGNATURE_TWO_ARG = IFunction.builder()
      .name("resolve-uri")
      .deterministic()
      .contextIndependent()
      .focusIndependent()
      .argument(IArgument.newBuilder()
          .name("relative")
          .type(IStringItem.class)
          .zeroOrOne()
          .build())
      .argument(IArgument.newBuilder()
          .name("base")
          .type(IStringItem.class)
          .one()
          .build())
      .returnType(IAnyUriItem.class)
      .returnZeroOrOne()
      .functionHandler(FnResolveUri::executeTwoArg)
      .build();

  @SuppressWarnings("unused")
  @NotNull
  private static ISequence<IAnyUriItem> executeOneArg(@NotNull IFunction function,
      @NotNull List<@NotNull ISequence<?>> arguments,
      @NotNull DynamicContext dynamicContext,
      INodeItem focus) {

    ISequence<? extends IStringItem> relativeSequence = FunctionUtils.asType(arguments.get(0));
    if (relativeSequence.isEmpty()) {
      return ISequence.empty();
    }

    IStringItem relativeString = FunctionUtils.getFirstItem(relativeSequence, true);

    IAnyUriItem baseUri = FnStaticBaseUri.fnStaticBaseUri(dynamicContext);
    if (baseUri == null) {
      throw new UriFunctionException(UriFunctionException.BASE_URI_NOT_DEFINED_IN_STATIC_CONTEXT,
          "The base-uri is not defined in the static context");
    }

    IAnyUriItem resolvedUri = fnResolveUri(relativeString, baseUri);
    return resolvedUri == null ? ISequence.empty() : ISequence.of(resolvedUri);
  }

  /**
   * Implements the two argument version of the XPath 3.1 function
   * <a href="https://www.w3.org/TR/xpath-functions-31/#func-resolve-uri">resolve-uri</a>.
   * 
   * @param function
   *          the function definition
   * @param arguments
   *          a list of sequence arguments with an expected size of 2
   * @param dynamicContext
   *          the evaluation context
   * @param focus
   *          the current focus item
   * @return a sequence containing the resolved URI or and empty sequence if either the base or
   *         relative URI is {@code null}
   */
  @NotNull
  private static ISequence<IAnyUriItem> executeTwoArg(@NotNull IFunction function,
      @NotNull List<@NotNull ISequence<?>> arguments,
      @NotNull DynamicContext dynamicContext,
      INodeItem focus) {

    /* there will always be two arguments */
    assert arguments.size() == 2;

    ISequence<? extends IStringItem> relativeSequence = FunctionUtils.asType(arguments.get(0));
    if (relativeSequence.isEmpty()) {
      return ISequence.empty();
    }

    IStringItem relativeString = FunctionUtils.getFirstItem(relativeSequence, true);

    ISequence<? extends IStringItem> baseSequence = FunctionUtils.asType(arguments.get(1));

    IStringItem baseString = FunctionUtils.getFirstItem(baseSequence, true);

    if (baseString == null) {
      throw new InvalidArgumentFunctionMetapathException(
          InvalidArgumentFunctionMetapathException.INVALID_ARGUMENT_TO_RESOLVE_URI,
          "Invalid argument to fn:resolve-uri().");
    }
    IAnyUriItem baseUri = IAnyUriItem.cast(baseString);

    IAnyUriItem resolvedUri = fnResolveUri(relativeString, baseUri);
    return resolvedUri == null ? ISequence.empty() : ISequence.of(resolvedUri);
  }

  /**
   * Resolve the {@code relative} URI against the provided {@code base} URI.
   * 
   * @param relative
   *          the relative URI to resolve
   * @param base
   *          the base URI to resolve against
   * @return the resolved URI or {@code null} if the {@code relative} URI in {@code null}
   */
  @Nullable
  public static IAnyUriItem fnResolveUri(@Nullable IStringItem relative, @NotNull IAnyUriItem base) {
    IAnyUriItem relativeUri = relative == null ? null : IAnyUriItem.cast(relative);

    return fnResolveUri(relativeUri, base);
  }

  /**
   * Resolve the {@code relative} URI against the provided {@code base} URI.
   * 
   * @param relative
   *          the relative URI to resolve
   * @param base
   *          the base URI to resolve against
   * @return the resolved URI or {@code null} if the {@code relative} URI in {@code null}
   */
  @Nullable
  public static IAnyUriItem fnResolveUri(@Nullable IAnyUriItem relative, @NotNull IAnyUriItem base) {
    if (relative == null) {
      return null;
    }

    @SuppressWarnings("null")
    @NotNull
    URI resolvedUri = base.getValue().resolve(relative.getValue());
    return IAnyUriItem.valueOf(resolvedUri);
  }
}
