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

package gov.nist.secauto.metaschema.binding.model;

import gov.nist.secauto.metaschema.binding.BindingContext;
import gov.nist.secauto.metaschema.binding.BindingException;
import gov.nist.secauto.metaschema.binding.io.json.parser.JsonReader;
import gov.nist.secauto.metaschema.binding.io.json.writer.AssemblyJsonWriter;
import gov.nist.secauto.metaschema.binding.io.xml.parser.XmlParsePlan;
import gov.nist.secauto.metaschema.binding.io.xml.writer.XmlWriter;
import gov.nist.secauto.metaschema.binding.model.annotations.Assembly;
import gov.nist.secauto.metaschema.binding.model.annotations.FieldValue;
import gov.nist.secauto.metaschema.binding.model.annotations.Flag;
import gov.nist.secauto.metaschema.binding.model.annotations.RootWrapper;
import gov.nist.secauto.metaschema.binding.model.property.FlagPropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBinding;
import gov.nist.secauto.metaschema.binding.model.property.PropertyBindingFilter;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public interface ClassBinding<CLASS> {

  public static <CLASS> ClassBinding<CLASS> newClassBinding(Class<CLASS> clazz) throws BindingException {
    boolean hasFlag = false;
    boolean hasFieldValue = false;
    boolean hasModelProperty = false;
    for (Field javaField : clazz.getDeclaredFields()) {
      if (javaField.isAnnotationPresent(FieldValue.class)) {
        hasFieldValue = true;
      } else if (javaField.isAnnotationPresent(Flag.class)) {
        hasFlag = true;
      } else if (javaField.isAnnotationPresent(gov.nist.secauto.metaschema.binding.model.annotations.Field.class)
          || javaField.isAnnotationPresent(Assembly.class)) {
        hasModelProperty = true;
      }
    }

    if (hasFieldValue && hasModelProperty) {
      throw new BindingException(
          String.format("Class '%s' contains a FieldValue annotation and Field and/or Assembly annotations."
              + " FieldValue can only be used with Flag annotations.", clazz.getName()));
    }

    ClassBinding<CLASS> retval;
    if (hasFieldValue) {
      retval = new FieldClassBindingImpl<CLASS>(clazz);
    } else if (hasFlag || hasModelProperty) {
      retval = new AssemblyClassBindingImpl<CLASS>(clazz);
    } else {
      retval = null;
    }
    return retval;
  }

  Class<CLASS> getClazz();

  List<FlagPropertyBinding> getFlagPropertyBindings();

  FlagPropertyBinding getJsonKeyFlagPropertyBinding();

  Map<String, PropertyBinding> getJsonPropertyBindings(BindingContext bindingContext, PropertyBindingFilter filter)
      throws BindingException;

  boolean hasRootWrapper();

  RootWrapper getRootWrapper();

  XmlParsePlan<CLASS> getXmlParsePlan(BindingContext bindingContext) throws BindingException;

  XmlWriter getXmlWriter() throws BindingException;

  AssemblyJsonWriter<CLASS> getAssemblyJsonWriter(BindingContext bindingContext) throws BindingException;

  JsonReader<CLASS> getJsonReader(BindingContext bindingContext) throws BindingException;

  CLASS newInstance() throws BindingException;

  void callBeforeDeserialize(Object obj, Object parent) throws BindingException;

  void callAfterDeserialize(Object obj, Object parent) throws BindingException;
}
