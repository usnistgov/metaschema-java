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

package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.io.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.annotations.XmlGroupAsBehavior;
import gov.nist.secauto.metaschema.binding.model.property.info.DataTypeHandler;
import gov.nist.secauto.metaschema.binding.model.property.info.PropertyCollector;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public interface ModelProperty extends NamedProperty {
  DataTypeHandler getBindingSupplier();

  int getMinimumOccurance();

  int getMaximumOccurance();

  QName getXmlGroupQName();

  JsonGroupAsBehavior getJsonGroupAsBehavior();

  XmlGroupAsBehavior getXmlGroupAsBehavior();

  /**
   * Reads an individual XML item from the XML stream.
   * 
   * @param collector
   * @param parentInstance
   * @param start
   *          the current containing XML element
   * @param context
   *          the XML parsing context
   * @return the item read, or {@code null} if no item was read
   * @throws BindingException
   *           if a Java class binding error occurred
   * @throws XMLStreamException
   *           if an error occurred while generating an {@link XMLEvent}
   * @throws IOException
   *           if an error occurred reading the underlying XML file
   */
  boolean readItem(PropertyCollector collector, Object parentInstance, StartElement start, XmlParsingContext context)
      throws BindingException, XMLStreamException, IOException;

  /**
   * Reads a set of JSON items from the JSON stream.
   * 
   * @param collector
   * @param parentInstance
   * @param context
   *          the JSON/YAML parsing context
   * @return the item read, or {@code null} if no item was read
   * @throws BindingException
   *           if a Java class binding error occurred
   * @throws IOException
   *           if an error occurred reading the underlying XML file
   */
  boolean readItem(PropertyCollector collector, Object parentInstance, JsonParsingContext context)
      throws BindingException, IOException;

  boolean writeItem(Object item, QName parentName, XmlWritingContext context) throws XMLStreamException, IOException;

  // void writeItems(List<? extends WritableItem> items, JsonWritingContext context);

  // Collection<? extends WritableItem> getItemsToWrite(Collection<? extends Object> items);

  // void writeItem(Object instance, JsonWritingContext context);
}
