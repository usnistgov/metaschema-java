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
package gov.nist.secauto.metaschema.binding;

import gov.nist.secauto.metaschema.binding.io.json.parser.JsonParsingContext;
import gov.nist.secauto.metaschema.binding.io.json.writer.JsonWritingContext;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsingContext;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWriter;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWritingContext;
import gov.nist.secauto.metaschema.binding.model.ClassBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

import java.util.Objects;
import java.util.function.Supplier;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;

class ObjectJavaTypeAdapter<CLASS> implements JavaTypeAdapter<CLASS> {
  private final ClassBinding<CLASS> classBinding;

  public ObjectJavaTypeAdapter(ClassBinding<CLASS> classBinding) {
    Objects.requireNonNull(classBinding, "classBinding");
    this.classBinding = classBinding;
  }

  protected ClassBinding<CLASS> getClassBinding() {
    return classBinding;
  }

  @Override
  public boolean isParsingStartElement() {
    return true;
  }

  @Override
  public boolean canHandleQName(QName nextQName) {
    // we are only handling the element being parsed
    return false;
  }

  @Override
  public CLASS parse(String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CLASS parse(XmlParsingContext parsingContext) throws BindingException {
    // delegates all parsing to the parse plan
    XmlParsePlan<CLASS> plan = getClassBinding().getXmlParsePlan(parsingContext.getBindingContext());
    return plan.parse(parsingContext);
  }

  @Override
  public CLASS parse(JsonParsingContext parsingContext) throws BindingException {
    // TODO: support same pathway as parse(XML)
    throw new UnsupportedOperationException();
  }

  @Override
  public void writeXmlElement(Object value, QName valueQName, StartElement parent, XmlWritingContext writingContext)
      throws BindingException {
    XmlWriter writer = getClassBinding().getXmlWriter();
    writer.writeXml(value, valueQName, writingContext);
  }

  @Override
  public void writeJsonFieldValue(Object value, PropertyBindingFilter filter, JsonWritingContext writingContext)
      throws BindingException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDefaultJsonFieldName() {
    throw new UnsupportedOperationException(
        "A bound object must always be referenced from an assembly or field property");
  }

  @Override
  public boolean isUnrappedValueAllowedInXml() {
    return false;
  }

  @Override
  public CLASS copy(CLASS obj) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Supplier<?> parseAndSupply(String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Supplier<CLASS> parseAndSupply(XmlParsingContext parsingContext) throws BindingException {
    CLASS retval = parse(parsingContext);
    return () -> retval;
  }

  @Override
  public Supplier<CLASS> parseAndSupply(JsonParsingContext parsingContext) throws BindingException {
    CLASS retval = parse(parsingContext);
    return () -> retval;
  }

}
