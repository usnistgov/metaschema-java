
package gov.nist.secauto.metaschema.docsgen.explode;

import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDefinitionNodeItem;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class AbstractModelElement<I extends IDefinitionNodeItem> implements IMutableModelElement {
  @NotNull
  private final I nodeItem;
  @NotNull
  private final List<@NotNull IModelElement> flags = new LinkedList<>();
  @NotNull
  private final List<@NotNull IModelElement> modelItems = new LinkedList<>();
  @NotNull
  private final Set<@NotNull IConstraint> constraints = new LinkedHashSet<>();

  protected AbstractModelElement(@NotNull I nodeItem) {
    this.nodeItem = nodeItem;
  }

  @Override
  public I getNodeItem() {
    return nodeItem;
  }

  @Override
  public List<@NotNull IModelElement> getFlags() {
    return flags;
  }

  @Override
  public List<@NotNull IModelElement> getModelItems() {
    return modelItems;
  }

  @Override
  public void addFlag(@NotNull IModelElement flag) {
    flags.add(flag);
  }

  @Override
  public void addModelItem(@NotNull IModelElement modelItem) {
    modelItems.add(modelItem);
  }

  @Override
  @NotNull
  public Set<@NotNull IConstraint> getConstraints() {
    return constraints;
  }

  @Override
  public void addConstraint(@NotNull IConstraint constraint) {
    if (!this.constraints.contains(constraint)) {
      this.constraints.add(constraint);
    }
  }
}
