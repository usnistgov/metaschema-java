/**
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

package gov.nist.secauto.metaschema.binding.io.property;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.AssemblyClassBinding;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.AssemblyPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.FieldPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

/**
 * Represents the type of item, which will be one of: Flag, FieldValue, Assembly as an Assembly
 * object, Field (with Flags) as a Field object, Field (without Flags) as a scalar value
 */
public interface PropertyItemHandler {

  static PropertyItemHandler newPropertyItemHandler(PropertyBinding propertyBinding, BindingContext bindingContext)
      throws BindingException {
    Class<?> itemClass = propertyBinding.getPropertyInfo().getItemType();
    ClassBinding<?> itemClassBinding = bindingContext.getClassBinding(itemClass);

    PropertyItemHandler retval;
    if (itemClassBinding != null) {
      if (itemClassBinding instanceof FieldClassBinding) {
        FieldClassBinding<?> fieldClassBinding = (FieldClassBinding<?>) itemClassBinding;
        retval = new FieldPropertyItemHandler(fieldClassBinding, (FieldPropertyBinding) propertyBinding);
      } else if (itemClassBinding instanceof AssemblyClassBinding) {
        retval = new AssemblyPropertyItemHandler((AssemblyClassBinding<?>) itemClassBinding,
            (AssemblyPropertyBinding) propertyBinding);
      } else {
        throw new UnsupportedOperationException(String.format("Unsupported class binding '%s' for class '%s'",
            itemClassBinding.getClass().getName(), itemClassBinding.getClazz().getName()));
      }
    } else {
      retval = new DataTypePropertyItemHandler(propertyBinding, bindingContext);
    }
    return retval;
  }

  PropertyBinding getPropertyBinding();

  List<Object> parse(PropertyBindingFilter filter, Object parent, JsonParsingContext parsingContext)
      throws BindingException, IOException;

  Object parse(Object parent, XmlParsingContext parsingContext) throws BindingException;

  void writeValue(Object value, PropertyBindingFilter filter, JsonWritingContext writingContext)
      throws BindingException, IOException;

  void writeXmlElement(Object child, QName itemWrapperQName, StartElement propertyParent,
      XmlWritingContext writingContext) throws BindingException;

  boolean isParsingXmlStartElement();

  boolean canHandleQName(QName nextQName);
}
