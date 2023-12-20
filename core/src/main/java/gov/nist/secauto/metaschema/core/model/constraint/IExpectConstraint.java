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

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.model.constraint.impl.DefaultExpectConstraint;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Represents a rule requiring a Metaschema assembly, field, or flag data
 * instance to pass a Metapath-based test.
 * <p>
 * A custom message can be used to indicate what a test failure signifies.
 */

public interface IExpectConstraint extends IConstraint {
  @NonNull
  MetapathExpression getTest();

  /**
   * A message to emit when the constraint is violated. Allows embedded Metapath
   * expressions using the syntax {@code \{path\}}.
   *
   * @return the message if defined or {@code null} otherwise
   */
  String getMessage();

  CharSequence generateMessage(@NonNull INodeItem item, @NonNull DynamicContext context);

  @Override
  default <T, R> R accept(IConstraintVisitor<T, R> visitor, T state) {
    return visitor.visitExpectConstraint(this, state);
  }

  @NonNull
  static Builder builder() {
    return new Builder();
  }

  class Builder
      extends AbstractConstraintBuilder<Builder, IExpectConstraint> {
    private MetapathExpression test;
    private String message;

    private Builder() {
      // disable construction
    }

    @NonNull
    public Builder test(@NonNull MetapathExpression test) {
      this.test = test;
      return this;
    }

    @NonNull
    public Builder message(@NonNull String message) {
      this.message = message;
      return this;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    protected void validate() {
      super.validate();

      ObjectUtils.requireNonNull(getTest());
    }

    protected MetapathExpression getTest() {
      return test;
    }

    protected String getMessage() {
      return message;
    }

    @Override
    protected IExpectConstraint newInstance() {
      return new DefaultExpectConstraint(
          getId(),
          getFormalName(),
          getDescription(),
          ObjectUtils.notNull(getSource()),
          getLevel(),
          getTarget(),
          getProperties(),
          ObjectUtils.requireNonNull(getTest()),
          getMessage(),
          getRemarks());
    }
  }
}
