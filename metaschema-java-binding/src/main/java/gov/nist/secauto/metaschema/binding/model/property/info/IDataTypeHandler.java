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

package gov.nist.secauto.metaschema.binding.model.property.info;

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.IBoundNamedModelInstance;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

// TODO: get rid of functional interfaces
public interface IDataTypeHandler extends IJsonBindingSupplier, IXmlBindingSupplier, IXmlBindingConsumer {
  IBoundNamedModelInstance getProperty();

  /**
   * Get the class binding associated with this handler.
   * 
   * @return the class binding or {@code null} if the property's item type is not a bound class
   */
  IClassBinding getClassBinding();

  IJavaTypeAdapter<?> getJavaTypeAdapter();

  // a proxy to the JavaTypeAdapter if it is used or {@code false}
  boolean isUnrappedValueAllowedInXml();

  // void writeProxyWritableItem(ProxyWritableItem item, IJsonWritingContext context) throws
  // IOException;
  //
  // void writeCollapsedWritableItem(CollapsedWritableItem proxy, IJsonWritingContext context);

  void writeItems(Collection<@NotNull ? extends Object> items, boolean writeObjectWrapper, IJsonWritingContext context)
      throws IOException;

  Object copyItem(@NotNull Object fromItem, Object parentInstance) throws BindingException;
}
