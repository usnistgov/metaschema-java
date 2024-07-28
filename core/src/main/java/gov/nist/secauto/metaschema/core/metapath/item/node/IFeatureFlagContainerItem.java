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

package gov.nist.secauto.metaschema.core.metapath.item.node;

import gov.nist.secauto.metaschema.core.model.IModelDefinition;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * This mixin interface indicates that the implementation is a {@link INodeItem}
 * that is based on an {@link IModelDefinition}. This means it has flag
 * children.
 * <p>
 * If an implementation may have flag and model children, or model children
 * only, then the {@link IFeatureModelContainerItem} should be used instead.
 */
public interface IFeatureFlagContainerItem extends INodeItem {

  /**
   * Get the model implementation that potentially contains flags.
   *
   * @return the model
   */
  @NonNull
  FlagContainer getModel();

  @Override
  default Collection<IFlagNodeItem> getFlags() {
    return getModel().getFlags();
  }

  @Override
  default IFlagNodeItem getFlagByName(@NonNull QName name) {
    return getModel().getFlagByName(name);
  }

  @Override
  default Collection<? extends List<? extends IModelNodeItem<?, ?>>> getModelItems() {
    // no model items
    return CollectionUtil.emptyList();
  }

  @Override
  default List<? extends IModelNodeItem<?, ?>> getModelItemsByName(QName name) {
    // no model items
    return CollectionUtil.emptyList();
  }

  /**
   * Provides an abstract implementation of a model that contains a collection of
   * flags.
   */
  class FlagContainer {
    @NonNull
    private final Map<QName, IFlagNodeItem> flags;

    /**
     * Initialize the container with the provided collection of flags.
     *
     * @param flags
     *          a flag mapping of qualified name to corresponding
     *          {@link IFlagNodeItem}
     */
    protected FlagContainer(@NonNull Map<QName, IFlagNodeItem> flags) {
      this.flags = flags;
    }

    /**
     * Get a flag in this container using the associated flag qualified name.
     *
     * @param name
     *          the qualified name of the flag
     * @return the corresponding flag item or {@code null} if no flag had the
     *         provided name
     */
    @Nullable
    public IFlagNodeItem getFlagByName(@NonNull QName name) {
      return flags.get(name);
    }

    /**
     * Get the flags in this container.
     *
     * @return the flags
     */
    @NonNull
    @SuppressWarnings("null")
    public Collection<IFlagNodeItem> getFlags() {
      return flags.values();
    }
  }
}
