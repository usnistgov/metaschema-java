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

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.AbstractFieldInstance;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundFieldValue;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelFieldComplex;
import gov.nist.secauto.metaschema.databind.model.IBoundProperty;
import gov.nist.secauto.metaschema.databind.model.IGroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundField;
import gov.nist.secauto.metaschema.databind.model.annotations.GroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.info.IModelInstanceCollectionInfo;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Predicate;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.talsmasoftware.lazy4j.Lazy;

/**
 * Implements a Metaschema module field instance bound to a Java field,
 * supported by a bound definition class.
 */
public final class InstanceModelFieldComplex
    extends AbstractFieldInstance<
        IBoundDefinitionModelAssembly,
        IBoundDefinitionModelFieldComplex,
        IBoundInstanceModelFieldComplex,
        IBoundDefinitionModelAssembly>
    implements IBoundInstanceModelFieldComplex, IFeatureInstanceModelGroupAs {
  @NonNull
  private final Field javaField;
  @NonNull
  private final BoundField annotation;
  @NonNull
  private final Lazy<IModelInstanceCollectionInfo> collectionInfo;
  @NonNull
  private final IGroupAs groupAs;
  @NonNull
  private final DefinitionField definition;
  @NonNull
  private final Lazy<Object> defaultValue;
  @NonNull
  private final Lazy<Map<String, IBoundProperty>> jsonProperties;

  public static InstanceModelFieldComplex newInstance(
      @NonNull Field javaField,
      @NonNull DefinitionField definition,
      @NonNull IBoundDefinitionModelAssembly parent) {
    BoundField annotation = ModelUtil.getAnnotation(javaField, BoundField.class);
    if (!annotation.inXmlWrapped()) {
      if (definition.hasChildren()) { // NOPMD efficiency
        throw new IllegalStateException(
            String.format("Field '%s' on class '%s' is requested to be unwrapped, but it has flags preventing this.",
                javaField.getName(),
                parent.getBoundClass().getName()));
      }
      if (!definition.getJavaTypeAdapter().isUnrappedValueAllowedInXml()) {
        throw new IllegalStateException(
            String.format(
                "Field '%s' on class '%s' is requested to be unwrapped, but its data type '%s' does not allow this.",
                javaField.getName(),
                parent.getBoundClass().getName(),
                definition.getJavaTypeAdapter().getPreferredName()));
      }
    }

    IGroupAs groupAs = ModelUtil.groupAs(
        annotation.groupAs(),
        parent.getContainingModule());
    if (annotation.maxOccurs() == -1 || annotation.maxOccurs() > 1) {
      if (IGroupAs.SINGLETON_GROUP_AS.equals(groupAs)) {
        throw new IllegalStateException(String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
            javaField.getName(),
            javaField.getDeclaringClass().getName(),
            GroupAs.class.getName())); // NOPMD false positive
      }
    } else if (!IGroupAs.SINGLETON_GROUP_AS.equals(groupAs)) {
      // max is 1 and a groupAs is set
      throw new IllegalStateException(
          String.format(
              "Field '%s' on class '%s' has the '%s' annotation, but maxOccurs=1. A groupAs must not be specfied.",
              javaField.getName(),
              javaField.getDeclaringClass().getName(),
              GroupAs.class.getName())); // NOPMD false positive
    }
    return new InstanceModelFieldComplex(javaField, annotation, groupAs, definition, parent);
  }

  /**
   * Construct a new field instance bound to a Java field, supported by a bound
   * definition class.
   *
   * @param javaField
   *          the Java field bound to this instance
   * @param definition
   *          the assembly definition this instance is bound to
   * @param parent
   *          the definition containing this instance
   */
  @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "Use of final fields")
  private InstanceModelFieldComplex(
      @NonNull Field javaField,
      @NonNull BoundField annotation,
      @NonNull IGroupAs groupAs,
      @NonNull DefinitionField definition,
      @NonNull IBoundDefinitionModelAssembly parent) {
    super(parent);
    this.javaField = javaField;
    this.annotation = annotation;
    this.collectionInfo = ObjectUtils.notNull(Lazy.lazy(() -> IModelInstanceCollectionInfo.of(this)));
    this.groupAs = groupAs;
    this.definition = definition;

    this.defaultValue = ObjectUtils.notNull(Lazy.lazy(() -> {
      Object retval = null;
      if (getMaxOccurs() == 1) {
        IBoundFieldValue fieldValue = definition.getFieldValue();

        Object fieldValueDefault = fieldValue.getDefaultValue();
        if (fieldValueDefault != null) {
          retval = newInstance();
          fieldValue.setValue(retval, fieldValueDefault);

          for (IBoundInstanceFlag flag : definition.getFlagInstances()) {
            Object flagDefault = flag.getResolvedDefaultValue();
            if (flagDefault != null) {
              flag.setValue(retval, flagDefault);
            }
          }
        }
      }
      return retval;
    }));
    this.jsonProperties = ObjectUtils.notNull(Lazy.lazy(() -> {
      Predicate<IBoundInstanceFlag> flagFilter = null;
      IBoundInstanceFlag jsonKey = getEffectiveJsonKey();
      if (jsonKey != null) {
        flagFilter = flag -> !jsonKey.equals(flag);
      }
      return getDefinition().getJsonProperties(flagFilter);
    }));
  }

  @Override
  public Field getField() {
    return javaField;
  }

  /**
   * Get the binding Java annotation.
   *
   * @return the binding Java annotation
   */
  @NonNull
  public BoundField getAnnotation() {
    return annotation;
  }

  @SuppressWarnings("null")
  @Override
  public IModelInstanceCollectionInfo getCollectionInfo() {
    return collectionInfo.get();
  }

  @Override
  public DefinitionField getDefinition() {
    return definition;
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue.get();
  }

  @Override
  public Map<String, IBoundProperty> getJsonProperties() {
    return ObjectUtils.notNull(jsonProperties.get());
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @Override
  public IGroupAs getGroupAs() {
    return groupAs;
  }

  @Override
  public String getFormalName() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().formalName());
  }

  @Override
  public MarkupLine getDescription() {
    return ModelUtil.resolveToMarkupLine(getAnnotation().description());
  }

  @Override
  public String getUseName() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().useName());
  }

  @Override
  public Integer getUseIndex() {
    int value = getAnnotation().useIndex();
    return value == Integer.MIN_VALUE ? null : value;
  }

  @Override
  public boolean isInXmlWrapped() {
    return getAnnotation().inXmlWrapped();
  }

  @Override
  public int getMinOccurs() {
    return getAnnotation().minOccurs();
  }

  @Override
  public int getMaxOccurs() {
    return getAnnotation().maxOccurs();
  }

  @Override
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getAnnotation().remarks());
  }
}
