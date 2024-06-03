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

package gov.nist.secauto.metaschema.schemagen;

import gov.nist.secauto.metaschema.core.model.IAssemblyDefinition;
import gov.nist.secauto.metaschema.core.model.IAssemblyInstance;
import gov.nist.secauto.metaschema.core.model.IChoiceInstance;
import gov.nist.secauto.metaschema.core.model.IDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldDefinition;
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.INamedInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstance;
import gov.nist.secauto.metaschema.core.model.INamedModelInstanceGrouped;
import gov.nist.secauto.metaschema.core.model.ModelWalker;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.codegen.JavaGenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ModuleIndex {
  private static final Logger LOGGER = LogManager.getLogger(JavaGenerator.class);

  private final Map<IDefinition, DefinitionEntry> index = new LinkedHashMap<>();// new ConcurrentHashMap<>();

  @NonNull
  public static ModuleIndex indexDefinitions(@NonNull IModule module, @NonNull IInlineStrategy inlineStrategy) {
    Collection<? extends IAssemblyDefinition> definitions = module.getExportedRootAssemblyDefinitions();
    ModuleIndex index = new ModuleIndex();
    if (!definitions.isEmpty()) {
      IndexVisitor visitor = new IndexVisitor(index, inlineStrategy);
      for (IAssemblyDefinition definition : definitions) {
        assert definition != null;

        // // add the root definition to the index
        // index.getEntry(definition).incrementReferenceCount();

        // walk the definition
        visitor.walk(ObjectUtils.requireNonNull(definition));
      }
    }
    return index;
  }

  public boolean hasEntry(@NonNull IDefinition definition) {
    return index.containsKey(definition);
  }

  @NonNull
  public DefinitionEntry getEntry(@NonNull IDefinition definition) {
    return ObjectUtils.notNull(index.computeIfAbsent(
        definition,
        k -> new ModuleIndex.DefinitionEntry(ObjectUtils.notNull(k))));
  }

  @NonNull
  public Collection<DefinitionEntry> getDefinitions() {
    return ObjectUtils.notNull(index.values());
  }

  private static class IndexVisitor
      extends ModelWalker<ModuleIndex> {
    @NonNull
    private final IInlineStrategy inlineStrategy;
    @NonNull
    private final ModuleIndex index;

    public IndexVisitor(@NonNull ModuleIndex index, @NonNull IInlineStrategy inlineStrategy) {
      this.index = index;
      this.inlineStrategy = inlineStrategy;
    }

    @Override
    protected ModuleIndex getDefaultData() {
      return index;
    }

    @Override
    protected boolean visit(IFlagInstance instance, ModuleIndex index) {
      handleInstance(instance);
      return true;
    }

    @Override
    protected boolean visit(IFieldInstance instance, ModuleIndex index) {
      handleInstance(instance);
      return true;
    }

    @Override
    protected boolean visit(IAssemblyInstance instance, ModuleIndex index) {
      handleInstance(instance);
      return true;
    }

    @Override
    protected void visit(IFlagDefinition def, ModuleIndex data) {
      handleDefinition(def);
    }

    // @Override
    // protected boolean visit(IAssemblyDefinition def, ModuleIndex data) {
    // // only walk if the definition hasn't already been visited
    // return !index.hasEntry(def);
    // }

    @Override
    protected boolean visit(IFieldDefinition def, ModuleIndex data) {
      return handleDefinition(def);
    }

    @Override
    protected boolean visit(IAssemblyDefinition def, ModuleIndex data) {
      return handleDefinition(def);
    }

    private boolean handleDefinition(@NonNull IDefinition definition) {
      DefinitionEntry entry = getDefaultData().getEntry(definition);
      boolean visited = entry.isVisited();
      if (!visited) {
        entry.markVisited();

        if (inlineStrategy.isInline(definition, index)) {
          entry.markInline();
        }
      }
      return !visited;
    }

    /**
     * Updates the index entry for the definition associated with the reference.
     *
     * @param instance
     *          the instance to process
     */
    @NonNull
    private DefinitionEntry handleInstance(INamedInstance instance) {
      IDefinition definition = instance.getDefinition();
      // check if this will be a new entry, which needs to be called before getEntry,
      // which will create it
      DefinitionEntry entry = getDefaultData().getEntry(definition);
      entry.addReference(instance);

      if (isChoice(instance)) {
        entry.markUsedAsChoice();
      }

      if (isChoiceSibling(instance)) {
        entry.markAsChoiceSibling();
      }
      return entry;
    }

    private static boolean isChoice(@NonNull INamedInstance instance) {
      return instance.getParentContainer() instanceof IChoiceInstance;
    }

    private static boolean isChoiceSibling(@NonNull INamedInstance instance) {
      IDefinition containingDefinition = instance.getContainingDefinition();
      return containingDefinition instanceof IAssemblyDefinition
          && !((IAssemblyDefinition) containingDefinition).getChoiceInstances().isEmpty();
    }
  }

  public static class DefinitionEntry {
    @NonNull
    private final IDefinition definition;
    private final Set<INamedInstance> references = new HashSet<>();
    private final AtomicBoolean inline = new AtomicBoolean(); // false
    private final AtomicBoolean visited = new AtomicBoolean(); // false
    private final AtomicBoolean usedAsChoice = new AtomicBoolean(); // false
    private final AtomicBoolean choiceSibling = new AtomicBoolean(); // false

    public DefinitionEntry(@NonNull IDefinition definition) {
      this.definition = definition;
    }

    @NonNull
    public IDefinition getDefinition() {
      return definition;
    }

    public boolean isRoot() {
      return definition instanceof IAssemblyDefinition
          && ((IAssemblyDefinition) definition).isRoot();
    }

    public boolean isReferenced() {
      return !references.isEmpty()
          || isRoot();
    }

    public Set<INamedInstance> getReferences() {
      return references;
    }

    public boolean addReference(@NonNull INamedInstance reference) {
      return references.add(reference);
    }

    public void markVisited() {
      visited.compareAndSet(false, true);
    }

    public boolean isVisited() {
      return visited.get();
    }

    public void markInline() {
      inline.compareAndSet(false, true);
    }

    public boolean isInline() {
      return inline.get();
    }

    public void markUsedAsChoice() {
      usedAsChoice.compareAndSet(false, true);
    }

    public boolean isUsedAsChoice() {
      return usedAsChoice.get();
    }

    public void markAsChoiceSibling() {
      choiceSibling.compareAndSet(false, true);
    }

    public boolean isChoiceSibling() {
      return choiceSibling.get();
    }

    public boolean isUsedAsJsonKey() {
      return references.stream()
          .anyMatch(ref -> ref instanceof INamedModelInstance
              && ((INamedModelInstance) ref).hasJsonKey());
    }

    public boolean isUsedWithoutJsonKey() {
      return definition instanceof IFlagDefinition
          || references.isEmpty()
          || references.stream()
              .anyMatch(ref -> ref instanceof INamedModelInstance
                  && !((INamedModelInstance) ref).hasJsonKey());
    }

    public boolean isChoiceGroupMember() {
      return references.stream()
          .anyMatch(INamedModelInstanceGrouped.class::isInstance);
    }
  }
}
