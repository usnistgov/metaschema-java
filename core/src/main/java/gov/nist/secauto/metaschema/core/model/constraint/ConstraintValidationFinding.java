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

import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.model.IResourceLocation;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraint.Level;
import gov.nist.secauto.metaschema.core.model.validation.IValidationFinding;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.net.URI;
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
  @Nullable
  private final String message;
  @NonNull
  private final INodeItem node;
  @NonNull
  private final INodeItem target;
  @NonNull
  private final List<? extends INodeItem> subjects;
  private final Throwable cause;
  @NonNull
  private final Kind kind;
  @NonNull
  private final Level severity;

  private ConstraintValidationFinding(
      @NonNull List<? extends IConstraint> constraints,
      @NonNull INodeItem node,
      @Nullable String message,
      @NonNull INodeItem target,
      @NonNull List<? extends INodeItem> subjects,
      @NonNull Kind kind,
      @NonNull Level severity,
      @Nullable Throwable cause) {
    this.constraints = constraints;
    this.node = node;
    this.message = message;
    this.target = target;
    this.subjects = subjects;
    this.kind = kind;
    this.severity = severity;
    this.cause = cause;
  }

  @Override
  public String getIdentifier() {
    return constraints.size() == 1 ? constraints.get(0).getId() : null;
  }

  @NonNull
  public List<? extends IConstraint> getConstraints() {
    return constraints;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @NonNull
  public INodeItem getNode() {
    return node;
  }

  @NonNull
  public INodeItem getTarget() {
    return target;
  }

  @NonNull
  public List<? extends INodeItem> getSubjects() {
    return subjects;
  }

  @Override
  public IResourceLocation getLocation() {
    // first try the target
    INodeItem node = getTarget();
    IResourceLocation retval = node.getLocation();
    if (retval == null) {
      // if no location, try the parent
      node = node.getParentContentNodeItem();
      if (node != null) {
        retval = node.getLocation();
      }
    }
    return retval;
  }

  @Override
  public String getPathKind() {
    return "metapath";
  }

  @Override
  public String getPath() {
    return getTarget().getMetapath();
  }

  @Override
  public Throwable getCause() {
    return cause;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public Level getSeverity() {
    return severity;
  }

  @Override
  public URI getDocumentUri() {
    return getTarget().getBaseUri();
  }

  @NonNull
  public static Builder builder(@NonNull List<? extends IConstraint> constraints, @NonNull INodeItem node) {
    return new Builder(constraints, node);
  }

  @NonNull
  public static Builder builder(@NonNull IConstraint constraint, @NonNull INodeItem node) {
    return new Builder(CollectionUtil.singletonList(constraint), node);
  }

  public static final class Builder {
    @NonNull
    private final List<? extends IConstraint> constraints;
    @NonNull
    private final INodeItem node;
    @NonNull
    private INodeItem target;
    private String message;
    private List<? extends INodeItem> subjects;
    private Throwable cause;
    private Kind kind;
    private Level severity;

    private Builder(@NonNull List<? extends IConstraint> constraints, @NonNull INodeItem node) {
      this.constraints = constraints;
      this.node = node;
      this.target = node;
    }

    public Builder target(@NonNull INodeItem target) {
      this.target = target;
      return this;
    }

    @NonNull
    public Builder message(@NonNull String message) {
      this.message = message;
      return this;
    }

    @NonNull
    public Builder subjects(@NonNull List<? extends INodeItem> targets) {
      this.subjects = CollectionUtil.unmodifiableList(targets);
      return this;
    }

    @NonNull
    public Builder cause(@NonNull Throwable cause) {
      this.cause = cause;
      return this;
    }

    @NonNull
    public Builder kind(@NonNull Kind kind) {
      this.kind = kind;
      return this;
    }

    @NonNull
    public Builder severity(@NonNull Level severity) {
      this.severity = severity;
      return this;
    }

    @NonNull
    public ConstraintValidationFinding build() {
      Level severity = ObjectUtils.notNull(this.severity == null ? constraints.stream()
          .map(IConstraint::getLevel)
          .max(Comparator.comparing(Level::ordinal))
          .get() : this.severity);

      List<? extends INodeItem> subjects = this.subjects == null ? CollectionUtil.emptyList() : this.subjects;

      assert subjects != null;
      assert kind != null;

      return new ConstraintValidationFinding(
          constraints,
          node,
          message,
          target,
          subjects,
          kind,
          severity,
          cause);
    }
  }
}
