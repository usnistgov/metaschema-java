
package gov.nist.secauto.metaschema.docsgen.explode;

import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.StaticContext;
import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.ICycledAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDefinitionNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFieldNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IMetaschemaNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItemVisitable;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItemVisitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExplosionVisitor
    implements INodeItemVisitor<@NotNull IMutableModelElement, ExplosionVisitor.@NotNull Context> {
  private static final Logger LOGGER = LogManager.getLogger(ExplosionVisitor.class);

  @NotNull
  public final IModelElement visit(@NotNull INodeItemVisitable item, @NotNull DynamicContext dynamicContext) {
    return item.accept(this, new Context(dynamicContext));
  }

  protected void visitFlags(@NotNull INodeItem item, @NotNull IMutableModelElement model, @NotNull Context context) {
    for (IFlagNodeItem flag : item.getFlags()) {
      model.addFlag(flag.accept(this, context));
    }
  }

  protected void visitModelChildren(@NotNull INodeItem item, @NotNull IMutableModelElement model,
      @NotNull Context context) {
    for (List<@NotNull ? extends IModelNodeItem> childItems : item.getModelItems()) {
      for (IModelNodeItem childItem : childItems) {
        model.addModelItem(childItem.accept(this, context));
      }
    }
  }

  @Override
  public IMutableModelElement visitDocument(IDocumentNodeItem item, Context context) {
    throw new UnsupportedOperationException();
  }

  @Override
  public FlagModelElementImpl visitFlag(IFlagNodeItem item, Context context) {
    FlagModelElementImpl model = new FlagModelElementImpl(item);
    context.register(model);

    localizeConstraints(item, context);

    return model;
  }

  @Override
  public FieldModelElementImpl visitField(IFieldNodeItem item, Context context) {
    FieldModelElementImpl model = new FieldModelElementImpl(item);
    context.register(model);
    context.push(model);
    
    visitFlags(item, model, context);

    localizeConstraints(item, context);

    context.pop();

    return model;
  }

  @Override
  public AssemblyModelElementImpl visitAssembly(IAssemblyNodeItem item, Context context) {
    AssemblyModelElementImpl model = new AssemblyModelElementImpl(item);
    context.register(model);

    if (!(item instanceof ICycledAssemblyNodeItem)) {
      context.push(model);
  
      visitFlags(item, model, context);
      visitModelChildren(item, model, context);

      localizeConstraints(item, context);

      context.pop();
    }
    return model;
  }

  @Override
  public IMutableModelElement visitMetaschema(@NotNull IMetaschemaNodeItem item, Context context) {
    throw new UnsupportedOperationException();
  }

  protected void localizeConstraints(@NotNull IDefinitionNodeItem item, @NotNull Context context) {
    for (IConstraint constraint : item.getDefinition().getConstraints()) {
      ISequence<?> result = constraint.matchTargets(item, context.getDynamicContext());
      if (result.isEmpty()) {
        throw new IllegalStateException("Constraint doesn't target anything");
      }
      
      result.asStream().forEachOrdered(target -> {
        if (!(target instanceof IDefinitionNodeItem)) {
          throw new IllegalStateException("Constraint doesn't target a node");
        }
        
        IModelElement element = context.getModelElementForNodeItem((IDefinitionNodeItem)target);
        if (element == null) {
          throw new IllegalStateException("Model element doesn't exist.");
        }
        element.addConstraint(constraint);
      });
    }
  }

  class Context {
    private final Deque<IMutableModelElement> lifoStack = new LinkedList<>();
    private final Map<@NotNull IDefinitionNodeItem, IModelElement> nodeItemToElementMap = new HashMap<>();
    @NotNull
    private final DynamicContext dynamicContext;

    public Context() {
      this.dynamicContext = new StaticContext().newDynamicContext().disablePredicateEvaluation();
    }

    public Context(@NotNull DynamicContext dynamicContext) {
      this.dynamicContext = dynamicContext;
    }

    @NotNull
    public DynamicContext getDynamicContext() {
      return dynamicContext;
    }

    public IMutableModelElement pop() {
      return lifoStack.pop();

    }

    public void push(IMutableModelElement element) {
      lifoStack.push(element);
    }

    public void register(@NotNull IModelElement element) {
      nodeItemToElementMap.put(element.getNodeItem(), element);
    }

    @Nullable
    public IModelElement getModelElementForNodeItem(@NotNull IDefinitionNodeItem nodeItem) {
      return nodeItemToElementMap.get(nodeItem);
    }
  }
}
