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

package gov.nist.secauto.metaschema.model.common.metapath.item;

import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class AbstractMetaschemaNodeItem implements IMetaschemaNodeItem {
  @NotNull
  private final IMetaschema metaschema;
  private Map<@NotNull String, IFlagNodeItem> flags;
  private Map<@NotNull String, ? extends List<@NotNull ? extends IModelNodeItem>> modelItems;

  public AbstractMetaschemaNodeItem(@NotNull IMetaschema metaschema) {
    this.metaschema = metaschema;
  }

  protected IMetaschema getMetaschema() {
    return metaschema;
  }

  @SuppressWarnings("unchecked")
  @Override
  public IMetaschema getValue() {
    return metaschema;
  }

  @Override
  public URI getBaseUri() {
    return metaschema.getLocation();
  }

  protected Map<@NotNull String, IFlagNodeItem> initFlags() {
    synchronized (this) {
      if (this.flags == null) {
        this.flags = newFlags();
      }
    }
    return this.flags;
  }

  protected abstract Map<@NotNull String, IFlagNodeItem> newFlags();

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull IFlagNodeItem> getFlags() {
    return initFlags().values();
  }

  @Override
  public IFlagNodeItem getFlagByName(@NotNull String name) {
    return initFlags().get(name);
  }

  protected Map<@NotNull String, ? extends List<@NotNull ? extends IModelNodeItem>> initModelItems() {
    synchronized (this) {
      if (this.modelItems == null) {
        this.modelItems = newModelItems();
      }
    }
    return this.modelItems;
  }

  protected abstract Map<@NotNull String, ? extends List<@NotNull ? extends IModelNodeItem>> newModelItems();

  @SuppressWarnings("null")
  @Override
  public Collection<@NotNull ? extends List<@NotNull ? extends IModelNodeItem>> getModelItems() {
    return initModelItems().values();
  }

  @SuppressWarnings("null")
  @Override
  public List<@NotNull ? extends IModelNodeItem> getModelItemsByName(String name) {
    List<@NotNull ? extends IModelNodeItem> result = initModelItems().get(name);
    return result == null ? CollectionUtil.emptyList() : result;
  }
}
