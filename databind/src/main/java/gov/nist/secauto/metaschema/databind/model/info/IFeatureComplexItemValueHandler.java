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

package gov.nist.secauto.metaschema.databind.model.info;

import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.databind.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IFeatureComplexItemValueHandler extends IItemValueHandler {
  @NonNull
  IClassBinding getClassBinding();

  @Override
  default boolean isUnwrappedValueAllowedInXml() {
    // never for a complex item
    return false;
  }

  @Override
  default Object readItem(Object parent, IItemReadHandler handler) throws IOException {
    return handler.readComplexItem(parent, this);
  }

  @Override
  default void writeItem(Object item, IItemWriteHandler handler) throws IOException {
    handler.writeComplexItem(item, this);
  }

  @Override
  default Object deepCopyItem(Object item, Object parentInstance) throws BindingException {
    return getClassBinding().deepCopyItem(item, parentInstance);
  }

  @Nullable
  IBoundFlagInstance getJsonKey();

  @Override
  default Object readItem(Object parent, StartElement start, IXmlParsingContext context)
      throws IOException, XMLStreamException {
    return context.readDefinitionValue(getClassBinding(), parent, start);
  }

  @Override
  default void writeItem(Object item, QName currentParentName, IXmlWritingContext context)
      throws IOException, XMLStreamException {
    context.writeDefinitionValue(getClassBinding(), item, currentParentName);
  }
}
