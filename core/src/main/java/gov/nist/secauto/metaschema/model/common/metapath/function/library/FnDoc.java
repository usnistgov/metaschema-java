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
import gov.nist.secauto.metaschema.model.common.metapath.function.DocumentFunctionException;
import gov.nist.secauto.metaschema.model.common.metapath.function.FunctionUtils;
import gov.nist.secauto.metaschema.model.common.metapath.function.IArgument;
import gov.nist.secauto.metaschema.model.common.metapath.function.IFunction;
import gov.nist.secauto.metaschema.model.common.metapath.item.IItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.atomic.IStringItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class FnDoc {
  // private static final Logger logger = LogManager.getLogger(FnDoc.class);

  @NonNull
  static final IFunction SIGNATURE = IFunction.builder()
      .name("doc")
      .deterministic()
      .contextDependent()
      .focusIndependent()
      .argument(IArgument.newBuilder()
          .name("arg1")
          .type(IStringItem.class)
          .zeroOrOne()
          .build())
      .returnType(IDocumentNodeItem.class)
      .returnOne()
      .functionHandler(FnDoc::execute)
      .build();

  private FnDoc() {
    // disable construction
  }

  @SuppressWarnings("unused")
  @NonNull
  private static ISequence<IDocumentNodeItem> execute(@NonNull IFunction function,
      @NonNull List<ISequence<?>> arguments, @NonNull DynamicContext dynamicContext,
      IItem focus) {
    ISequence<? extends IStringItem> arg = FunctionUtils.asType(ObjectUtils.requireNonNull(arguments.get(0)));

    IStringItem item = FunctionUtils.getFirstItem(arg, true);

    return item == null ? ISequence.empty() : ISequence.of(fnDoc(item, dynamicContext));
  }

  /**
   * Dynamically load the document associated with the URI, and return a {@link IDocumentNodeItem}
   * containing the result.
   * <p>
   * Based on the XPath 3.1 <a href="https://www.w3.org/TR/xpath-functions-31/#func-doc">fn:doc</a>
   * function.
   *
   * @param uri
   *          the resource to load the data from
   * @param context
   *          the Metapath dynamic context
   * @return the loaded document node item
   */
  public static IDocumentNodeItem fnDoc(@NonNull IStringItem uri, @NonNull DynamicContext context) {
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

    try {
      return context.getDocumentLoader().loadAsNodeItem(ObjectUtils.notNull(documentUri.toURL()));
    } catch (IOException | URISyntaxException ex) {
      throw new DocumentFunctionException(DocumentFunctionException.ERROR_RETRIEVING_RESOURCE, String
          .format("Unable to retrieve the resource identified by the URI '%s'.", documentUri.toString()), ex);
    }
  }
}
