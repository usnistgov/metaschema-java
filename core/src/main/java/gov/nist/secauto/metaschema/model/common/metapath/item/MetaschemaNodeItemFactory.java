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

import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.INamedModelInstance;
import gov.nist.secauto.metaschema.model.common.util.CollectionUtil;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class MetaschemaNodeItemFactory
    extends AbstractNodeItemFactory {
  @NonNull
  static final MetaschemaNodeItemFactory SINGLETON = new MetaschemaNodeItemFactory();

  /**
   * Get the singleton instance of this node factory.
   *
   * @return the node factory instance
   */
  @NonNull
  public static MetaschemaNodeItemFactory instance() {
    return SINGLETON;
  }

  private MetaschemaNodeItemFactory() {
    // prevent construction
  }

  @Override
  public INodeItemFactory getNodeItemFactory() {
    return MetaschemaNodeItemFactory.this;
  }

  @Override
  public Map<String, IFlagNodeItem> generateFlags(IModelNodeItem parent) {
    Map<String, IFlagNodeItem> retval = new LinkedHashMap<>(); // NOPMD - intentional

    for (IFlagInstance instance : parent.getDefinition().getFlagInstances()) {
      assert instance != null;
      IFlagNodeItem item = newFlagNodeItem(instance, parent, null);
      retval.put(instance.getEffectiveName(), item);
    }
    return retval.isEmpty() ? CollectionUtil.emptyMap() : CollectionUtil.unmodifiableMap(retval);
  }

  @Override
  public Map<String, List<IModelNodeItem>> generateModelItems(IAssemblyNodeItem parent) {
    Map<String, List<IModelNodeItem>> retval = new LinkedHashMap<>(); // NOPMD - intentional

    for (INamedModelInstance instance : CollectionUtil.toIterable(getNamedModelInstances(parent.getDefinition()))) {
      assert instance != null;
      IModelNodeItem item = newModelItem(instance, parent, 1, null);
      retval.put(instance.getEffectiveName(), Collections.singletonList(item));
    }
    return retval.isEmpty() ? CollectionUtil.emptyMap() : CollectionUtil.unmodifiableMap(retval);
  }
}
