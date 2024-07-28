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
import gov.nist.secauto.metaschema.core.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.core.metapath.function.IArgument;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;
import gov.nist.secauto.metaschema.core.metapath.function.InvalidArgumentFunctionException;
import gov.nist.secauto.metaschema.core.metapath.function.UriFunctionException;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IAnyUriItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class FnResolveUri {
  @NonNull
  static final IFunction SIGNATURE_ONE_ARG = IFunction.builder()
      .name("resolve-uri")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS)
      .deterministic()
      .contextDependent()
      .focusIndependent()
      .argument(IArgument.builder()
          .name("relative")
          .type(IStringItem.class)
          .zeroOrOne()
          .build())
      .returnType(IAnyUriItem.class)
      .returnZeroOrOne()
      .functionHandler(FnResolveUri::executeOneArg)
      .build();

  @NonNull
  static final IFunction SIGNATURE_TWO_ARG = IFunction.builder()
      .name("resolve-uri")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS)
      .deterministic()
      .contextIndependent()
      .focusIndependent()
      .argument(IArgument.builder()
          .name("relative")
          .type(IStringItem.class)
          .zeroOrOne()
          .build())
      .argument(IArgument.builder()
          .name("base")
          .type(IStringItem.class)
          .one()
          .build())
      .returnType(IAnyUriItem.class)
      .returnZeroOrOne()
      .functionHandler(FnResolveUri::executeTwoArg)
      .build();

  private FnResolveUri() {
    // disable construction
  }

  @SuppressWarnings("unused")
  @NonNull
  private static ISequence<IAnyUriItem> executeOneArg(
      @NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {

    ISequence<? extends IStringItem> relativeSequence
        = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(0)));
    if (relativeSequence.isEmpty()) {
      return ISequence.empty(); // NOPMD - readability
    }

    IStringItem relativeString = relativeSequence.getFirstItem(true);
    IAnyUriItem resolvedUri = null;
    if (relativeString != null) {
      resolvedUri = fnResolveUri(relativeString, null, dynamicContext);
    }
    return ISequence.of(resolvedUri);
  }

  /**
   * Implements the two argument version of the XPath 3.1 function <a href=
   * "https://www.w3.org/TR/xpath-functions-31/#func-resolve-uri">resolve-uri</a>.
   *
   * @param function
   *          the function definition
   * @param arguments
   *          a list of sequence arguments with an expected size of 2
   * @param dynamicContext
   *          the evaluation context
   * @param focus
   *          the current focus item
   * @return a sequence containing the resolved URI or and empty sequence if
   *         either the base or relative URI is {@code null}
   */
  @SuppressWarnings("PMD.UnusedPrivateMethod") // used in lambda
  @NonNull
  private static ISequence<IAnyUriItem> executeTwoArg(
      @NonNull IFunction function, // NOPMD - ok
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext, // NOPMD - ok
      IItem focus) { // NOPMD - ok

    /* there will always be two arguments */
    assert arguments.size() == 2;

    ISequence<? extends IStringItem> relativeSequence = FunctionUtils.asType(
        ObjectUtils.requireNonNull(arguments.get(0)));
    if (relativeSequence.isEmpty()) {
      return ISequence.empty(); // NOPMD - readability
    }

    ISequence<? extends IStringItem> baseSequence = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(1)));
    IStringItem baseString = baseSequence.getFirstItem(true);

    if (baseString == null) {
      throw new InvalidArgumentFunctionException(
          InvalidArgumentFunctionException.INVALID_ARGUMENT_TO_RESOLVE_URI,
          "Invalid argument to fn:resolve-uri().");
    }
    IAnyUriItem baseUri = IAnyUriItem.cast(baseString);

    IStringItem relativeString = relativeSequence.getFirstItem(true);

    IAnyUriItem resolvedUri = null;
    if (relativeString != null) {
      resolvedUri = fnResolveUri(relativeString, baseUri, dynamicContext);
    }
    return ISequence.of(resolvedUri);
  }

  /**
   * Resolve the {@code relative} URI against the provided {@code base} URI.
   *
   * @param relative
   *          the relative URI to resolve
   * @param base
   *          the base URI to resolve against
   * @param dynamicContext
   *          the evaluation context used to get the static base URI if needed
   * @return the resolved URI or {@code null} if the {@code relative} URI in
   *         {@code null}
   */
  @Nullable
  public static IAnyUriItem fnResolveUri(
      @NonNull IStringItem relative,
      @Nullable IAnyUriItem base,
      @NonNull DynamicContext dynamicContext) {
    return fnResolveUri(IAnyUriItem.cast(relative), base, dynamicContext);
  }

  /**
   * Resolve the {@code relative} URI against the provided {@code base} URI.
   *
   * @param relative
   *          the relative URI to resolve
   * @param base
   *          the base URI to resolve against
   * @param dynamicContext
   *          the evaluation context used to get the static base URI if needed
   * @return the resolved URI or {@code null} if the {@code relative} URI in
   *         {@code null}
   */
  @NonNull
  public static IAnyUriItem fnResolveUri(
      @NonNull IAnyUriItem relative,
      @Nullable IAnyUriItem base,
      @NonNull DynamicContext dynamicContext) {

    IAnyUriItem baseUri = base;
    if (baseUri == null) {
      baseUri = FnStaticBaseUri.fnStaticBaseUri(dynamicContext);
      if (baseUri == null) {
        throw new UriFunctionException(UriFunctionException.BASE_URI_NOT_DEFINED_IN_STATIC_CONTEXT,
            "The base-uri is not defined in the static context");
      }
    }

    return baseUri.resolve(relative);
  }
}
