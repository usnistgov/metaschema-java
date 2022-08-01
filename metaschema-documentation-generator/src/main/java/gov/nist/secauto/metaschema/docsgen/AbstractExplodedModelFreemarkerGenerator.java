
package gov.nist.secauto.metaschema.docsgen;

import gov.nist.secauto.metaschema.docsgen.explode.ExplosionVisitor;
import gov.nist.secauto.metaschema.docsgen.explode.IAssemblyModelElement;
import gov.nist.secauto.metaschema.freemarker.support.AbstractFreemarkerGenerator;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.StaticContext;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItemFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public abstract class AbstractExplodedModelFreemarkerGenerator
    extends AbstractFreemarkerGenerator {

  @Override
  protected void buildModel(@NonNull Configuration cfg, @NonNull Map<String, Object> root,
      @NonNull IMetaschema metaschema) throws IOException, TemplateException {

    ExplosionVisitor visitor = new ExplosionVisitor();

    INodeItemFactory factory = INodeItemFactory.instance();

    DynamicContext dynamicContext = new StaticContext()
        .newDynamicContext()
        .disablePredicateEvaluation();

    List<? extends IAssemblyModelElement> rootAssemblies = metaschema.getExportedAssemblyDefinitions().stream()
        .filter(modelItem -> modelItem.isRoot())
        .map(rootDefinition -> factory.newAssemblyNodeItem(rootDefinition, metaschema.getLocation()))
        .map(rootItem -> (IAssemblyModelElement) visitor.visit(rootItem, dynamicContext))
        .collect(Collectors.toUnmodifiableList());

    // cfg.setObjectWrapper(new DefaultObjectWrapperBuilder()...build());

    root.put("roots", rootAssemblies);
  }

}
