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

package gov.nist.secauto.metaschema.model.common.constraint;

import gov.nist.secauto.metaschema.datatypes.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class DefaultAllowedValuesConstraint extends AbstractConstraint implements IAllowedValuesConstraint {
  private final boolean allowedOther;
  @NotNull
  private final Map<@NotNull String, @NotNull DefaultAllowedValue> allowedValues;

  /**
   * Construct a new allowed values constraint which ensures that a target instance's value match one
   * of the allowed values. This match is required if {@link #isAllowedOther()} is {@code false},
   * otherwise the constraint will generate a validation warning message if the target instance's
   * value does not match any of the associated allowed value constraints targeting it.
   * 
   * the associated test evaluates to {@link IBooleanItem#TRUE} against the target.
   * 
   * @param id
   *          the optional identifier for the constraint
   * @param target
   *          the Metapath expression identifying the nodes the constraint targets
   * @param allowedValues
   *          the list of allowed values for this constraint
   * @param allowedOther
   *          when {@code true} values other than the values specified by {@code allowedValues} are
   *          allowed, or disallowed if {@code false}
   * @param remarks
   *          optional remarks describing the intent of the constraint
   */
  public DefaultAllowedValuesConstraint(
      @Nullable String id,
      @NotNull MetapathExpression target,
      @NotNull Map<@NotNull String, @NotNull DefaultAllowedValue> allowedValues,
      boolean allowedOther,
      @Nullable MarkupMultiline remarks) {
    super(id, target, remarks);
    this.allowedValues = allowedValues;
    this.allowedOther = allowedOther;
  }

  @Override
  public Map<@NotNull String, @NotNull DefaultAllowedValue> getAllowedValues() {
    return allowedValues;
  }

  @Override
  public boolean isAllowedOther() {
    return allowedOther;
  }
}
