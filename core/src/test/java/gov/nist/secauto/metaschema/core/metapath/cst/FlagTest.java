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

package gov.nist.secauto.metaschema.core.metapath.cst;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ExpressionTestBase;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.cst.path.Flag;
import gov.nist.secauto.metaschema.core.metapath.cst.path.NameTest;
import gov.nist.secauto.metaschema.core.metapath.item.node.IFlagNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.IModelNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.NodeItemType;
import gov.nist.secauto.metaschema.core.model.IFlagInstance;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.jupiter.api.Test;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;

class FlagTest
    extends ExpressionTestBase {
  @Test
  void testFlagWithName() {
    DynamicContext dynamicContext = newDynamicContext();

    Mockery context = getContext();

    @SuppressWarnings("null")
    @NonNull IModelNodeItem<?, ?> focusItem = context.mock(IModelNodeItem.class);

    IFlagInstance instance = context.mock(IFlagInstance.class);
    IFlagNodeItem flagNode = context.mock(IFlagNodeItem.class);

    QName flagName = new QName("test");

    context.checking(new Expectations() {
      { // NOPMD - intentional
        allowing(focusItem).getNodeItem();
        will(returnValue(focusItem));
        allowing(focusItem).getNodeItemType();
        will(returnValue(NodeItemType.ASSEMBLY));
        allowing(focusItem).getFlagByName(flagName);
        will(returnValue(flagNode));

        allowing(flagNode).getInstance();
        will(returnValue(instance));

        allowing(instance).getEffectiveName();
        will(returnValue(flagName));

      }
    });

    Flag expr = new Flag(new NameTest(flagName));

    ISequence<?> result = expr.accept(dynamicContext, ISequence.of(focusItem));
    assertEquals(ISequence.of(flagNode), result, "Sequence does not match");
  }
}
