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

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.model.constraint.impl.DefaultMatchesConstraint;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents a rule requiring the value of a field or flag to match a pattern
 * and/or conform to an identified data type.
 */
public interface IMatchesConstraint extends IConstraint {
  /**
   * Get the expected pattern.
   *
   * @return the expected pattern or {@code null} if there is no expected pattern
   */
  @Nullable
  Pattern getPattern();

  /**
   * Get the expected data type.
   *
   * @return the expected data type or {@code null} if there is no expected data
   *         type
   */
  @Nullable
  IDataTypeAdapter<?> getDataType();

  @Override
  default <T, R> R accept(IConstraintVisitor<T, R> visitor, T state) {
    return visitor.visitMatchesConstraint(this, state);
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
      extends AbstractConstraintBuilder<Builder, IMatchesConstraint> {
    private Pattern pattern;
    private IDataTypeAdapter<?> datatype;

    private Builder() {
      // disable construction
    }

    /**
     * Use the provided pattern to validate associated values.
     *
     * @param pattern
     *          the pattern to use
     * @return this builder
     */
    public Builder regex(@NonNull String pattern) {
      return regex(ObjectUtils.notNull(Pattern.compile(pattern)));
    }

    /**
     * Use the provided pattern to validate associated values.
     *
     * @param pattern
     *          the expected pattern
     * @return this builder
     */
    public Builder regex(@NonNull Pattern pattern) {
      this.pattern = pattern;
      return this;
    }

    /**
     * Use the provided data type to validate associated values.
     *
     * @param datatype
     *          the expected data type
     * @return this builder
     */
    public Builder datatype(@NonNull IDataTypeAdapter<?> datatype) {
      this.datatype = datatype;
      return this;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    protected void validate() {
      super.validate();

      if (getPattern() == null && getDatatype() == null) {
        throw new IllegalStateException("A pattern or data type must be provided at minimum.");
      }
    }

    private Pattern getPattern() {
      return pattern;
    }

    private IDataTypeAdapter<?> getDatatype() {
      return datatype;
    }

    @Override
    protected IMatchesConstraint newInstance() {
      return new DefaultMatchesConstraint(
          getId(),
          getFormalName(),
          getDescription(),
          ObjectUtils.notNull(getSource()),
          getLevel(),
          getTarget(),
          getProperties(),
          getPattern(),
          getDatatype(),
          getRemarks());
    }
  }
}
