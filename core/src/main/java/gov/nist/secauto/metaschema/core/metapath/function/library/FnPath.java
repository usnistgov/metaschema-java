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
import gov.nist.secauto.metaschema.core.metapath.InvalidTypeMetapathException;
import gov.nist.secauto.metaschema.core.metapath.MetapathConstants;
import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.core.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.core.metapath.function.IArgument;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Since a node doesn't have a base URI in Metaschema, this is an alias for the
 * document-uri function.
 */
public final class FnPath {

  @NonNull
  static final IFunction SIGNATURE_NO_ARG = IFunction.builder()
      .name("path")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS)
      .deterministic()
      .contextDependent()
      .focusDependent()
      .returnType(IStringItem.class)
      .returnZeroOrOne()
      .functionHandler(FnPath::executeNoArg)
      .build();

  @NonNull
  static final IFunction SIGNATURE_ONE_ARG = IFunction.builder()
      .name("path")
      .namespace(MetapathConstants.NS_METAPATH_FUNCTIONS)
      .deterministic()
      .contextIndependent()
      .focusIndependent()
      .argument(IArgument.builder()
          .name("arg1")
          .type(INodeItem.class)
          .zeroOrOne()
          .build())
      .returnType(IStringItem.class)
      .returnZeroOrOne()
      .functionHandler(FnPath::executeOneArg)
      .build();

  private FnPath() {
    // disable construction
  }

  @SuppressWarnings("unused")
  @NonNull
  private static ISequence<IStringItem> executeNoArg(@NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {

    INodeItem item = FunctionUtils.requireTypeOrNull(INodeItem.class, focus);

    ISequence<IStringItem> retval;
    if (item == null) {
      retval = ISequence.empty();
    } else {
      retval = ISequence.of(IStringItem.valueOf(item.toPath(IPathFormatter.METAPATH_PATH_FORMATER)));
    }
    return retval;
  }

  @SuppressWarnings("unused")
  @NonNull
  private static ISequence<IStringItem> executeOneArg(@NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {

    return fnPath(ObjectUtils.requireNonNull(arguments.get(0)));
  }

  /**
   * An implementation of XPath 3.1
   * <a href="https://www.w3.org/TR/xpath-functions-31/#func-path">fn:path</a>.
   *
   * @param sequence
   *          a Metapath sequence containing an item to get the Metapath for
   * @return a sequence containing the generated Metapath
   */
  @NonNull
  public static ISequence<IStringItem> fnPath(@NonNull ISequence<?> sequence) {
    IItem item = sequence.getFirstItem(true);

    ISequence<IStringItem> retval;
    if (item == null) {
      retval = ISequence.empty();
    } else {
      try {
        retval = ISequence.of(fnPath((INodeItem) item));
      } catch (ClassCastException ex) {
        throw new InvalidTypeMetapathException(
            item,
            String.format("Expected a '%s', but received a '%s'",
                INodeItem.class.getName(),
                item.getClass().getName()),
            ex);
      }
    }
    return retval;
  }

  /**
   * Generates a Metapath for the provided item.
   *
   * @param item
   *          the item to get the Metapath for
   * @return the generated Metapath
   */
  @Nullable
  public static IStringItem fnPath(@Nullable INodeItem item) {
    return item == null ? null : IStringItem.valueOf(item.toPath(IPathFormatter.METAPATH_PATH_FORMATER));
  }
}
