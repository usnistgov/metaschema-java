package gov.nist.secauto.metaschema.docsgen.explode;

import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFieldNodeItem;

public interface IFieldModelElement extends IModelElement {
  @Override
  IFieldNodeItem getNodeItem();
  
  @Override
  IFieldDefinition getDefinition();
  
  @Override
  IFieldInstance getInstance();
}
