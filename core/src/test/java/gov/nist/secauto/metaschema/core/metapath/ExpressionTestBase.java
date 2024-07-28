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

package gov.nist.secauto.metaschema.core.metapath;

import gov.nist.secauto.metaschema.core.metapath.item.node.IDocumentNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.net.URI;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public class ExpressionTestBase {
  @NonNull
  @RegisterExtension
  private final Mockery context = new JUnit5Mockery();

  /**
   * Get the mocking context.
   *
   * @return the mocking context
   */
  @NonNull
  protected Mockery getContext() {
    return context;
  }

  /**
   * Construct a new dynamic context for testing.
   *
   * @return the dynamic context
   */
  @NonNull
  protected static DynamicContext newDynamicContext() {
    URI baseUri = ObjectUtils.notNull(new File("").getAbsoluteFile().toURI());

    return new DynamicContext(StaticContext.builder()
        .baseUri(baseUri)
        .build());
  }

  /**
   * Get a mocked document node item.
   *
   * @return the mocked node item
   */
  @NonNull
  protected IDocumentNodeItem newDocumentNodeMock() {
    IDocumentNodeItem retval = getContext().mock(IDocumentNodeItem.class);
    assert retval != null;

    getContext().checking(new Expectations() {
      { // NOPMD - intentional
        allowing(retval).getNodeItem();
        will(returnValue(retval));
        allowing(retval).ancestorOrSelf();
        will(returnValue(Stream.of(retval)));
      }
    });

    return retval;
  }

  /**
   * Get a mocked node item.
   *
   * @param mockName
   *          the name of the mocked object
   *
   * @return the mocked node item
   */
  @NonNull
  protected INodeItem newNonDocumentNodeMock(@NonNull String mockName) {
    INodeItem retval = getContext().mock(INodeItem.class, mockName);
    assert retval != null;

    getContext().checking(new Expectations() {
      { // NOPMD - intentional
        allowing(retval).getNodeItem();
        will(returnValue(retval));
        allowing(retval).ancestorOrSelf();
        will(returnValue(Stream.of(retval)));
      }
    });

    return retval;
  }
}
