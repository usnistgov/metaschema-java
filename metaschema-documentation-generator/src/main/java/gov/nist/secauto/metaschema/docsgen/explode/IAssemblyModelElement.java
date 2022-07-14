package gov.nist.secauto.metaschema.docsgen.explode;

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;

public interface IAssemblyModelElement extends IModelElement {
  @Override
  IAssemblyNodeItem getNodeItem();
  
  @Override
  IAssemblyDefinition getDefinition();
  
  @Override
  IAssemblyInstance getInstance();
}
