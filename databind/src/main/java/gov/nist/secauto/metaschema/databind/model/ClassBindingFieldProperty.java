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

package gov.nist.secauto.metaschema.databind.model;

import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.info.IDataTypeHandler;

import java.lang.reflect.Field;

import edu.umd.cs.findbugs.annotations.NonNull;

class ClassBindingFieldProperty
    extends AbstractFieldProperty {

  @NonNull
  private final IFieldClassBinding definition;

  /**
   * Construct a new bound flag instance based on a Java property. The name of the
   * property is bound to the name of the instance.
   *
   * @param field
   *          the Java field to bind to
   * @param definition
   *          the field's Module definition
   * @param parentClassBinding
   *          the class binding for the field's containing class
   */
  public ClassBindingFieldProperty(
      @NonNull Field field,
      @NonNull IFieldClassBinding definition,
      @NonNull IAssemblyClassBinding parentClassBinding) {
    super(field, parentClassBinding);

    this.definition = definition;

    if (!isInXmlWrapped()) {
      if (!definition.isSimple()) { // NOPMD efficiency
        throw new IllegalStateException(
            String.format("Field '%s' on class '%s' is requested to be unwrapped, but it has flags preventing this.",
                field.getName(),
                parentClassBinding.getBoundClass().getName()));
      } else if (!getDefinition().getJavaTypeAdapter().isUnrappedValueAllowedInXml()) {
        throw new IllegalStateException(
            String.format(
                "Field '%s' on class '%s' is requested to be unwrapped, but its data type '%s' does not allow this.",
                field.getName(),
                parentClassBinding.getBoundClass().getName(),
                getDefinition().getJavaTypeAdapter().getPreferredName()));
      }
    }
  }

  @Override
  public final IFieldClassBinding getDefinition() {
    return definition;
  }

  @Override
  protected IDataTypeHandler newDataTypeHandler() {
    return IDataTypeHandler.newDataTypeHandler(this, getDefinition());
  }

  @Override
  public Object defaultValue() throws BindingException {
    Object retval = null;
    if (getMaxOccurs() == 1) {
      IFieldClassBinding definition = getDefinition();
      IBoundFieldValueInstance fieldValue = definition.getFieldValueInstance();

      Object defaultValue = fieldValue.getDefaultValue();
      if (defaultValue != null) {
        retval = definition.newInstance();
        fieldValue.setValue(retval, defaultValue);

        for (IBoundFlagInstance flag : definition.getFlagInstances()) {
          Object flagDefault = flag.defaultValue();
          if (flagDefault != null) {
            flag.setValue(retval, flagDefault);
          }
        }
      }
    } else {
      retval = getPropertyInfo().newPropertyCollector().getValue();
    }
    return retval;
  }
}
