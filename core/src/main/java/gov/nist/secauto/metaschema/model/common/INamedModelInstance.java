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

package gov.nist.secauto.metaschema.model.common;

import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface INamedModelInstance extends INamedInstance, IModelInstance {
  @Override
  @NonNull
  IFlagContainer getDefinition();

  @Override
  default String getJsonName() {
    @NonNull String retval;
    if (getMaxOccurs() == -1 || getMaxOccurs() > 1) {
      @NonNull String groupAsName = ObjectUtils.requireNonNull(getGroupAsName(),
          ObjectUtils.notNull(String.format("null group-as name in instance '%s' on definition '%s' in '%s'",
              this.getName(),
              this.getContainingDefinition().getName(),
              this.getContainingMetaschema().getLocation())));
      retval = groupAsName;
    } else {
      retval = getEffectiveName();
    }
    return retval;
  }

  /**
   * Get the item values for the provided {@code instanceValue}. An instance may be singular or many
   * valued.
   *
   * @param instanceValue
   *          the instance
   * @return the item values or an empty collection if no item values exist
   */
  @NonNull
  Collection<?> getItemValues(Object instanceValue);

  /**
   * Indicates if a flag's value can be used as a property name in the containing object in JSON who's
   * value will be the object containing the flag. In such cases, the flag will not appear in the
   * object. This is only allowed if the flag is required, as determined by a {@code true} result from
   * {@link IFlagInstance#isRequired()}. The {@link IFlagInstance} can be retrieved using
   * {@link #getJsonKeyFlagInstance()}.
   *
   * @return {@code true} if the flag's value can be used as a property name, or {@code false}
   *         otherwise
   * @see #getJsonKeyFlagInstance()
   */
  default boolean hasJsonKey() {
    return getDefinition().hasJsonKey();
  }

  /**
   * Retrieves the flag instance to use as as the property name for the containing object in JSON
   * who's value will be the object containing the flag.
   *
   * @return the flag instance if a JSON key is configured, or {@code null} otherwise
   * @see #hasJsonKey()
   */
  @Nullable
  default IFlagInstance getJsonKeyFlagInstance() {
    return getDefinition().getJsonKeyFlagInstance();
  }
}