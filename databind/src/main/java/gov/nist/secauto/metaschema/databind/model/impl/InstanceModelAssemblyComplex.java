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
import gov.nist.secauto.metaschema.core.model.AbstractAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IBoundObject;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceFlag;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundProperty;
import gov.nist.secauto.metaschema.databind.model.IGroupAs;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundAssembly;
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
 * Implements a Metaschema module assembly instance bound to a Java field,
 * supported by a bound definition class.
 */
// TODO: implement getProperties()
public final class InstanceModelAssemblyComplex
    extends AbstractAssemblyInstance<
        IBoundDefinitionModelAssembly,
        IBoundDefinitionModelAssembly,
        IBoundInstanceModelAssembly,
        IBoundDefinitionModelAssembly>
    implements IBoundInstanceModelAssembly, IFeatureInstanceModelGroupAs<IBoundObject> {
  @NonNull
  private final Field javaField;
  @NonNull
  private final BoundAssembly annotation;
  @NonNull
  private final Lazy<IModelInstanceCollectionInfo<IBoundObject>> collectionInfo;
  @NonNull
  private final IBoundDefinitionModelAssembly definition;
  @NonNull
  private final IGroupAs groupAs;
  @NonNull
  private final Lazy<Map<String, IBoundProperty<?>>> jsonProperties;

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
   * @return the instance
   */
  @NonNull
  public static InstanceModelAssemblyComplex newInstance(
      @NonNull Field javaField,
      @NonNull IBoundDefinitionModelAssembly definition,
      @NonNull IBoundDefinitionModelAssembly containingDefinition) {
    BoundAssembly annotation = ModelUtil.getAnnotation(javaField, BoundAssembly.class);
    IGroupAs groupAs = ModelUtil.resolveDefaultGroupAs(
        annotation.groupAs(),
        containingDefinition.getContainingModule());
    if (annotation.maxOccurs() == -1 || annotation.maxOccurs() > 1) {
      if (IGroupAs.SINGLETON_GROUP_AS.equals(groupAs)) {
        throw new IllegalStateException(String.format("Field '%s' on class '%s' is missing the '%s' annotation.",
            javaField.getName(),
            containingDefinition.getBoundClass().getName(),
            GroupAs.class.getName()));
      }
    } else if (!IGroupAs.SINGLETON_GROUP_AS.equals(groupAs)) {
      // max is 1 and a groupAs is set
      throw new IllegalStateException(
          String.format(
              "Field '%s' on class '%s' has the '%s' annotation, but maxOccurs=1. A groupAs must not be specfied.",
              javaField.getName(),
              containingDefinition.getBoundClass().getName(),
              GroupAs.class.getName()));
    }
    return new InstanceModelAssemblyComplex(javaField, annotation, groupAs, definition, containingDefinition);
  }

  @SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "Use of final fields")
  private InstanceModelAssemblyComplex(
      @NonNull Field javaField,
      @NonNull BoundAssembly annotation,
      @NonNull IGroupAs groupAs,
      @NonNull IBoundDefinitionModelAssembly definition,
      @NonNull IBoundDefinitionModelAssembly containingDefinition) {
    super(containingDefinition);
    this.javaField = javaField;
    this.annotation = annotation;
    this.groupAs = groupAs;
    this.collectionInfo = ObjectUtils.notNull(Lazy.lazy(() -> IModelInstanceCollectionInfo.of(this)));
    this.definition = definition;
    this.jsonProperties = ObjectUtils.notNull(Lazy.lazy(() -> {
      IBoundInstanceFlag jsonKey = getEffectiveJsonKey();
      Predicate<IBoundInstanceFlag> flagFilter = jsonKey == null ? null : flag -> !jsonKey.equals(flag);
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
  public BoundAssembly getAnnotation() {
    return annotation;
  }

  @SuppressWarnings("null")
  @Override
  public IModelInstanceCollectionInfo<IBoundObject> getCollectionInfo() {
    return collectionInfo.get();
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @Override
  public Map<String, IBoundProperty<?>> getJsonProperties() {
    return ObjectUtils.notNull(jsonProperties.get());
  }

  @Override
  public IBoundDefinitionModelAssembly getDefinition() {
    return definition;
  }

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
