
package gov.nist.secauto.metaschema.docsgen.explode;

import gov.nist.secauto.metaschema.docsgen.explode.IModelElementVisitor.IModelElementVisitable;
import gov.nist.secauto.metaschema.model.common.IDefinition;
import gov.nist.secauto.metaschema.model.common.INamedInstance;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDefinitionNodeItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface IModelElement extends IModelElementVisitable {
  @NotNull
  IDefinitionNodeItem getNodeItem();

  @NotNull
  List<@NotNull IModelElement> getFlags();

  @NotNull
  List<@NotNull IModelElement> getModelItems();

  void addConstraint(@NotNull IConstraint constraint);

  @NotNull
  Set<@NotNull IConstraint> getConstraints();

  @Nullable
  INamedInstance getInstance();

  @NotNull
  IDefinition getDefinition();
}
