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

import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.Level;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.validation.IValidationFinding;

import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents an individual constraint validation issue.
 */
public class ConstraintValidationFinding implements IValidationFinding { // NOPMD - intentional
  @NonNull
  private final List<? extends IConstraint> constraints;
  @NonNull
  private final CharSequence message;
  @NonNull
  private final INodeItem node;
  @NonNull
  private final List<? extends INodeItem> targets;
  private final Throwable cause;
  private final Level severity;

  private ConstraintValidationFinding(
      @NonNull List<? extends IConstraint> constraints,
      @NonNull INodeItem node,
      @NonNull CharSequence message,
      @NonNull List<? extends INodeItem> targets,
      @NonNull Level severity,
      @Nullable Throwable cause) {
    this.constraints = constraints;
    this.node = node;
    this.message = message;
    this.targets = targets;
    this.severity = severity;
    this.cause = cause;
  }

  public List<? extends IConstraint> getConstraints() {
    return constraints;
  }

  @Override
  public CharSequence getMessage() {
    return message;
  }

  public INodeItem getNode() {
    return node;
  }

  public List<? extends INodeItem> getTargets() {
    return targets;
  }

  @Override
  public Throwable getCause() {
    return cause;
  }

  @SuppressWarnings("null")
  @Override
  public Level getSeverity() {
    return severity;
  }

  @SuppressWarnings("null")
  @Override
  public @NonNull URI getDocumentUri() {
    return getNode().getBaseUri();
  }

  @NonNull
  public static Builder builder(@NonNull List<? extends IConstraint> constraints, @NonNull INodeItem node) {
    return new Builder(constraints, node);
  }

  @NonNull
  public static Builder builder(@NonNull IConstraint constraint, @NonNull INodeItem node) {
    return new Builder(CollectionUtil.singletonList(constraint), node);
  }

  public static class Builder {
    @NonNull
    private final List<? extends IConstraint> constraints;
    @NonNull
    private final INodeItem node;
    private CharSequence message;
    private List<? extends INodeItem> targets;
    private Throwable cause;
    private Level severity;

    private Builder(@NonNull List<? extends IConstraint> constraints, @NonNull INodeItem node) {
      this.constraints = constraints;
      this.node = node;
    }
    
    @NonNull
    public Builder message(@NonNull CharSequence message) {
      this.message = message;
      return this;
    }
    
    @NonNull
    public Builder target(@NonNull INodeItem target) {
      this.targets = Collections.singletonList(target);
      return this;
    }
    
    @NonNull
    public Builder targets(@NonNull List<? extends INodeItem> targets) {
      this.targets = CollectionUtil.unmodifiableList(targets);
      return this;
    }
    
    @NonNull
    public Builder cause(@NonNull Throwable cause) {
      this.cause = cause;
      return this;
    }
    
    @NonNull
    public Builder severity(@NonNull Level severity) {
      this.severity = severity;
      return this;
    }

    @NonNull
    public ConstraintValidationFinding build() {
      if (message == null) {
        throw new IllegalStateException("Missing message");
      }
      
      Level severity = this.severity == null ? constraints.stream()
          .map(IConstraint::getLevel)
          .max(Comparator.comparing(Level::ordinal))
          .get() : this.severity;
      
      List<? extends INodeItem> targets = this.targets == null ? CollectionUtil.emptyList() : this.targets;

      assert message != null;
      assert targets != null;
      assert severity != null;
      
      return new ConstraintValidationFinding(
          constraints,
          node,
          message,
          targets,
          severity,
          cause);
    }
  }
}
