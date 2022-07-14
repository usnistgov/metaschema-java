package gov.nist.secauto.metaschema.docsgen.explode;

import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class FlagModelElementImpl
    extends AbstractModelElement<IFlagNodeItem>
    implements IFlagModelElement {

  protected FlagModelElementImpl(@NotNull IFlagNodeItem nodeItem) {
    super(nodeItem);
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(@NotNull IModelElementVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitFlag(this, context);
  }

  @Override
  public @NotNull IFlagDefinition getDefinition() {
    return getNodeItem().getDefinition();
  }

  @Override
  public @Nullable IFlagInstance getInstance() {
    return getNodeItem().getInstance();
  }
}
