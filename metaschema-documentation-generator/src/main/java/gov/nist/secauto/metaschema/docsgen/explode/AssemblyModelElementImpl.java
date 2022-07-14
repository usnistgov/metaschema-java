package gov.nist.secauto.metaschema.docsgen.explode;

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class AssemblyModelElementImpl
    extends AbstractModelElement<IAssemblyNodeItem>
    implements IAssemblyModelElement {

  protected AssemblyModelElementImpl(@NotNull IAssemblyNodeItem nodeItem) {
    super(nodeItem);
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(@NotNull IModelElementVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitAssembly(this, context);
  }

  @Override
  public @NotNull IAssemblyDefinition getDefinition() {
    return getNodeItem().getDefinition();
  }

  @Override
  public @Nullable IAssemblyInstance getInstance() {
    return getNodeItem().getInstance();
  }
}
