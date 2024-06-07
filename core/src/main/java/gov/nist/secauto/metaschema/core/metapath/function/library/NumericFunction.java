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
import gov.nist.secauto.metaschema.core.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.core.metapath.function.IArgument;
import gov.nist.secauto.metaschema.core.metapath.function.IFunction;
import gov.nist.secauto.metaschema.core.metapath.function.IFunctionExecutor;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.atomic.INumericItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Provides a generic implementation of methods defined by <a href=
 * "https://www.w3.org/TR/xpath-functions-31/#numeric-value-functions">XPath 3.1
 * Functions on numeric values</a>.
 */
public final class NumericFunction implements IFunctionExecutor {

  @NonNull
  private final INumericExecutor executor;

  @NonNull
  static IFunction signature(@NonNull URI namespace, @NonNull String name, @NonNull INumericExecutor executor) {
    return signature(ObjectUtils.notNull(namespace.toASCIIString()), name, executor);
  }

  @NonNull
  static IFunction signature(@NonNull String namespace, @NonNull String name, @NonNull INumericExecutor executor) {
    return IFunction.builder()
        .name(name)
        .namespace(namespace)
        .deterministic()
        .contextIndependent()
        .focusIndependent()
        .argument(IArgument.builder()
            .name("arg1")
            .type(INumericItem.class)
            .zeroOrOne()
            .build())
        .returnType(INumericItem.class)
        .returnZeroOrOne()
        .functionHandler(newFunctionHandler(executor))
        .build();
  }

  @NonNull
  static IFunction signature(@NonNull QName qname, @NonNull INumericExecutor executor) {
    return signature(
        ObjectUtils.requireNonNull(qname.getNamespaceURI(), "the namespace URI must not be null"),
        ObjectUtils.requireNonNull(qname.getLocalPart(), "the localpart must not be null"),
        executor);
  }

  @NonNull
  static NumericFunction newFunctionHandler(@NonNull INumericExecutor executor) {
    return new NumericFunction(executor);
  }

  private NumericFunction(@NonNull INumericExecutor executor) {
    this.executor = executor;
  }

  @Override
  public ISequence<INumericItem> execute(@NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments,
      @NonNull DynamicContext dynamicContext,
      IItem focus) {

    ISequence<? extends INumericItem> sequence = FunctionUtils.asType(
        ObjectUtils.requireNonNull(arguments.get(0)));
    if (sequence.isEmpty()) {
      return ISequence.empty(); // NOPMD - readability
    }

    INumericItem item = sequence.getFirstItem(true);
    if (item == null) {
      return ISequence.empty(); // NOPMD - readability
    }

    INumericItem result = executor.execute(item);
    return ISequence.of(result);
  }

  @FunctionalInterface
  public interface INumericExecutor {
    /**
     * Perform the execution using the provided {@code item}.
     *
     * @param item
     *          the item to operate on
     * @return the numeric result from the execution
     */
    @NonNull
    INumericItem execute(@NonNull INumericItem item);
  }
}
