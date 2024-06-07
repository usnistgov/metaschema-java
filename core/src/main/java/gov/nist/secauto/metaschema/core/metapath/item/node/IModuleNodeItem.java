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

import gov.nist.secauto.metaschema.core.metapath.format.IPathFormatter;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;

import java.net.URI;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Supports querying of global definitions and associated instances in a
 * Metaschema module by effective name.
 * <p>
 * All definitions in the {@link ModuleScopeEnum#INHERITED} scope. This allows
 * the exported structure of the Metaschema module to be queried.
 */
public interface IModuleNodeItem extends IDocumentNodeItem, IFeatureNoDataItem {

  /**
   * The Metaschema module this item is based on.
   *
   * @return the Metaschema module
   */
  @NonNull
  IModule getModule();

  @Override
  default URI getDocumentUri() {
    return getModule().getLocation();
  }

  @Override
  default NodeItemType getNodeItemType() {
    return NodeItemType.METASCHEMA;
  }

  @Override
  default IModuleNodeItem getNodeItem() {
    return this;
  }

  @Override
  default String format(@NonNull IPathFormatter formatter) {
    return formatter.formatMetaschema(this);
  }

  @Override
  default <CONTEXT, RESULT> RESULT accept(@NonNull INodeItemVisitor<CONTEXT, RESULT> visitor, CONTEXT context) {
    return visitor.visitMetaschema(this, context);
  }
}
