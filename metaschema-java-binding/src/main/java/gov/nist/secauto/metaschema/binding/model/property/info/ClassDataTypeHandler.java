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
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.datatypes.adapter.JavaTypeAdapter;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

public class ClassDataTypeHandler implements DataTypeHandler {
  private final ClassBinding classBinding;

  public ClassDataTypeHandler(ClassBinding classBinding) {
    Objects.requireNonNull(classBinding, "classBinding");
    this.classBinding = classBinding;
  }

  @Override
  public JavaTypeAdapter<?> getJavaTypeAdapter() {
    // this is always null
    return null;
  }

  @Override
  public ClassBinding getClassBinding() {
    return classBinding;
  }

  @Override
  public boolean isUnrappedValueAllowedInXml() {
    // classes are always wrapped
    return false;
  }

  @Override
  public boolean get(PropertyCollector collector, Object parentInstance, JsonParsingContext context)
      throws BindingException, IOException {
    return classBinding.readItem(collector, parentInstance, context);
  }

  @Override
  public boolean get(PropertyCollector collector, Object parentInstance, StartElement start, XmlParsingContext context)
      throws BindingException, IOException, XMLStreamException {
    return classBinding.readItem(collector, parentInstance, start, context);
  }

  @Override
  public void accept(Object item, QName currentParentName, XmlWritingContext context)
      throws IOException, XMLStreamException {
    classBinding.writeItem(item, currentParentName, context);
  }

  @Override
  public void writeItems(Collection<? extends Object> items, boolean writeObjectWrapper, JsonWritingContext context)
      throws IOException {
    classBinding.writeItems(items, writeObjectWrapper, context);
  }
}
