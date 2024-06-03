/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.metaschema.core.model.xml;

import gov.nist.secauto.metaschema.core.model.AbstractLoader;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IConstraintLoader;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.constraint.AbstractTargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.AssemblyConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.IFeatureModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.ITargetedConstraints;
import gov.nist.secauto.metaschema.core.model.xml.impl.ConstraintXmlSupport;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.MetaschemaMetaConstraintsDocument;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.MetaschemaMetapathReferenceType;
import gov.nist.secauto.metaschema.core.model.xml.xmlbeans.ModelContextType;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public class XmlMetaConstraintLoader
    extends AbstractLoader<IConstraintSet>
    implements IConstraintLoader {

  @Override
  protected IConstraintSet parseResource(URI resource, Deque<URI> visitedResources) throws IOException {

    ISource source = ISource.externalSource(resource);

    // parse this metaschema
    MetaschemaMetaConstraintsDocument xmlObject = parseConstraintSet(resource);

    MetaschemaMetaConstraintsDocument.MetaschemaMetaConstraints constraints = xmlObject.getMetaschemaMetaConstraints();

    List<ITargetedConstraints> targetedConstraints = ObjectUtils.notNull(constraints.getContextList().stream()
        .flatMap(context -> parseContext(ObjectUtils.notNull(context), null, source).getTargetedConstraints().stream())
        .collect(Collectors.toList()));
    return new MetaConstraintSet(targetedConstraints);
  }

  private Context parseContext(
      @NonNull ModelContextType contextObj,
      @Nullable Context parent,
      @NonNull ISource source) {

    List<String> metapaths;
    if (parent == null) {
      metapaths = ObjectUtils.notNull(contextObj.getMetapathList().stream()
          .map(MetaschemaMetapathReferenceType::getTarget)
          .collect(Collectors.toList()));
    } else {
      List<String> parentMetapaths = parent.getMetapaths().stream()
          .collect(Collectors.toList());
      metapaths = ObjectUtils.notNull(contextObj.getMetapathList().stream()
          .map(MetaschemaMetapathReferenceType::getTarget)
          .flatMap(childPath -> {
            return parentMetapaths.stream()
                .map(parentPath -> parentPath + '/' + childPath);
          })
          .collect(Collectors.toList()));
    }

    IModelConstrained constraints = new AssemblyConstraintSet();
    ConstraintXmlSupport.parse(constraints, ObjectUtils.notNull(contextObj.getConstraints()), source);
    Context context = new Context(metapaths, constraints);

    List<Context> childContexts = contextObj.getContextList().stream()
        .map(childObj -> parseContext(ObjectUtils.notNull(childObj), context, source))
        .collect(Collectors.toList());

    context.addAll(childContexts);

    return context;
  }

  /**
   * Parse the provided XML resource as a Metaschema constraints.
   *
   * @param resource
   *          the resource to parse
   * @return the XMLBeans representation of the Metaschema contraints
   * @throws IOException
   *           if a parsing error occurred
   */
  @NonNull
  protected MetaschemaMetaConstraintsDocument parseConstraintSet(@NonNull URI resource) throws IOException {
    try {
      XmlOptions options = new XmlOptions();
      options.setBaseURI(resource);
      options.setLoadLineNumbers();
      return ObjectUtils.notNull(MetaschemaMetaConstraintsDocument.Factory.parse(resource.toURL(),
          options));
    } catch (XmlException ex) {
      throw new IOException(ex);
    }
  }

  private static class Context {
    @NonNull
    private final List<String> metapaths;
    @NonNull
    private final IModelConstrained constraints;
    @NonNull
    private final List<Context> childContexts = new LinkedList<>();

    public Context(
        @NonNull List<String> metapaths,
        @NonNull IModelConstrained constraints) {
      this.metapaths = metapaths;
      this.constraints = constraints;
    }

    public List<ITargetedConstraints> getTargetedConstraints() {
      return Stream.concat(
          getMetapaths().stream()
              .map(metapath -> new MetaTargetedContraints(ObjectUtils.notNull(metapath), constraints)),
          childContexts.stream()
              .flatMap(child -> child.getTargetedConstraints().stream()))
          .collect(Collectors.toList());
    }

    public void addAll(Collection<Context> childContexts) {
      childContexts.addAll(childContexts);
    }

    public List<String> getMetapaths() {
      return metapaths;
    }
  }

  private static class MetaTargetedContraints
      extends AbstractTargetedConstraints<IModelConstrained>
      implements IFeatureModelConstrained {

    protected MetaTargetedContraints(
        @NonNull String target,
        @NonNull IModelConstrained constraints) {
      super(target, constraints);
    }

    /**
     * Apply the constraints to the provided {@code definition}.
     * <p>
     * This will be called when a definition is found that matches the target
     * expression.
     *
     * @param definition
     *          the definition to apply the constraints to.
     */
    protected void applyTo(@NonNull IDefinition definition) {
      getAllowedValuesConstraints().forEach(definition::addConstraint);
      getMatchesConstraints().forEach(definition::addConstraint);
      getIndexHasKeyConstraints().forEach(definition::addConstraint);
      getExpectConstraints().forEach(definition::addConstraint);
    }

    protected void applyTo(@NonNull IAssemblyDefinition definition) {
      applyTo((IDefinition) definition);
      getIndexConstraints().forEach(definition::addConstraint);
      getUniqueConstraints().forEach(definition::addConstraint);
      getHasCardinalityConstraints().forEach(definition::addConstraint);
    }

    @Override
    public void target(IFlagDefinition definition) {
      applyTo(definition);
    }

    @Override
    public void target(IFieldDefinition definition) {
      applyTo(definition);
    }

    @Override
    public void target(IAssemblyDefinition definition) {
      applyTo(definition);
    }
  }

  private static final class MetaConstraintSet implements IConstraintSet {
    @NonNull
    private final List<ITargetedConstraints> targetedConstraints;

    private MetaConstraintSet(@NonNull List<ITargetedConstraints> targetedConstraints) {
      this.targetedConstraints = targetedConstraints;
    }

    @Override
    public Iterable<ITargetedConstraints> getTargetedConstraintsForModule(IModule module) {
      return targetedConstraints;
    }

    @Override
    public Collection<IConstraintSet> getImportedConstraintSets() {
      return CollectionUtil.emptyList();
    }

  }
}
