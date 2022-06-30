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

import gov.nist.secauto.metaschema.model.common.IAssemblyDefinition;
import gov.nist.secauto.metaschema.model.common.IAssemblyInstance;
import gov.nist.secauto.metaschema.model.common.IFieldDefinition;
import gov.nist.secauto.metaschema.model.common.IFieldInstance;
import gov.nist.secauto.metaschema.model.common.IFlagDefinition;
import gov.nist.secauto.metaschema.model.common.IFlagInstance;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.INamedDefinition;
import gov.nist.secauto.metaschema.model.common.IRootAssemblyDefinition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

public interface INodeItemFactory {

  @SuppressWarnings("null")
  @NotNull
  default INodeItem newNodeItem(@NotNull INamedDefinition definition, @NotNull Object value, @NotNull URI baseUri,
      boolean rootNode) {
    INodeItem retval;
    if (definition instanceof IAssemblyDefinition) {
      if (rootNode && definition instanceof IRootAssemblyDefinition) {
        retval = newDocumentNodeItem((IRootAssemblyDefinition) definition, value, baseUri);
      } else {
        retval = newAssemblyNodeItem((IAssemblyDefinition) definition, value, baseUri);
      }
    } else if (definition instanceof IFieldDefinition) {
      retval = newFieldNodeItem((IFieldDefinition) definition, value, baseUri);
    } else {
      throw new UnsupportedOperationException("must be a bound assembly or field");
    }
    return retval;
  }

  @NotNull
  default IFlagNodeItem newFlagNodeItem(@NotNull IFlagDefinition definition, @Nullable URI baseUri) {
    return new FlagDefinitionNodeItemImpl(definition, baseUri);
  }

  @NotNull
  default IFlagNodeItem newFlagNodeItem(@NotNull IFlagInstance instance,
      @NotNull IModelNodeItem parent) {
    return new FlagInstanceNodeItemImpl(instance, parent);
  }

  @NotNull
  default IRequiredValueFlagNodeItem newFlagNodeItem(@NotNull IFlagInstance instance,
      @NotNull IRequiredValueModelNodeItem parent, @NotNull Object value) {
    return new RequiredValueFlagInstanceNodeItemImpl(instance, parent, value);
  }

  @NotNull
  default IFieldNodeItem newFieldNodeItem(@NotNull IFieldDefinition definition, @Nullable URI baseUri) {
    return new FieldDefinitionNodeItemImpl(definition, baseUri);
  }

  @NotNull
  default IFieldNodeItem newFieldNodeItem(@NotNull IFieldInstance instance,
      @NotNull IAssemblyNodeItem parentNodeItem) {
    return new FieldInstanceNodeItemImpl(instance, parentNodeItem, 1);
  }

  @NotNull
  default IRequiredValueFieldNodeItem newFieldNodeItem(@NotNull IFieldDefinition definition,
      @NotNull Object value, @Nullable URI baseUri) {
    return new RequiredValueFieldDefinitionNodeItemImpl(definition, value, baseUri);
  }

  @NotNull
  default IRequiredValueFieldNodeItem newFieldNodeItem(@NotNull IFieldInstance instance,
      @NotNull IRequiredValueAssemblyNodeItem parent, int position, @NotNull Object value) {
    return new RequiredValueFieldInstanceNodeItemImpl(instance, parent, position, value);
  }

  @NotNull
  default IAssemblyNodeItem newAssemblyNodeItem(@NotNull IAssemblyDefinition definition, @Nullable URI baseUri) {
    return new AssemblyDefinitionNodeItemImpl(definition, baseUri);
  }

  @NotNull
  default IAssemblyNodeItem newAssemblyNodeItem(@NotNull IAssemblyInstance instance,
      @NotNull IAssemblyNodeItem parent) {
    return new AssemblyInstanceNodeItemImpl(instance, parent, 1);
  }

  default IAssemblyNodeItem newAssemblyNodeItem(@NotNull IAssemblyDefinition definition, @NotNull Object value,
      @Nullable URI baseUri) {
    return new RequiredValueAssemblyDefinitionNodeItemImpl(definition, value, baseUri);
  }

  @NotNull
  default IRequiredValueAssemblyNodeItem newAssemblyNodeItem(@NotNull IAssemblyInstance instance,
      @NotNull IRequiredValueAssemblyNodeItem parent, int position, @NotNull Object value) {
    return new RequiredValueAssemblyInstanceNodeItemImpl(instance, parent, position, value);
  }

  @NotNull
  default IDocumentNodeItem newDocumentNodeItem(@NotNull IRootAssemblyDefinition definition, @NotNull Object value,
      @NotNull URI documentUri) {
    return new DocumentNodeItemImpl(definition, value, documentUri);
  }

  @NotNull
  default IMetaschemaNodeItem newMetaschemaNodeItem(@NotNull IMetaschema metaschema) {
    return new MetaschemaNodeItemImpl(metaschema);
  }
}
