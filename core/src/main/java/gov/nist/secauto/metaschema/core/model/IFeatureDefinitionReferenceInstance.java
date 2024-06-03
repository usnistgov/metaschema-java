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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface IFeatureDefinitionReferenceInstance<
    DEFINITION extends IDefinition,
    INSTANCE extends INamedInstance>
    extends INamedInstance {

  @Override
  DEFINITION getDefinition();

  /**
   * Get the instance this definition is combined with.
   *
   * @return the instance or {@code null} if the definition is not inline
   */
  @Nullable
  default INSTANCE getInlineInstance() {
    return null;
  }

  @Override
  default String getEffectiveFormalName() {
    String result = getFormalName();
    return result == null ? getDefinition().getEffectiveFormalName() : result;
  }

  @Override
  default MarkupLine getEffectiveDescription() {
    MarkupLine result = getDescription();
    return result == null ? getDefinition().getEffectiveDescription() : result;
  }

  @Override
  @NonNull
  default String getEffectiveName() {
    String result = getUseName();
    if (result == null) {
      // fall back to the definition
      IDefinition def = getDefinition();
      result = def.getEffectiveName();
    }
    return result;
  }

  @Override
  @Nullable
  default Integer getEffectiveIndex() {
    Integer result = getUseIndex();
    if (result == null) {
      // fall back to the definition
      IDefinition def = getDefinition();
      result = def.getEffectiveIndex();
    }
    return result;
  }

  /**
   * The resolved default value, which allows an instance to override a
   * definition's default value.
   *
   * @return the default value or {@code null} if not defined on either the
   *         instance or definition
   */
  @Override
  @Nullable
  default Object getEffectiveDefaultValue() {
    Object retval = getDefaultValue();
    if (retval == null) {
      retval = getDefinition().getDefaultValue();
    }
    return retval;
  }
}
