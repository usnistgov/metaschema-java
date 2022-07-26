package gov.nist.secauto.metaschema.docsgen.explode;

import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFieldNodeItem;

import edu.umd.cs.findbugs.annotations.NonNull;

class FieldModelElementImpl
    extends AbstractModelElement<IFieldNodeItem>
    implements IFieldModelElement {

  protected FieldModelElementImpl(@NonNull IFieldNodeItem nodeItem) {
    super(nodeItem);
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(@NonNull IModelElementVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitField(this, context);
  }

  @Override
  public IFieldDefinition getDefinition() {
    return getNodeItem().getDefinition();
  }

  @Override
  public IFieldInstance getInstance() {
    return getNodeItem().getInstance();
  }
}
