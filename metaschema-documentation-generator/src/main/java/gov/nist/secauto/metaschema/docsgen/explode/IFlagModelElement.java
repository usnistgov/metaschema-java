package gov.nist.secauto.metaschema.docsgen.explode;

import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;

public interface IFlagModelElement extends IModelElement {
  @Override
  IFlagNodeItem getNodeItem();
  
  @Override
  IFlagDefinition getDefinition();
  
  @Override
  IFlagInstance getInstance();
}
