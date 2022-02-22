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
import gov.nist.secauto.metaschema.binding.io.json.IJsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.IJsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.IXmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.IBoundNamedModelInstance;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public interface IModelPropertyInfo {
  /**
   * Get the associated property for which this info is for.
   * 
   * @return the property
   */
  IBoundNamedModelInstance getProperty();

  /**
   * Get the type of the bound object.
   * 
   * @return the raw type of the bound object
   */
  Class<?> getItemType();

  IPropertyCollector newPropertyCollector();

  // TODO is the following needed?
  /**
   * Read the value data for the property. At the point that this is called, the parser must be
   * located just after the property/field name has been parsed. This method will return a value based
   * on the property's value type as reported by {@link #getProperty()}.
   * 
   * @param collector
   *          used to hold parsed values
   * @param context
   *          the JSON parsing context
   * @param parentInstance
   *          the instance the property is on
   * @throws IOException
   *           if there was an error when reading JSON data
   */
  void readValue(IPropertyCollector collector, Object parentInstance, IJsonParsingContext context)
      throws IOException;

  boolean readValue(IPropertyCollector collector, Object parentInstance, StartElement start, IXmlParsingContext context)
      throws IOException, XMLStreamException;

  boolean writeValue(Object parentInstance, QName parentName, IXmlWritingContext context)
      throws XMLStreamException, IOException;

  void writeValue(Object parentInstance, IJsonWritingContext context) throws IOException;

  boolean isValueSet(Object parentInstance) throws IOException;

  default Collection<? extends Object> getItemsFromParentInstance(Object parentInstance) {
    Object value = getProperty().getValue(parentInstance);
    return getItemsFromValue(value);
  }

  Collection<? extends Object> getItemsFromValue(Object value);

  void copy(@NotNull Object fromInstance, @NotNull Object toInstance, @NotNull IPropertyCollector collector)
      throws BindingException;
}
