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

package gov.nist.secauto.metaschema.core.model;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Locale;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * A trait indicating that the implementation is a localized definition that is
 * declared in-line as an instance.
 *
 * @param <DEFINITION>
 *          the associated definition Java type
 * @param <INSTANCE>
 *          the associated instance Java type
 */
public interface IFeatureDefinitionInstanceInlined<
    DEFINITION extends IDefinition,
    INSTANCE extends INamedInstance>
    extends IDefinition, INamedInstance {
  @Override
  default boolean isInline() {
    // has to be inline
    return true;
  }

  @Override
  default QName getDefinitionQName() {
    return getReferencedDefinitionQName();
  }

  @Override
  default DEFINITION getDefinition() {
    return ObjectUtils.asType(this);
  }

  @Override
  @NonNull
  default INSTANCE getInlineInstance() {
    return ObjectUtils.asType(this);
  }

  @Override
  default String getEffectiveFormalName() {
    return getFormalName();
  }

  @Override
  default MarkupLine getEffectiveDescription() {
    return getDescription();
  }

  @Override
  default String getEffectiveName() {
    // don't use use-name
    return getName();
  }

  @Override
  default Integer getEffectiveIndex() {
    return getIndex();
  }

  @Override
  @Nullable
  default Object getEffectiveDefaultValue() {
    // This is an inline instance that is both a definition and an instance. Don't
    // delegate to the definition, since this would be redundant.
    return getDefaultValue();
  }

  /**
   * Generates a "coordinate" string for the provided inline definition instance.
   *
   * A coordinate consists of the element's:
   * <ul>
   * <li>containing Metaschema module's short name</li>
   * <li>model type</li>
   * <li>definition name</li>
   * <li>hash code</li>
   * </ul>
   *
   * @return the coordinate
   */
  @SuppressWarnings("null")
  @Override
  default String toCoordinates() {
    IModule module = getContainingModule();
    return String.format("%s-inline-definition:%s:%s/%s@%d",
        getModelType().toString().toLowerCase(Locale.ROOT),
        module.getShortName(),
        getContainingDefinition().getName(),
        getName(),
        hashCode());
  }
}
