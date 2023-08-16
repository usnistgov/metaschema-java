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
import gov.nist.secauto.metaschema.core.model.IFieldInstance;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;
import gov.nist.secauto.metaschema.core.model.IMetaschema;
import gov.nist.secauto.metaschema.core.model.INamedInstance;
import gov.nist.secauto.metaschema.core.model.ModelWalker;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import edu.umd.cs.findbugs.annotations.NonNull;

public class MetaschemaIndex {
  private final Map<IDefinition, DefinitionEntry> index = new ConcurrentHashMap<>();

  @NonNull
  public static MetaschemaIndex indexDefinitions(@NonNull IMetaschema metaschema) {
    Collection<? extends IAssemblyDefinition> definitions = metaschema.getExportedRootAssemblyDefinitions();
    MetaschemaIndex index = new MetaschemaIndex();
    if (!definitions.isEmpty()) {
      IndexVisitor visitor = new IndexVisitor(index);
      for (IAssemblyDefinition definition : definitions) {
        assert definition != null;

        // add the root definition to the index
        index.getEntry(definition).incrementReferenceCount();

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
        k -> new DefinitionEntry(ObjectUtils.notNull(k))));
  }

  @NonNull
  public Collection<DefinitionEntry> getDefinitions() {
    return ObjectUtils.notNull(index.values());
  }

  private static class IndexVisitor
      extends ModelWalker<MetaschemaIndex> {
    private final MetaschemaIndex index;

    public IndexVisitor(@NonNull MetaschemaIndex index) {
      this.index = index;
    }

    @Override
    protected MetaschemaIndex getDefaultData() {
      return index;
    }

    @Override
    protected boolean visit(IFlagInstance instance, MetaschemaIndex data) {
      return handleInstance(instance);
    }

    @Override
    protected boolean visit(IFieldInstance instance, MetaschemaIndex data) {
      return handleInstance(instance);
    }

    @Override
    protected boolean visit(IAssemblyInstance instance, MetaschemaIndex data) {
      return handleInstance(instance);
    }

    @Override
    protected void visit(IFlagDefinition def, MetaschemaIndex data) {
      // do nothing
    }
    //
    // @Override
    // protected boolean visit(IAssemblyDefinition def, MetaschemaIndex data) {
    // // only walk if the definition hasn't already been visited
    // return !index.hasEntry(def);
    // }

    /**
     * Updates the index entry for the definition associated with the reference.
     *
     * @param instance
     *          the instance to process
     * @return {@code true} if this is the first time handling the definition this instance references,
     *         or {@code false} otherwise
     */
    private boolean handleInstance(INamedInstance instance) {
      IDefinition definition = instance.getDefinition();
      // check if this will be a new entry, which needs to be called before getEntry, which will create it
      final boolean exists = getDefaultData().hasEntry(definition);
      DefinitionEntry entry = getDefaultData().getEntry(definition);
      entry.incrementReferenceCount();

      if (isChoice(instance)) {
        entry.markUsedAsChoice();
      }

      if (isChoiceSibling(instance)) {
        entry.markAsChoiceSibling();
      }
      return !exists;
    }

    private static boolean isChoice(@NonNull INamedInstance instance) {
      return instance.getParentContainer() instanceof IChoiceInstance;
    }

    private static boolean isChoiceSibling(@NonNull INamedInstance instance) {
      IDefinition containingDefinition = instance.getOwningDefinition();
      return containingDefinition instanceof IAssemblyDefinition
          && !((IAssemblyDefinition) containingDefinition).getChoiceInstances().isEmpty();
    }
  }

  public static class DefinitionEntry {
    @NonNull
    private final IDefinition definition;
    private final AtomicInteger referenceCount = new AtomicInteger(); // 0
    private final AtomicBoolean usedAsChoice = new AtomicBoolean(); // false
    private final AtomicBoolean choiceSibling = new AtomicBoolean(); // false

    public DefinitionEntry(@NonNull IDefinition definition) {
      this.definition = definition;
    }

    public IDefinition getDefinition() {
      return definition;
    }

    public boolean isReferenced() {
      return getReferenceCount() > 0;
    }

    public int getReferenceCount() {
      return referenceCount.get();
    }

    public int incrementReferenceCount() {
      return referenceCount.incrementAndGet();
    }

    public int incrementReferenceCount(int increment) {
      return referenceCount.addAndGet(increment);
    }

    public boolean isInline() {
      return getDefinition().isInline();
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
  }
}
