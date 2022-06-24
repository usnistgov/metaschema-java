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

package gov.nist.secauto.metaschema.model.common.metapath.format;

import gov.nist.secauto.metaschema.model.common.metapath.item.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IDocumentNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFieldNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IMetaschemaNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IModelNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.IRootAssemblyNodeItem;

import org.jetbrains.annotations.NotNull;

public class MetapathFormatter implements IPathFormatter {

  @Override
  public @NotNull String formatMetaschema(@NotNull IMetaschemaNodeItem metaschema) {
    // this will result in a slash being generated using the join in the format method
    return "";
  }

  @Override
  public String formatDocument(IDocumentNodeItem document) {
    // this will result in a slash being generated using the join in the format method
    return "";
  }

  @Override
  public String formatRootAssembly(@NotNull IRootAssemblyNodeItem root) {
    return root.getName();
  }

  @Override
  public String formatAssembly(@NotNull IAssemblyNodeItem assembly) {
    // TODO: does it make sense to use this for an intermediate that has no parent?
    return formatModelPathSegment(assembly);
  }

  @Override
  public String formatField(IFieldNodeItem field) {
    return formatModelPathSegment(field);
  }

  @Override
  public String formatFlag(IFlagNodeItem flag) {
    return "@" + flag.getName();
  }

  @SuppressWarnings("null")
  @NotNull
  protected String formatModelPathSegment(IModelNodeItem item) {
    StringBuilder builder = new StringBuilder(item.getName())
        .append('[')
        .append(item.getPosition())
        .append(']');
    return builder.toString();
  }
}
