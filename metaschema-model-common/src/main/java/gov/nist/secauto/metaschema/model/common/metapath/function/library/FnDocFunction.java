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
import gov.nist.secauto.metaschema.model.common.metapath.function.DocumentFunctionException;
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.IArgument;
import gov.nist.secauto.metaschema.model.common.metapath.function.IFunction;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IStringItem;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class FnDocFunction {
  // private static final Logger logger = LogManager.getLogger(FnDocFunction.class);

  static final IFunction SIGNATURE = IFunction.newBuilder()
      .name("doc")
      .deterministic()
      .contextDependent()
      .focusIndependent()
      .argument(IArgument.newBuilder()
          .name("arg1")
          .type(IStringItem.class)
          .zeroOrOne()
          .build())
      .returnType(INodeItem.class)
      .returnOne()
      .functionHandler(FnDocFunction::execute)
      .build();

  @NotNull
  public static ISequence<?> execute(@NotNull IFunction function,
      @NotNull List<@NotNull ISequence<?>> arguments, @NotNull DynamicContext dynamicContext,
      INodeItem focus) {
    ISequence<? extends IStringItem> arg = FunctionUtils.asType(arguments.get(0));

    IItem item = FunctionUtils.getFirstItem(arg, true);
    if (item == null) {
      return ISequence.empty();
    }

    return ISequence.of(fnDoc(FunctionUtils.asType(item), dynamicContext));
  }

  public static INodeItem fnDoc(IStringItem uri, DynamicContext context) {
    if (uri == null) {
      return null;
    }

    URI documentUri;
    try {
      documentUri = URI.create(uri.asString());
    } catch (IllegalArgumentException ex) {
      throw new DocumentFunctionException(DocumentFunctionException.INVALID_ARGUMENT,
          String.format("Invalid URI argument '%s' to fn:doc or fn:doc-available.", uri.asString()), ex);
    }

    URI baseUri = context.getStaticContext().getBaseUri();
    if (baseUri != null) {
      // resolve if possible
      documentUri = baseUri.resolve(documentUri);
    } else {
      if (!documentUri.isAbsolute() && !documentUri.isOpaque()) {
        throw new DocumentFunctionException(DocumentFunctionException.ERROR_RETRIEVING_RESOURCE, String
            .format("No base-uri is available in the static context to resolve the URI '%s'.", documentUri.toString()));
      }
    }

    INodeItem documentNodeItem;
    try {
      @SuppressWarnings("null")
      INodeItem result = context.getDocumentLoader().loadAsNodeItem(documentUri.toURL());
      documentNodeItem = result;
    } catch (IOException e) {
      throw new DocumentFunctionException(DocumentFunctionException.ERROR_RETRIEVING_RESOURCE, String
          .format("Unable to retrieve the resource identified by the URI '%s'.", documentUri.toString()));
    }
    return documentNodeItem;
  }
}
