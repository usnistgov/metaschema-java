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
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.model.IBoundDefinitionModelAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelChoiceGroup;
import gov.nist.secauto.metaschema.databind.model.IBoundInstanceModelGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.IBoundProperty;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundGroupedAssembly;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;

import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

/**
 * Represents an assembly model instance that is a member of a choice group
 * instance.
 */
public class InstanceModelGroupedAssembly
    extends AbstractAssemblyInstance<
        IBoundInstanceModelChoiceGroup,
        IBoundDefinitionModelAssembly,
        IBoundInstanceModelGroupedAssembly,
        IBoundDefinitionModelAssembly>
    // extends AbstractBoundInstanceModelGroupedNamed<BoundGroupedAssembly>
    implements IBoundInstanceModelGroupedAssembly {
  @NonNull
  private final BoundGroupedAssembly annotation;
  @NonNull
  private final IBoundDefinitionModelAssembly definition;
  @NonNull
  private final Lazy<Map<String, IBoundProperty>> jsonProperties;

  /**
   * Construct a new field model instance instance that is a member of a choice
   * group instance.
   *
   * @param annotation
   *          the Java annotation the instance is bound to
   * @param definition
   *          the assembly definition this instance is bound to
   * @param container
   *          the choice group instance containing the instance
   */
  public InstanceModelGroupedAssembly(
      @NonNull BoundGroupedAssembly annotation,
      @NonNull IBoundDefinitionModelAssembly definition,
      @NonNull IBoundInstanceModelChoiceGroup container) {
    super(container);
    this.annotation = annotation;
    this.definition = definition;
    // IBoundInstanceFlag jsonKey = getEffectiveJsonKey();
    // Predicate<IBoundInstanceFlag> flagFilter = jsonKey == null ? null : (flag) ->
    // !jsonKey.equals(flag);
    // return getDefinition().getJsonProperties(flagFilter);
    this.jsonProperties = ObjectUtils.notNull(Lazy.lazy(() -> getDefinition().getJsonProperties(null)));
  }

  private BoundGroupedAssembly getAnnotation() {
    return annotation;
  }

  // ------------------------------------------
  // - Start annotation driven code - CPD-OFF -
  // ------------------------------------------

  @Override
  public Class<?> getBoundClass() {
    return getAnnotation().binding();
  }

  @Override
  public Map<String, IBoundProperty> getJsonProperties() {
    return ObjectUtils.notNull(jsonProperties.get());
  }

  @Override
  public IBoundDefinitionModelAssembly getDefinition() {
    return definition;
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
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getAnnotation().remarks());
  }

  @Override
  public String getDiscriminatorValue() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().discriminatorValue());
  }

  @Override
  public String getUseName() {
    return ModelUtil.resolveNoneOrValue(getAnnotation().useName());
  }

  @Override
  public Integer getUseIndex() {
    return ModelUtil.resolveNullOrInteger(getAnnotation().useIndex());
  }
}
