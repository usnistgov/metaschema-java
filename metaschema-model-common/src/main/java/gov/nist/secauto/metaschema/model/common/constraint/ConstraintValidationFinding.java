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

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

/**
 * Represents an individual constraint validation issue.
 */
public class ConstraintValidationFinding implements IValidationFinding { // NOPMD - intentional
  @NotNull
  private final List<@NotNull ? extends IConstraint> constraints;
  @NotNull
  private final CharSequence message;
  @NotNull
  private final INodeItem node;
  @NotNull
  private final List<@NotNull ? extends INodeItem> targets;
  private final Throwable cause;

  public ConstraintValidationFinding(
      @NotNull IConstraint constraint,
      @NotNull CharSequence message,
      Throwable cause,
      @NotNull INodeItem node,
      @NotNull List<@NotNull ? extends INodeItem> targets) {
    this(
        CollectionUtil.singletonList(constraint),
        message,
        cause,
        node,
        targets);
  }

  public ConstraintValidationFinding(
      @NotNull List<@NotNull ? extends IConstraint> constraints,
      @NotNull CharSequence message,
      Throwable cause,
      @NotNull INodeItem node,
      @NotNull List<@NotNull ? extends INodeItem> targets) {
    this.constraints = constraints;
    this.message = message;
    this.cause = cause;
    this.node = node;
    this.targets = targets;
  }

  public List<@NotNull ? extends IConstraint> getConstraints() {
    return constraints;
  }

  @Override
  public CharSequence getMessage() {
    return message;
  }

  public INodeItem getNode() {
    return node;
  }

  public List<@NotNull ? extends INodeItem> getTargets() {
    return targets;
  }

  @Override
  public Throwable getCause() {
    return cause;
  }

  @SuppressWarnings("null")
  @Override
  public Level getSeverity() {
    return getConstraints().stream()
        .map(IConstraint::getLevel)
        .max(Comparator.comparing(Level::ordinal))
        .get();
  }

  @SuppressWarnings("null")
  @Override
  public @NotNull URI getDocumentUri() {
    return getNode().getBaseUri();
  }
}
