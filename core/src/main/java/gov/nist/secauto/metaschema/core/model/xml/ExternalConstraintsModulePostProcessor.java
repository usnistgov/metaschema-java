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

package gov.nist.secauto.metaschema.core.model.xml;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression.ResultType;
import gov.nist.secauto.metaschema.core.metapath.StaticContext;
import gov.nist.secauto.metaschema.core.metapath.item.IItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDefinitionNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModuleNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItemFactory;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.IModuleLoader;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.ITargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.impl.ConstraintComposingVisitor;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A module loading post processor that integrates applicable external
 * constraints into a given module when loaded.
 *
 * @see ModuleLoader#ModuleLoader(List)
 */
public class ExternalConstraintsModulePostProcessor implements IModuleLoader.IModulePostProcessor {
  private static final Logger LOGGER = LogManager.getLogger(ExternalConstraintsModulePostProcessor.class);
  @NonNull
  private final List<IConstraintSet> registeredConstraintSets;

  /**
   * Create a new post processor.
   *
   * @param additionalConstraintSets
   *          the external constraint sets to apply
   */
  public ExternalConstraintsModulePostProcessor(@NonNull Collection<IConstraintSet> additionalConstraintSets) {
    this.registeredConstraintSets = ObjectUtils.notNull(additionalConstraintSets.stream()
        .flatMap(set -> Stream.concat(
            Stream.of(set),
            set.getImportedConstraintSets().stream()))
        .distinct()
        .collect(Collectors.toUnmodifiableList()));
  }

  /**
   * Get the external constraint sets associated with this post processor.
   *
   * @return the list of constraint sets
   */
  protected List<IConstraintSet> getRegisteredConstraintSets() {
    return registeredConstraintSets;
  }

  @Override
  public void processModule(IModule module) {
    ConstraintComposingVisitor visitor = new ConstraintComposingVisitor();
    IModuleNodeItem moduleItem = INodeItemFactory.instance().newModuleNodeItem(module);

    StaticContext staticContext = StaticContext.builder()
        .defaultModelNamespace(module.getXmlNamespace())
        .build();
    DynamicContext dynamicContext = new DynamicContext(staticContext);

    for (IConstraintSet set : getRegisteredConstraintSets()) {
      assert set != null;
      applyConstraints(module, moduleItem, set, visitor, dynamicContext);
    }
  }

  private static void applyConstraints(
      @NonNull IModule module,
      @NonNull IModuleNodeItem moduleItem,
      @NonNull IConstraintSet set,
      @NonNull ConstraintComposingVisitor visitor,
      @NonNull DynamicContext dynamicContext) {
    for (ITargetedConstraints targeted : set.getTargetedConstraintsForModule(module)) {
      // apply targeted constraints
      String targetExpression = targeted.getTargetExpression();
      MetapathExpression metapath = MetapathExpression.compile(targetExpression, dynamicContext.getStaticContext());
      ISequence<?> items = metapath.evaluateAs(moduleItem, ResultType.SEQUENCE, dynamicContext);
      assert items != null;

      for (IItem item : items) {
        if (item instanceof IDefinitionNodeItem) {
          ((IDefinitionNodeItem<?, ?>) item).accept(visitor, targeted);
        } else {
          // log error
          if (LOGGER.isErrorEnabled()) {
            LOGGER.atError().log(
                "Found non-definition item '{}' while applying external constraints using target expression '{}'.",
                item.toString(),
                targetExpression);
          }
        }
      }
    }
  }
}
