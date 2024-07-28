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

import gov.nist.secauto.metaschema.core.model.constraint.IFeatureValueConstrained;

import java.util.Locale;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IDefinition extends INamedModelElement, IAttributable, IFeatureValueConstrained {

  @NonNull
  ModuleScopeEnum DEFAULT_DEFINITION_MODEL_SCOPE = ModuleScopeEnum.INHERITED;

  /**
   * Retrieve the definition's scope within the context of its defining module.
   *
   * @return the module scope
   */
  @NonNull
  default ModuleScopeEnum getModuleScope() {
    return ModuleScopeEnum.LOCAL;
  }

  /**
   * The qualified name for the definition.
   * <p>
   * This name is the combination of the definition's namespace, which is the
   * module's namespace, and the definition's name.
   *
   * @return the definition's qualified name
   */
  @NonNull
  default QName getDefinitionQName() {
    return new QName(
        getContainingModule().getXmlNamespace().toASCIIString(),
        getName());
  }

  /**
   * Determine if the definition is defined inline, meaning the definition is
   * declared where it is used.
   *
   * @return {@code true} if the definition is declared inline or {@code false} if
   *         the definition is able to be globally referenced
   */
  default boolean isInline() {
    return getInlineInstance() != null;
  }

  /**
   * If {@link #isInline()} is {@code true}, return the instance the definition is
   * inlined for.
   *
   * @return the instance or {@code null} otherwise
   */
  INamedInstance getInlineInstance();

  /**
   * Generates a coordinate string for the provided information element
   * definition.
   *
   * A coordinate consists of the element's:
   * <ul>
   * <li>containing Metaschema's short name</li>
   * <li>model type</li>
   * <li>name</li>
   * <li>hash code</li>
   * </ul>
   *
   * @return the coordinate
   */
  @SuppressWarnings("null")
  @Override
  default String toCoordinates() {
    return String.format("%s:%s-definition:%s(%d)",
        getContainingModule().getShortName(),
        getModelType().toString().toUpperCase(Locale.ROOT),
        getName(),
        hashCode());
  }

  @Nullable
  default IResourceLocation getLocation(@NonNull Object itemValue) {
    return itemValue instanceof IBoundObject ? ((IBoundObject) itemValue).getMetaschemaData() : null;
  }
}
