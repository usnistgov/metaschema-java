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

package gov.nist.secauto.metaschema.core.model.constraint;

import gov.nist.secauto.metaschema.core.model.IModelInstanceAbsolute;
import gov.nist.secauto.metaschema.core.model.constraint.impl.DefaultCardinalityConstraint;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents a rule requiring a Metaschema assembly data instance to have
 * elements with a minimum and/or maximum occurrence.
 */
public interface ICardinalityConstraint extends IConstraint {
  /**
   * Retrieve the required minimum occurrence of the target instance. If
   * specified, this value must be less than or equal to the value of
   * {@link IModelInstanceAbsolute#getMaxOccurs()} and greater than
   * {@link IModelInstanceAbsolute#getMinOccurs()}.
   *
   * @return a non-negative integer or {@code null} if not defined
   */
  @Nullable
  Integer getMinOccurs();

  /**
   * Retrieve the required maximum occurrence of the target instance. If
   * specified, this value must be less than the value of
   * {@link IModelInstanceAbsolute#getMaxOccurs()} and greater than or equal to
   * {@link IModelInstanceAbsolute#getMinOccurs()}.
   *
   * @return a non-negative integer or {@code null} if not defined
   */
  @Nullable
  Integer getMaxOccurs();

  @Override
  default <T, R> R accept(IConstraintVisitor<T, R> visitor, T state) {
    return visitor.visitCardinalityConstraint(this, state);
  }

  /**
   * Create a new constraint builder.
   *
   * @return the builder
   */
  @NonNull
  static Builder builder() {
    return new Builder();
  }

  final class Builder
      extends AbstractConstraintBuilder<Builder, ICardinalityConstraint> {
    private Integer minOccurs;
    private Integer maxOccurs;

    private Builder() {
      // disable construction
    }

    /**
     * Use the provided minimum occurrence to validate associated targets.
     *
     * @param value
     *          the expected occurrence
     * @return this builder
     */
    public Builder minOccurs(int value) {
      this.minOccurs = value;
      return this;
    }

    /**
     * Use the provided maximum occurrence to validate associated targets.
     *
     * @param value
     *          the expected occurrence
     * @return this builder
     */
    public Builder maxOccurs(int value) {
      this.maxOccurs = value;
      return this;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    protected void validate() {
      super.validate();

      if (getMinOccurs() == null && getMaxOccurs() == null) {
        throw new IllegalStateException("At least one of minOccurs or maxOccurs must be provided.");
      }
    }

    private Integer getMinOccurs() {
      return minOccurs;
    }

    private Integer getMaxOccurs() {
      return maxOccurs;
    }

    @Override
    protected ICardinalityConstraint newInstance() {
      return new DefaultCardinalityConstraint(
          getId(),
          getFormalName(),
          getDescription(),
          ObjectUtils.notNull(getSource()),
          getLevel(),
          getTarget(),
          getProperties(),
          getMinOccurs(),
          getMaxOccurs(),
          getRemarks());
    }
  }
}
