
package gov.nist.secauto.metaschema.docsgen.explode;

import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDefinitionNodeItem;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbstractModelElement<I extends IDefinitionNodeItem> implements IMutableModelElement {
  @NonNull
  private final I nodeItem;
  @NonNull
  private final List<IModelElement> flags = new LinkedList<>();
  @NonNull
  private final List<IModelElement> modelItems = new LinkedList<>();
  @NonNull
  private final Set<IConstraint> constraints = new LinkedHashSet<>();

  protected AbstractModelElement(@NonNull I nodeItem) {
    this.nodeItem = nodeItem;
  }

  @Override
  public I getNodeItem() {
    return nodeItem;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "this is a data holder")
  public List<IModelElement> getFlags() {
    return flags;
  }

  @Override
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "this is a data holder")
  public List<IModelElement> getModelItems() {
    return modelItems;
  }

  @Override
  public void addFlag(@NonNull IModelElement flag) {
    flags.add(flag);
  }

  @Override
  public void addModelItem(@NonNull IModelElement modelItem) {
    modelItems.add(modelItem);
  }

  @Override
  @NonNull
  @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "this is a data holder")
  public Set<IConstraint> getConstraints() {
    return constraints;
  }

  @Override
  public void addConstraint(@NonNull IConstraint constraint) {
    if (!this.constraints.contains(constraint)) {
      this.constraints.add(constraint);
    }
  }
}
