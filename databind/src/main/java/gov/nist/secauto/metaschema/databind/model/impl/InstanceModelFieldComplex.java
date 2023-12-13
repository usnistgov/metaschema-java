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

package gov.nist.secauto.metaschema.databind.model.impl;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldComplex;

import java.lang.reflect.Field;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

/**
 * Implements a Metaschema module field instance bound to a Java field,
 * supported by a bound definition class.
 */
public class InstanceModelFieldComplex
    extends AbstractBoundInstanceField
    implements IBoundInstanceModelFieldComplex {
  @NonNull
  private final DefinitionField definition;
  @NonNull
  private final Lazy<Object> defaultValue;

  /**
   * Construct a new field instance bound to a Java field, supported by a bound
   * definition class.
   *
   * @param javaField
   *          the Java field bound to this instance
   * @param definition
   *          the assembly definition this instance is bound to
   * @param containingDefinition
   *          the definition containing this instance
   */
  public InstanceModelFieldComplex(
      @NonNull Field javaField,
      @NonNull DefinitionField definition,
      @NonNull IBoundDefinitionAssembly containingDefinition) {
    super(javaField, containingDefinition);
    this.definition = definition;

    if (!isValueWrappedInXml()) {
      if (!definition.isSimple()) { // NOPMD efficiency
        throw new IllegalStateException(
            String.format("Field '%s' on class '%s' is requested to be unwrapped, but it has flags preventing this.",
                javaField.getName(),
                containingDefinition.getBoundClass().getName()));
      } else if (!getDefinition().getJavaTypeAdapter().isUnrappedValueAllowedInXml()) {
        throw new IllegalStateException(
            String.format(
                "Field '%s' on class '%s' is requested to be unwrapped, but its data type '%s' does not allow this.",
                javaField.getName(),
                containingDefinition.getBoundClass().getName(),
                getDefinition().getJavaTypeAdapter().getPreferredName()));
      }
    }
    this.defaultValue = ObjectUtils.notNull(Lazy.lazy(() -> {
      Object retval = null;
      if (getMaxOccurs() == 1) {
        IBoundFieldValue fieldValue = definition.getFieldValue();

        Object fieldValueDefault = fieldValue.getDefaultValue();
        if (fieldValueDefault != null) {
          retval = newInstance();
          fieldValue.setValue(retval, fieldValueDefault);

          for (IBoundInstanceFlag flag : definition.getFlagInstances()) {
            Object flagDefault = flag.getEffectiveDefaultValue();
            if (flagDefault != null) {
              flag.setValue(retval, flagDefault);
            }
          }
        }
      }
      return retval;
    }));
  }

  @Override
  public DefinitionField getDefinition() {
    return definition;
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue.get();
  }
}
