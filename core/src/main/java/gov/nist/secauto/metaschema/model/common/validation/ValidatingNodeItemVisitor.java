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

package gov.nist.secauto.metaschema.model.common.validation;

import gov.nist.secauto.metaschema.model.common.constraint.IConstraintValidator;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.AbstractNodeItemVisitor;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IAssemblyNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IFieldNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IFlagNodeItem;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IMetaschemaNodeItem;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ValidatingNodeItemVisitor
    extends AbstractNodeItemVisitor<Boolean, IConstraintValidator> {

  @Override
  protected Boolean defaultResult() {
    return Boolean.TRUE;
  }

  @Override
  public Boolean visitFlag(IFlagNodeItem item, IConstraintValidator context) {
    context.validate(item);
    return super.visitFlag(item, context);
  }

  @Override
  public Boolean visitField(IFieldNodeItem item, IConstraintValidator context) {
    context.validate(item);
    return super.visitField(item, context);
  }

  @Override
  public Boolean visitAssembly(IAssemblyNodeItem item, IConstraintValidator context) {
    context.validate(item);
    return super.visitAssembly(item, context);
  }

  @Override
  public Boolean visitMetaschema(@NonNull IMetaschemaNodeItem item, IConstraintValidator context) {
    throw new UnsupportedOperationException("validation of a metaschema node item is not needed");
  }
}
