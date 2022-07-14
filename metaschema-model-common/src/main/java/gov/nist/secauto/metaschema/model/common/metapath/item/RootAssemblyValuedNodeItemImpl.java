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

import gov.nist.secauto.metaschema.model.common.IRootAssemblyDefinition;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

class RootAssemblyValuedNodeItemImpl
    extends AbstractModelNodeContext<
        IRequiredValueFlagNodeItem,
        IRequiredValueModelNodeItem,
        AbstractModelNodeContext.Model<IRequiredValueFlagNodeItem, IRequiredValueModelNodeItem>>
    implements IRootAssemblyNodeItem {
  @NotNull
  private final IRootAssemblyDefinition definition;
  @NotNull
  private final IDocumentNodeItem parent;
  @NotNull
  private final Object value;

  public RootAssemblyValuedNodeItemImpl(
      @NotNull IRootAssemblyDefinition definition,
      @NotNull IDocumentNodeItem parent,
      @NotNull Object value,
      @NotNull INodeItemFactory factory) {
    super(factory);
    this.definition = definition;
    this.parent = parent;
    this.value = value;
  }

  @Override
  protected @NotNull Supplier<Model<IRequiredValueFlagNodeItem, IRequiredValueModelNodeItem>>
      newModelSupplier(@NotNull INodeItemFactory factory) {
    return () -> {
      Map<@NotNull String, IRequiredValueFlagNodeItem> flags = factory.generateFlagsWithValues(this);
      Map<@NotNull String, List<@NotNull IRequiredValueModelNodeItem>> modelItems
          = factory.generateModelItemsWithValues(this);
      return new AbstractModelNodeContext.Model<>(flags, modelItems);
    };
  }

  @Override
  public IRootAssemblyDefinition getDefinition() {
    return definition;
  }

  @Override
  public IDocumentNodeItem getDocumentNodeItem() {
    return parent;
  }

  @Override
  public IRequiredValueAssemblyNodeItem getParentContentNodeItem() {
    // there is no parent assembly
    return null;
  }

  @Override
  @NotNull
  public Object getValue() {
    return value;
  }
}
