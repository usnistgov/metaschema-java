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

import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.item.IBooleanItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class DefaultAllowedValuesConstraint
    extends AbstractConstraint
    implements IAllowedValuesConstraint {
  private final boolean allowedOther;
  @NonNull
  private final Extensible extensible;
  @NonNull
  private final Map<String, DefaultAllowedValue> allowedValues;

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
   * @param formalName
   *          the constraint's formal name or {@code null} if not provided
   * @param description
   *          the constraint's semantic description or {@code null} if not provided
   * @param source
   *          information about the constraint source
   * @param level
   *          the significance of a violation of this constraint
   * @param target
   *          the Metapath expression identifying the nodes the constraint targets
   * @param properties
   *          a collection of associated properties
   * @param allowedValues
   *          the list of allowed values for this constraint
   * @param allowedOther
   *          when {@code true} values other than the values specified by {@code allowedValues} are
   *          allowed, or disallowed if {@code false}
   * @param extensible
   *          indicates the degree to which extended values should be allowed
   * @param remarks
   *          optional remarks describing the intent of the constraint
   */
  private DefaultAllowedValuesConstraint( // NOPMD necessary
      @Nullable String id,
      @Nullable String formalName,
      @Nullable MarkupLine description,
      @NonNull ISource source,
      @NonNull Level level,
      @NonNull MetapathExpression target,
      @NonNull Map<QName, Set<String>> properties,
      @NonNull Map<String, DefaultAllowedValue> allowedValues,
      boolean allowedOther,
      @NonNull Extensible extensible,
      @Nullable MarkupMultiline remarks) {
    super(id, formalName, description, source, level, target, properties, remarks);
    this.allowedValues = allowedValues;
    this.allowedOther = allowedOther;
    this.extensible = extensible;
  }

  @Override
  public Map<String, DefaultAllowedValue> getAllowedValues() {
    return allowedValues;
  }

  @Override
  public boolean isAllowedOther() {
    return allowedOther;
  }

  @Override
  public Extensible getExtensible() {
    return extensible;
  }

  @Override
  public <T, R> R accept(IConstraintVisitor<T, R> visitor, T state) {
    return visitor.visitAllowedValues(this, state);
  }

  @NonNull
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder
      extends AbstractConstraintBuilder<Builder, DefaultAllowedValuesConstraint> {
    @NonNull
    private final Map<String, DefaultAllowedValue> allowedValues = new LinkedHashMap<>(); // NOPMD not thread safe
    private boolean allowedOther = IAllowedValuesConstraint.DEFAULT_ALLOW_OTHER;
    @NonNull
    private Extensible extensible = IAllowedValuesConstraint.DEFAULT_EXTENSIBLE;

    private Builder() {
      // disable construction
    }

    public Builder allowedValue(@NonNull DefaultAllowedValue allowedValue) {
      this.allowedValues.put(allowedValue.getValue(), allowedValue);
      return this;
    }

    public Builder allowedValues(@NonNull Map<String, DefaultAllowedValue> allowedValues) {
      this.allowedValues.putAll(allowedValues);
      return this;
    }

    public Builder allowedOther(boolean bool) {
      this.allowedOther = bool;
      return this;
    }

    public Builder extensible(@NonNull Extensible extensible) {
      this.extensible = extensible;
      return this;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @NonNull
    protected Map<String, DefaultAllowedValue> getAllowedValues() {
      return allowedValues;
    }

    protected boolean isAllowedOther() {
      return allowedOther;
    }

    @NonNull
    protected Extensible getExtensible() {
      return extensible;
    }

    @Override
    protected DefaultAllowedValuesConstraint newInstance() {
      return new DefaultAllowedValuesConstraint(
          getId(),
          getFormalName(),
          getDescription(),
          ObjectUtils.notNull(getSource()),
          getLevel(),
          getTarget(),
          getProperties(),
          getAllowedValues(),
          isAllowedOther(),
          getExtensible(),
          getRemarks());
    }
  }
}
