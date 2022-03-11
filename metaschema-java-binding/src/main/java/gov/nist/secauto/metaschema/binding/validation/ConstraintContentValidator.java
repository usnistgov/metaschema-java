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

package gov.nist.secauto.metaschema.binding.validation;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.binding.metapath.xdm.IBoundXdmDocumentNodeItem;
import gov.nist.secauto.metaschema.binding.metapath.xdm.IBoundXdmNodeItem;
import gov.nist.secauto.metaschema.model.common.constraint.AbstractFindingCollectingConstraintValidationHandler;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.validation.AbstractContentValidator;
import gov.nist.secauto.metaschema.model.common.validation.IValidationFinding;
import gov.nist.secauto.metaschema.model.common.validation.IValidationResult;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ConstraintContentValidator
    extends AbstractContentValidator {
  @NotNull
  private final IBindingContext bindingContext;

  public ConstraintContentValidator(@NotNull IBindingContext bindingContext) {
    this.bindingContext = Objects.requireNonNull(bindingContext, "bindingContext");
  }

  public IBindingContext getBindingContext() {
    return bindingContext;
  }

  @NotNull
  public IValidationResult validate(@NotNull IBoundXdmNodeItem nodeItem) throws IOException {
    BindingConstraintValidationHandler handler = new BindingConstraintValidationHandler();

    getBindingContext().validate(nodeItem, nodeItem.getBaseUri(), true, handler);

    return null;
  }

  @Override
  public IValidationResult validate(@NotNull InputStream is, @NotNull URI documentUri) throws IOException {
    IBoundXdmDocumentNodeItem nodeItem = getBindingContext().newBoundLoader().loadAsNodeItem(is, documentUri);
    return validate(nodeItem);
  }

  public static class BindingConstraintValidationHandler
      extends AbstractFindingCollectingConstraintValidationHandler {
    @NotNull
    private final List<ConstraintValidationFinding> findings = new LinkedList<>();
    private IConstraint.Level highestLevel = IConstraint.Level.INFORMATIONAL;

    public List<ConstraintValidationFinding> getFindings() {
      return Collections.unmodifiableList(findings);
    }

    public IConstraint.Level getHighestLevel() {
      return highestLevel;
    }

    @Override
    protected void newFinding(
        @NotNull IConstraint constraint,
        @NotNull INodeItem node,
        @NotNull List<? extends INodeItem> targets,
        @NotNull CharSequence message,
        Throwable cause) {
      findings.add(new ConstraintValidationFinding(constraint, message, cause, node, targets));

      if (constraint.getLevel().ordinal() > highestLevel.ordinal()) {
        highestLevel = constraint.getLevel();
      }
    }
  }

  public static class ConstraintValidationFinding implements IValidationFinding {
    @NotNull
    private final IConstraint constraint;
    @NotNull
    private final CharSequence message;
    @NotNull
    private final INodeItem node;
    @NotNull
    private final List<? extends INodeItem> targets;
    private final Throwable cause;

    @SuppressWarnings("null")
    public ConstraintValidationFinding(
        @NotNull IConstraint constraint,
        @NotNull CharSequence message,
        Throwable cause,
        @NotNull INodeItem node,
        @NotNull List<? extends INodeItem> targets) {
      this.constraint = Objects.requireNonNull(constraint, "constraint");
      this.message = Objects.requireNonNull(message, "message");
      this.cause = cause;
      this.node = Objects.requireNonNull(node, "node");
      this.targets = Objects.requireNonNull(targets, "targets");
    }

    public IConstraint getConstraint() {
      return constraint;
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

    @Override
    public IConstraint.Level getSeverity() {
      return getConstraint().getLevel();
    }

    @Override
    public @NotNull URI getDocumentUri() {
      return getNode().getBaseUri();
    }
  }
}
