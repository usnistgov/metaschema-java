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

import gov.nist.secauto.metaschema.model.common.IDefinition;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.MetaschemaException;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression.ResultType;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IDefinitionNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IMetaschemaNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.INodeItemFactory;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

public interface IConstraintSet {

  @NonNull
  static Set<IConstraintSet> resolveConstraintSets(@NonNull Set<IConstraintSet> constraintSets) {
    return ObjectUtils.notNull(constraintSets.stream()
        .flatMap(set -> resolveConstraintSet(ObjectUtils.notNull(set)))
        .distinct()
        .collect(Collectors.toUnmodifiableSet()));
  }

  @NonNull
  private static Stream<IConstraintSet> resolveConstraintSet(@NonNull IConstraintSet constraintSet) {
    return ObjectUtils.notNull(Stream.concat(
        Stream.of(constraintSet),
        constraintSet.getImportedConstraintSets().stream()));
  }

  @NonNull
  static List<ITargetedConstaints> getTargetedConstraintsForMetaschema(
      @NonNull Set<IConstraintSet> constraintSets, @NonNull IMetaschema metaschema) {
    return ObjectUtils.notNull(resolveConstraintSets(constraintSets).stream()
        .flatMap(set -> set.getTargetedConstraintsForMetaschema(metaschema))
        .collect(Collectors.toUnmodifiableList()));
  }

  @NonNull
  Stream<ITargetedConstaints> getTargetedConstraintsForMetaschema(@NonNull IMetaschema metaschema);

  static void applyConstraintSetToMetaschema(@NonNull Set<IConstraintSet> constraintSets,
      @NonNull IMetaschema metaschema) throws MetaschemaException {
    Set<IConstraintSet> resolvedConstraintSets = resolveConstraintSets(constraintSets);

    ConstraintComposingVisitor visitor = new ConstraintComposingVisitor();
    IMetaschemaNodeItem item = INodeItemFactory.instance().newMetaschemaNodeItem(metaschema);

    for (ITargetedConstaints targeted : getTargetedConstraintsForMetaschema(resolvedConstraintSets, metaschema)) {
      MetapathExpression targetExpression = targeted.getTargetExpression();
      INodeItem node = targetExpression.evaluateAs(item, ResultType.NODE);
      if (node == null) {
        throw new MetaschemaException(String.format("Target not found for expression '%s' on metaschema '%s'.",
            targetExpression.getPath(),
            metaschema.getQName()));
      } else if (node instanceof IDefinitionNodeItem) {
        IDefinition nodeDefinition = ((IDefinitionNodeItem<?, ?>) node).getDefinition();
        IMetaschema nodeMetaschema = nodeDefinition.getContainingMetaschema();
        if (!metaschema.equals(nodeMetaschema)) {
          throw new MetaschemaException(
              String.format("Target definition '%s' in metaschema '%s' is not in the scoped metaschema '%s'.",
                  nodeDefinition.getName(),
                  nodeMetaschema.getQName(),
                  metaschema.getQName()));
        }
      }
      node.accept(visitor, targeted);
    }

  }

  Collection<IConstraintSet> getImportedConstraintSets();

  /**
   * Get the set of Metaschema scoped constraints to apply by a {@link QName} formed from the
   * Metaschema namespace and short name.
   *
   * @return the mapping of QName to scoped constraints
   */
  @NonNull
  Map<QName, List<IScopedContraints>> getScopedContraints();
}
