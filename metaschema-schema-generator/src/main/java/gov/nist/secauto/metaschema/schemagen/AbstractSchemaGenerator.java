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

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.IChoiceInstance;
import gov.nist.secauto.metaschema.model.common.IDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IModelDefinition;
import gov.nist.secauto.metaschema.model.common.INamedInstance;
import gov.nist.secauto.metaschema.model.common.configuration.IConfiguration;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;
import gov.nist.secauto.metaschema.model.common.util.ModelWalker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import edu.umd.cs.findbugs.annotations.NonNull;

public abstract class AbstractSchemaGenerator implements ISchemaGenerator {

  @NonNull
  protected IInlineStrategy newInlineStrategy(@NonNull IConfiguration<SchemaGenerationFeature> configuration,
      @NonNull Collection<? extends IDefinition> definitions) {
    IInlineStrategy retval;
    if (configuration.isFeatureEnabled(SchemaGenerationFeature.INLINE_DEFINITIONS)) {
      if (configuration.isFeatureEnabled(SchemaGenerationFeature.INLINE_CHOICE_DEFINITIONS)) {
        retval = IInlineStrategy.DEFINED_AS_INLINE;
      } else {
        retval = new ChoiceInlineStrategy(definitions);
      }
    } else {
      retval = IInlineStrategy.NONE_INLINE;
    }
    return retval;
  }

  private static class ChoiceInlineStrategy implements IInlineStrategy {
    @NonNull
    private final Map<IDefinition, Boolean> definitionInlinedMap;

    public ChoiceInlineStrategy(@NonNull Collection<? extends IDefinition> definitions) {
      ChoiceModelWalker walker = new ChoiceModelWalker();
      for (IDefinition definition : definitions) {
        assert definition != null;
        walker.walkDefinition(definition);
      }
      definitionInlinedMap = walker.getDefinitionInlinedMap();
    }

    @Override
    public boolean isInline(@NonNull IDefinition definition) {
      Boolean inlined = definitionInlinedMap.get(definition);
      return inlined != null && inlined;
    }
  }

  private static class ChoiceModelWalker
      extends ModelWalker<Integer> {
    @NonNull
    private final Map<IDefinition, Boolean> definitionInlinedMap = new HashMap<>(); // NOPMD - intentional
    private final Stack<IDefinition> visitStack = new Stack<>();

    @NonNull
    protected Map<IDefinition, Boolean> getDefinitionInlinedMap() {
      return CollectionUtil.unmodifiableMap(definitionInlinedMap);
    }

    /**
     * Update the inline status based on the following logic.
     * <p>
     * If the current status is {@code null} or {@code true}, then use the provided inline status.
     * <p>
     * Otherwise, keep the status as-is.
     * 
     * @param definition
     *          the target definition
     * @param inline
     *          the status to update to
     */
    protected void updateInlineStatus(@NonNull IDefinition definition, boolean inline) {
      Boolean value = definitionInlinedMap.get(definition);

      if (value == null || (value && value != inline)) { // NOPMD - readability
        definitionInlinedMap.put(definition, inline);
      } // or leave it as-is
    }

    @Override
    protected Integer getDefaultData() {
      return 0;
    }

    private static boolean isChoiceSibling(@NonNull INamedInstance instance) {
      IModelDefinition containingDefinition = instance.getContainingDefinition();
      return containingDefinition instanceof IAssemblyDefinition
          && !((IAssemblyDefinition) containingDefinition).getChoiceInstances().isEmpty();
    }

    @Override
    protected boolean visit(@NonNull IFlagInstance instance, Integer data) {
      if (isChoiceSibling(instance)) {
        // choice siblings must not be inline
        updateInlineStatus(instance.getDefinition(), false);
      }
      return true;
    }

    @Override
    protected boolean visit(@NonNull IFieldInstance instance, Integer data) {
      if (isChoiceSibling(instance)) {
        // choice siblings must not be inline
        updateInlineStatus(instance.getDefinition(), false);
      }
      return true;
    }

    @Override
    protected boolean visit(@NonNull IAssemblyInstance instance, Integer data) {
      if (isChoiceSibling(instance)) {
        // choice siblings must not be inline
        updateInlineStatus(instance.getDefinition(), false);
      }
      return true;
    }

    @Override
    protected void visit(@NonNull IFlagDefinition def, Integer choiceDepth) {
      updateInlineStatus(def, def.isInline());
    }

    @Override
    protected boolean visit(@NonNull IFieldDefinition def, Integer choiceDepth) {
      boolean inline = def.isInline() && choiceDepth == 0;
      updateInlineStatus(def, inline);
      return true;
    }

    @Override
    protected boolean visit(@NonNull IAssemblyDefinition def, Integer choiceDepth) {
      boolean inline = def.isInline() && choiceDepth == 0;
      updateInlineStatus(def, inline);
      return true;
    }

    @Override
    public void walk(@NonNull IAssemblyDefinition def, Integer choiceDepth) {
      if (!visitStack.contains(def)) {
        visitStack.push(def);
        // ignore depth on children, since they can make their own decision
        super.walk(def, 0);
        visitStack.pop();
      }
    }

    @Override
    public void walk(@NonNull IChoiceInstance instance, Integer choiceDepth) {
      int newDepth = choiceDepth.intValue() + 1;
      super.walk(instance, newDepth);
    }
  }
}
