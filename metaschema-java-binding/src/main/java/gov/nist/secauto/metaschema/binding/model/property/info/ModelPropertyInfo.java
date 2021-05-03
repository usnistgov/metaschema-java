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
import gov.nist.secauto.metaschema.binding.io.json.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.property.ModelProperty;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public interface ModelPropertyInfo {
  /**
   * Get the associated property for which this info is for.
   * 
   * @return the property
   */
  ModelProperty getProperty();

  /**
   * Get the type of the bound object.
   * 
   * @return the raw type of the bound object
   */
  Class<?> getItemType();

  PropertyCollector newPropertyCollector();

  // TODO is the following needed?
  /**
   * Read the value data for the property. At the point that this is called, the parser must be
   * located just after the property/field name has been parsed. This method will return a value based
   * on the property's value type as reported by {@link #getProperty()}.
   * 
   * @param collector
   * @param parentInstance
   * @param context
   *          the JSON parsing context
   * @return the property's value based on the type
   * @throws IOException
   *           if there was an error when reading JSON data
   * @throws BindingException
   *           if there was an error related to this properties Java class binding
   */
  boolean readValue(PropertyCollector collector, Object parentInstance, JsonParsingContext context)
      throws IOException, BindingException;

  boolean readValue(PropertyCollector collector, Object parentInstance, StartElement start, XmlParsingContext context)
      throws IOException, BindingException, XMLStreamException;

  boolean writeValue(Object parentInstance, QName parentName, XmlWritingContext context)
      throws XMLStreamException, IOException;

  void writeValue(Object parentInstance, JsonWritingContext context) throws IOException;

  boolean isValueSet(Object parentInstance) throws IOException;
}
