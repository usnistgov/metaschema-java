package gov.nist.secauto.metaschema.docsgen.explode;

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class AssemblyModelElementImpl
    extends AbstractModelElement<IAssemblyNodeItem>
    implements IAssemblyModelElement {

  protected AssemblyModelElementImpl(@NonNull IAssemblyNodeItem nodeItem) {
    super(nodeItem);
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(@NonNull IModelElementVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitAssembly(this, context);
  }

  @Override
  public @NonNull IAssemblyDefinition getDefinition() {
    return getNodeItem().getDefinition();
  }

  @Override
  public @Nullable IAssemblyInstance getInstance() {
    return getNodeItem().getInstance();
  }
}
