
package gov.nist.secauto.metaschema.core.metapath.item.node;

import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;

//REFACTOR: Check if this is used, delete?
public interface IAssemblyInstanceGroupedNodeItem
    extends IAssemblyNodeItem {

  @Override
  default String format(IPathFormatter formatter) {
    return formatter.formatAssembly(this);
  }

  @Override
  default IAssemblyInstanceGroupedNodeItem getNodeItem() {
    return this;
  }

  @Override
  default <CONTEXT, RESULT> RESULT accept(INodeItemVisitor<CONTEXT, RESULT> visitor, CONTEXT context) {
    return visitor.visitAssembly(this, context);
  }
}
