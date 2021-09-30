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

package gov.nist.secauto.metaschema.binding.metapath.xdm;

import gov.nist.secauto.metaschema.binding.model.property.FlagProperty;
import gov.nist.secauto.metaschema.binding.model.property.NamedModelProperty;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Flag;
import gov.nist.secauto.metaschema.model.common.metapath.ast.Name;
import gov.nist.secauto.metaschema.model.common.metapath.xdm.IXdmAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.xdm.IXdmFlagNodeItem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public abstract class AbstractBoundXdmModelNodeItem<INSTANCE extends NamedModelProperty,
    PARENT extends IXdmAssemblyNodeItem> extends AbstractBoundXdmNodeItem<INSTANCE, PARENT>
    implements IBoundXdmModelNodeItem {

  private final int position;
  private Map<String, IBoundXdmFlagNodeItem> flags;

  public AbstractBoundXdmModelNodeItem(INSTANCE instance, Object value, int position, PARENT parentNodeItem) {
    super(instance, value, parentNodeItem);
    this.position = position;
  }

  @Override
  public int getPosition() {
    return position;
  }

  @Override
  public Map<String, ? extends IBoundXdmFlagNodeItem> getFlags() {
    initFlags();
    return flags;
  }

  protected synchronized void initFlags() {
    if (this.flags == null) {
      Map<String, IBoundXdmFlagNodeItem> flags = new LinkedHashMap<>();
      Object parentValue = getValue();
      for (FlagProperty instance : getDefinition().getFlagInstances().values()) {
        Object instanceValue = instance.getValue(parentValue);
        if (instanceValue != null) {
          IBoundXdmFlagNodeItem item = IXdmFactory.INSTANCE.newFlagNodeItem(instance, instanceValue, this);
          flags.put(instance.getEffectiveName(), item);
        }
      }
      this.flags = flags;
    }
  }

  public Stream<? extends IBoundXdmFlagNodeItem> flags() {
    return getFlags().values().stream();
  }

  public IBoundXdmFlagNodeItem getFlagByName(String name) {
    return getFlags().get(name);
  }

  @Override
  public Stream<? extends IBoundXdmFlagNodeItem> getMatchingChildFlags(Flag flag) {
    Stream<? extends IBoundXdmFlagNodeItem> retval;

    if (flag.isName()) {
      String name = ((Name) flag.getNode()).getValue();
      IBoundXdmFlagNodeItem item = getFlagByName(name);
      retval = item == null ? Stream.empty() : Stream.of(item);
    } else {
      // wildcard
      retval = flags();
    }
    return retval;
  }

}
