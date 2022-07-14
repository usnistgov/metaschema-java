package gov.nist.secauto.metaschema.docsgen.explode;

import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFieldNodeItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class FieldModelElementImpl
    extends AbstractModelElement<IFieldNodeItem>
    implements IFieldModelElement {

  protected FieldModelElementImpl(@NotNull IFieldNodeItem nodeItem) {
    super(nodeItem);
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(@NotNull IModelElementVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitField(this, context);
  }

  @Override
  public @NotNull IFieldDefinition getDefinition() {
    return getNodeItem().getDefinition();
  }

  @Override
  public @Nullable IFieldInstance getInstance() {
    return getNodeItem().getInstance();
  }
}
