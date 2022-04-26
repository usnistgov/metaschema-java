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

package gov.nist.secauto.metaschema.binding.metapath.item;

import gov.nist.secauto.metaschema.binding.IBindingContext;
import gov.nist.secauto.metaschema.model.common.constraint.AbstractFindingCollectingConstraintValidationHandler;
import gov.nist.secauto.metaschema.model.common.constraint.DefaultConstraintValidator;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.Level;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.StaticContext;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;
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

/**
 * A content validator that enforces Metaschema model constraints.
 */
// TODO: consider moving this to model if we can find a way to eliminate the need for the binding
// context
public class ConstraintContentValidator
    extends AbstractContentValidator {
  @NotNull
  private final IBindingContext bindingContext;

  public ConstraintContentValidator(@NotNull IBindingContext bindingContext) {
    this.bindingContext = ObjectUtils.requireNonNull(bindingContext, "bindingContext");
  }

  @NotNull
  protected IBindingContext getBindingContext() {
    return bindingContext;
  }

  @Override
  public IValidationResult validate(@NotNull InputStream is, @NotNull URI documentUri) throws IOException {
    IDocumentNodeItem nodeItem = getBindingContext().newBoundLoader().loadAsNodeItem(is, documentUri);
    return validate(nodeItem);
  }

  @NotNull
  public IValidationResult validate(@NotNull INodeItem nodeItem) {
    StaticContext staticContext = new StaticContext();
    DynamicContext dynamicContext = staticContext.newDynamicContext();
    dynamicContext.setDocumentLoader(getBindingContext().newBoundLoader());
    DefaultConstraintValidator validator = new DefaultConstraintValidator(dynamicContext);

    BindingConstraintValidationHandler result = new BindingConstraintValidationHandler();
    validator.setConstraintValidationHandler(result);

    nodeItem.validate(validator);
    validator.finalizeValidation();

    return result;
  }

  public static class BindingConstraintValidationHandler
      extends AbstractFindingCollectingConstraintValidationHandler
      implements IValidationResult {
    @NotNull
    private final List<ConstraintValidationFinding> findings = new LinkedList<>();
    @NotNull
    private Level highestLevel = IConstraint.Level.INFORMATIONAL;

    @Override
    public List<ConstraintValidationFinding> getFindings() {
      return ObjectUtils.notNull(Collections.unmodifiableList(findings));
    }

    @Override
    public @NotNull Level getHighestSeverity() {
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

  public static class ConstraintValidationFinding implements IValidationFinding { // NOPMD - intentional
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
