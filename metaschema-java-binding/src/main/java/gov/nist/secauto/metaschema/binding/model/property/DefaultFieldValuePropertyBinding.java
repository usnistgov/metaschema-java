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
package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.JavaTypeAdapter;
import gov.nist.secauto.metaschema.binding.io.xml.parser.DefaultFieldValuePropertyParser;
import gov.nist.secauto.metaschema.binding.io.xml.parser.FieldValueXmlPropertyParser;
import gov.nist.secauto.metaschema.binding.model.FieldClassBinding;
import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonFieldValueName;

import java.util.Objects;

import javax.xml.namespace.QName;

public class DefaultFieldValuePropertyBinding extends AbstractPropertyBinding implements FieldValuePropertyBinding {

  private final FieldClassBinding<?> classBinding;
  private final FieldValue fieldValueAnnotation;
  private final JsonFieldValueName jsonFieldValueName;

  public DefaultFieldValuePropertyBinding(FieldClassBinding<?> classBinding, BasicPropertyInfo propertyInfo,
      FieldValue fieldValueAnnotation, JsonFieldValueName jsonFieldValueName) {
    super(propertyInfo);
    Objects.requireNonNull(classBinding, "classBinding");
    Objects.requireNonNull(fieldValueAnnotation, "fieldValueAnnotation");
    this.classBinding = classBinding;
    this.fieldValueAnnotation = fieldValueAnnotation;
    this.jsonFieldValueName = jsonFieldValueName;
  }

  protected FieldClassBinding<?> getClassBinding() {
    return classBinding;
  }

  @Override
  public PropertyBindingType getPropertyBindingType() {
    return PropertyBindingType.FIELD_VALUE;
  }

  protected FieldValue getFieldValueAnnotation() {
    return fieldValueAnnotation;
  }

  protected JsonFieldValueName getJsonFieldValueName() {
    return jsonFieldValueName;
  }

  @Override
  public FieldValueXmlPropertyParser newXmlPropertyParser(BindingContext bindingContext) throws BindingException {
    return new DefaultFieldValuePropertyParser(this, bindingContext);
  }

  @Override
  public QName getXmlQName() {
    // always null
    return null;
  }

  @Override
  public String getJsonFieldName(BindingContext bindingContext) throws BindingException {
    String retval;
    if (getClassBinding().getJsonValueKeyFlagPropertyBinding() != null) {
      retval = null;
    } else if (getJsonFieldValueName() != null) {
      retval = getJsonFieldValueName().name();
    } else {
      // use the default from the java type binding
      JavaTypeAdapter<?> javaTypeAdapter = bindingContext.getJavaTypeAdapter(getPropertyInfo().getItemType());
      if (javaTypeAdapter == null) {
        throw new BindingException(String.format(
            "Unable to determine the JSON field name for the property '%s' on class '$s'. "
                + "Perhaps the data type is not bound?",
            getPropertyInfo().getSimpleName(), getClassBinding().getClazz().getName()));
      } else {
        retval = bindingContext.getJavaTypeAdapter(getPropertyInfo().getItemType()).getDefaultJsonFieldName();
      }
    }
    return retval;
  }

}
