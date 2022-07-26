
package gov.nist.secauto.metaschema.docsgen.explode;

import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;

import edu.umd.cs.findbugs.annotations.NonNull;

class FlagModelElementImpl
    extends AbstractModelElement<IFlagNodeItem>
    implements IFlagModelElement {

  protected FlagModelElementImpl(@NonNull IFlagNodeItem nodeItem) {
    super(nodeItem);
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(@NonNull IModelElementVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitFlag(this, context);
  }

  @Override
  public IFlagDefinition getDefinition() {
    return getNodeItem().getDefinition();
  }

  @Override
  public IFlagInstance getInstance() {
    return getNodeItem().getInstance();
  }
}
