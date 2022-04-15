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

package gov.nist.secauto.metaschema.binding.io;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultConstraintValidator;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.StaticContext;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

public abstract class AbstractDeserializer<CLASS>
    extends AbstractSerializationBase
    implements IDeserializer<CLASS> {

  /**
   * Construct a new deserializer.
   * 
   * @param bindingContext
   *          the binding context used to supply bound Java classes while writing
   * @param classBinding
   *          the bound class information for the Java type this deserializer is operating on
   */
  protected AbstractDeserializer(@NotNull IBindingContext bindingContext, @NotNull IAssemblyClassBinding classBinding) {
    super(bindingContext, classBinding);
  }

  @Override
  public INodeItem deserializeToNodeItem(Reader reader, URI documentUri) throws IOException {

    INodeItem nodeItem;
    try {
      nodeItem = deserializeToNodeItemInternal(reader, documentUri);
    } catch (Exception ex) { // NOPMD - this is intentional
      throw new IOException(ex);
    }

    if (isValidating()) {
      StaticContext staticContext = new StaticContext();
      DynamicContext dynamicContext = staticContext.newDynamicContext();
      dynamicContext.setDocumentLoader(getBindingContext().newBoundLoader());
      DefaultConstraintValidator validator = new DefaultConstraintValidator(dynamicContext);
      nodeItem.validate(validator);
      validator.finalizeValidation();
    }
    return nodeItem;
  }

  /**
   * This abstract method delegates parsing to the concrete implementation.
   * 
   * @param reader
   *          the reader instance to read data from
   * @param documentUri
   *          the URI of the document that is being read
   * @return a new node item containing the read contents
   * @throws IOException
   *           if an error occurred while reading data from the stream
   */
  @NotNull
  protected abstract INodeItem deserializeToNodeItemInternal(@NotNull Reader reader, @NotNull URI documentUri)
      throws IOException;
}
