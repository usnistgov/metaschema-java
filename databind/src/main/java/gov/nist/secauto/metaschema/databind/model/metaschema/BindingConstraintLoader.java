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

package gov.nist.secauto.metaschema.databind.model.metaschema;

import gov.nist.secauto.metaschema.core.metapath.MetapathException;
import gov.nist.secauto.metaschema.core.model.AbstractLoader;
import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IConstraintLoader;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.MetaschemaException;
import gov.nist.secauto.metaschema.core.model.constraint.AbstractTargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.AssemblyConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.AssemblyTargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.DefaultScopedContraints;
import gov.nist.secauto.metaschema.core.model.constraint.FieldTargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.FlagTargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.IConstraintSet;
import gov.nist.secauto.metaschema.core.model.constraint.IFeatureModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.IModelConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.IScopedContraints;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.ITargetedConstraints;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ValueConstraintSet;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.IBindingContext;
import gov.nist.secauto.metaschema.databind.io.DeserializationFeature;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;
import gov.nist.secauto.metaschema.databind.model.binding.metaschema.AssemblyConstraints;
import gov.nist.secauto.metaschema.databind.model.binding.metaschema.MetapathContext;
import gov.nist.secauto.metaschema.databind.model.binding.metaschema.MetaschemaMetaConstraints;
import gov.nist.secauto.metaschema.databind.model.binding.metaschema.MetaschemaMetapath;
import gov.nist.secauto.metaschema.databind.model.binding.metaschema.MetaschemaModuleConstraints;
import gov.nist.secauto.metaschema.databind.model.metaschema.impl.ConstraintBindingSupport;

import org.apache.xmlbeans.impl.values.XmlValueNotSupportedException;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

/**
 * Provides methods to load a constraint set expressed in XML.
 * <p>
 * Loaded constraint instances are cached to avoid the need to load them for
 * every use. Any constraint set imported is also loaded and cached
 * automatically.
 */
public class BindingConstraintLoader
    extends AbstractLoader<IConstraintSet>
    implements IConstraintLoader {

  @NonNull
  private final IBoundLoader loader;

  public BindingConstraintLoader() {
    // ensure the bindings are registered
    IBindingContext.instance().registerBindingMatcher(MetaschemaMetaConstraints.class);
    IBindingContext.instance().registerBindingMatcher(MetaschemaModuleConstraints.class);

    this.loader = IBindingContext.instance().newBoundLoader();
    this.loader.enableFeature(DeserializationFeature.DESERIALIZE_VALIDATE_CONSTRAINTS);
  }

  @Override
  protected IConstraintSet parseResource(@NonNull URI resource, @NonNull Deque<URI> visitedResources)
      throws IOException {

    Object constraintsDocument = loader.load(resource);

    ISource source = ISource.externalSource(resource);

    IConstraintSet retval;
    if (constraintsDocument instanceof MetaschemaModuleConstraints) {
      MetaschemaModuleConstraints obj = (MetaschemaModuleConstraints) constraintsDocument;

      // now check if this constraint set imports other constraint sets
      List<MetaschemaModuleConstraints.Import> imports = CollectionUtil.listOrEmpty(obj.getImports());

      @NonNull Map<URI, IConstraintSet> importedConstraints;
      if (imports.isEmpty()) {
        importedConstraints = ObjectUtils.notNull(Collections.emptyMap());
      } else {
        try {
          importedConstraints = new LinkedHashMap<>();
          for (MetaschemaModuleConstraints.Import imported : imports) {
            URI importedResource = imported.getHref();
            importedResource = ObjectUtils.notNull(resource.resolve(importedResource));
            importedConstraints.put(importedResource, loadInternal(importedResource, visitedResources));
          }
        } catch (MetaschemaException ex) {
          throw new IOException(ex);
        }
      }

      // now create this constraint set
      retval = new DefaultConstraintSet(
          resource,
          parseScopedConstraints(obj, source),
          new LinkedHashSet<>(importedConstraints.values()));
    } else if (constraintsDocument instanceof MetaschemaMetaConstraints) {
      MetaschemaMetaConstraints obj = (MetaschemaMetaConstraints) constraintsDocument;

      List<ITargetedConstraints> targetedConstraints = CollectionUtil.listOrEmpty(obj.getContexts()).stream()
          .flatMap(context -> parseContext(ObjectUtils.notNull(context), null, source)
              .getTargetedConstraints().stream())
          .collect(Collectors.toList());
      retval = new MetaConstraintSet(targetedConstraints);
    } else {
      throw new UnsupportedOperationException(String.format("Unsupported constraint content '%s'.", resource));
    }
    return retval;
  }

  /**
   * Parse individual constraint definitions from the provided XMLBeans object.
   *
   * @param obj
   *          the XMLBeans object
   * @param source
   *          the source of the constraint content
   * @return the scoped constraint definitions
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // intentional
  @NonNull
  protected List<IScopedContraints> parseScopedConstraints(
      @NonNull MetaschemaModuleConstraints obj,
      @NonNull ISource source) {
    List<IScopedContraints> scopedConstraints = new LinkedList<>();

    for (MetaschemaModuleConstraints.Scope scope : CollectionUtil.listOrEmpty(obj.getScopes())) {
      assert scope != null;

      List<ITargetedConstraints> targetedConstraints = new LinkedList<>();
      try {
        for (IValueConstraintsBase constraintsObj : CollectionUtil.listOrEmpty(scope.getConstraints())) {
          if (constraintsObj instanceof MetaschemaModuleConstraints.Scope.Assembly) {
            targetedConstraints.add(handleScopedAssembly(
                (MetaschemaModuleConstraints.Scope.Assembly) constraintsObj,
                source));
          } else if (constraintsObj instanceof MetaschemaModuleConstraints.Scope.Field) {
            targetedConstraints.add(handleScopedField(
                (MetaschemaModuleConstraints.Scope.Field) constraintsObj,
                source));
          } else if (constraintsObj instanceof MetaschemaModuleConstraints.Scope.Flag) {
            targetedConstraints.add(handleScopedFlag(
                (MetaschemaModuleConstraints.Scope.Flag) constraintsObj,
                source));
          }
        }
      } catch (MetapathException | XmlValueNotSupportedException ex) {
        if (ex.getCause() instanceof MetapathException) {
          throw new MetapathException(
              String.format("Unable to compile a Metapath in '%s'. %s",
                  source.getSource(),
                  ex.getLocalizedMessage()),
              ex);
        }
        throw ex;
      }

      URI namespace = ObjectUtils.requireNonNull(scope.getMetaschemaNamespace());
      String shortName = ObjectUtils.requireNonNull(scope.getMetaschemaShortName());

      scopedConstraints.add(new DefaultScopedContraints(
          namespace,
          shortName,
          CollectionUtil.unmodifiableList(targetedConstraints)));
    }
    return CollectionUtil.unmodifiableList(scopedConstraints);
  }

  private static AssemblyTargetedConstraints handleScopedAssembly(
      @NonNull MetaschemaModuleConstraints.Scope.Assembly obj,
      @NonNull ISource source) {
    IModelConstrained constraints = new AssemblyConstraintSet();
    ConstraintBindingSupport.parse(constraints, obj, source);
    return new AssemblyTargetedConstraints(
        ObjectUtils.requireNonNull(obj.getTarget()),
        constraints);
  }

  private static FieldTargetedConstraints handleScopedField(
      @NonNull MetaschemaModuleConstraints.Scope.Field obj,
      @NonNull ISource source) {
    IValueConstrained constraints = new ValueConstraintSet();
    ConstraintBindingSupport.parse(constraints, obj, source);

    return new FieldTargetedConstraints(
        ObjectUtils.requireNonNull(obj.getTarget()),
        constraints);
  }

  private static FlagTargetedConstraints handleScopedFlag(
      @NonNull MetaschemaModuleConstraints.Scope.Flag obj,
      @NonNull ISource source) {
    IValueConstrained constraints = new ValueConstraintSet();
    ConstraintBindingSupport.parse(constraints, obj, source);

    return new FlagTargetedConstraints(
        ObjectUtils.requireNonNull(obj.getTarget()),
        constraints);
  }

  private Context parseContext(
      @NonNull MetapathContext contextObj,
      @Nullable Context parent,
      @NonNull ISource source) {

    List<String> metapaths;
    if (parent == null) {
      metapaths = CollectionUtil.listOrEmpty(contextObj.getMetapaths()).stream()
          .map(MetaschemaMetapath::getTarget)
          .collect(Collectors.toList());
    } else {
      List<String> parentMetapaths = parent.getMetapaths().stream()
          .collect(Collectors.toList());
      metapaths = CollectionUtil.listOrEmpty(contextObj.getMetapaths()).stream()
          .map(MetaschemaMetapath::getTarget)
          .flatMap(childPath -> parentMetapaths.stream()
              .map(parentPath -> parentPath + '/' + childPath))
          .collect(Collectors.toList());
    }

    AssemblyConstraints contextConstraints = contextObj.getConstraints();
    IModelConstrained constraints = new AssemblyConstraintSet();
    if (contextConstraints != null) {
      ConstraintBindingSupport.parse(constraints, contextConstraints, source);
    }
    Context context = new Context(metapaths, constraints);

    List<Context> childContexts = CollectionUtil.listOrEmpty(contextObj.getContexts()).stream()
        .map(childObj -> parseContext(ObjectUtils.notNull(childObj), context, source))
        .collect(Collectors.toList());

    context.addAll(childContexts);

    return context;
  }

  private static class Context {
    @NonNull
    private final List<String> metapaths;
    @NonNull
    private final List<Context> childContexts = new LinkedList<>();
    @NonNull
    private final Lazy<List<ITargetedConstraints>> targetedConstraints;

    public Context(
        @NonNull List<String> metapaths,
        @NonNull IModelConstrained constraints) {
      this.metapaths = metapaths;
      this.targetedConstraints = ObjectUtils.notNull(Lazy.lazy(() -> {

        Stream<ITargetedConstraints> paths = getMetapaths().stream()
            .map(metapath -> new MetaTargetedContraints(ObjectUtils.notNull(metapath), constraints));
        Stream<ITargetedConstraints> childPaths = childContexts.stream()
            .flatMap(child -> child.getTargetedConstraints().stream());

        return Stream.concat(paths, childPaths)
            .collect(Collectors.toUnmodifiableList());
      }));
    }

    @NonNull
    public List<ITargetedConstraints> getTargetedConstraints() {
      return ObjectUtils.notNull(targetedConstraints.get());
    }

    public void addAll(@NonNull Collection<Context> childContexts) {
      childContexts.addAll(childContexts);
    }

    @NonNull
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
      getLetExpressions().values().forEach(definition::addLetExpression);
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
