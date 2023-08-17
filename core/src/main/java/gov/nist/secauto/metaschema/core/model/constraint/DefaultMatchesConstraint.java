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
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class DefaultMatchesConstraint
    extends AbstractConstraint
    implements IMatchesConstraint {
  private final Pattern pattern;
  private final IDataTypeAdapter<?> dataType;

  /**
   * Create a new matches constraint, which enforces a value pattern and/or data
   * type.
   *
   * @param id
   *          the optional identifier for the constraint
   * @param formalName
   *          the constraint's formal name or {@code null} if not provided
   * @param description
   *          the constraint's semantic description or {@code null} if not
   *          provided
   * @param source
   *          information about the constraint source
   * @param level
   *          the significance of a violation of this constraint
   * @param target
   *          the Metapath expression identifying the nodes the constraint targets
   * @param properties
   *          a collection of associated properties
   * @param pattern
   *          the value pattern to match or {@code null} if there is no match
   *          pattern
   * @param dataType
   *          the value data type to match or {@code null} if there is no match
   *          data type
   * @param remarks
   *          optional remarks describing the intent of the constraint
   */
  private DefaultMatchesConstraint(
      @Nullable String id,
      @Nullable String formalName,
      @Nullable MarkupLine description,
      @NonNull ISource source,
      @NonNull Level level,
      @NonNull MetapathExpression target,
      @NonNull Map<QName, Set<String>> properties,
      @Nullable Pattern pattern,
      @Nullable IDataTypeAdapter<?> dataType,
      @Nullable MarkupMultiline remarks) {
    super(id, formalName, description, source, level, target, properties, remarks);
    if (pattern == null && dataType == null) {
      throw new IllegalArgumentException("a pattern or data type must be provided");
    }
    this.pattern = pattern;
    this.dataType = dataType;
  }

  @Override
  public Pattern getPattern() {
    return pattern;
  }

  @Override
  public IDataTypeAdapter<?> getDataType() {
    return dataType;
  }

  @Override
  public <T, R> R accept(IConstraintVisitor<T, R> visitor, T state) {
    return visitor.visitMatchesConstraint(this, state);
  }

  @NonNull
  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder
      extends AbstractConstraintBuilder<Builder, DefaultMatchesConstraint> {
    private Pattern pattern;
    private IDataTypeAdapter<?> datatype;

    private Builder() {
      // disable construction
    }

    public Builder regex(@NonNull String pattern) {
      return regex(ObjectUtils.notNull(Pattern.compile(pattern)));
    }

    public Builder regex(@NonNull Pattern pattern) {
      this.pattern = pattern;
      return this;
    }

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

    protected Pattern getPattern() {
      return pattern;
    }

    protected IDataTypeAdapter<?> getDatatype() {
      return datatype;
    }

    @Override
    protected DefaultMatchesConstraint newInstance() {
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
