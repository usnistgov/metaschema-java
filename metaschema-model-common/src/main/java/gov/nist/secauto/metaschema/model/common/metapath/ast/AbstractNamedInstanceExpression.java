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

package gov.nist.secauto.metaschema.model.common.metapath.ast;

import gov.nist.secauto.metaschema.model.common.instance.IInstance;
import gov.nist.secauto.metaschema.model.common.instance.INamedInstance;
import gov.nist.secauto.metaschema.model.common.metapath.item.INodeItem;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractNamedInstanceExpression<RESULT_TYPE extends INodeItem>
    extends AbstractPathExpression<RESULT_TYPE> {
  private static final WildcardMatcher WILDCARD = new WildcardMatcher();

  private final IExpression<?> node;

  public AbstractNamedInstanceExpression(IExpression<?> node) {
    this.node = node;
  }

  public IExpression<?> getNode() {
    return node;
  }

  public boolean isName() {
    return getNode() instanceof Name;
  }

  @Override
  public abstract Class<RESULT_TYPE> getBaseResultType();

  @Override
  public Class<RESULT_TYPE> getStaticResultType() {
    return getBaseResultType();
  }

  public Predicate<IInstance> getInstanceMatcher() {
    IExpression<?> node = getNode();

    Predicate<IInstance> retval;
    if (node instanceof Name) {
      retval = new NameMatcher(((Name) node).getValue());
    } else if (node instanceof Wildcard) {
      retval = WILDCARD;
    } else {
      throw new UnsupportedOperationException();
    }
    return retval;
  }

  @Override
  public List<? extends IExpression<?>> getChildren() {
    return node != null ? List.of(node) : Collections.emptyList();
  }

  private static class NameMatcher implements Predicate<IInstance> {
    private final String name;

    public NameMatcher(String name) {
      this.name = name;
    }

    protected String getName() {
      return name;
    }

    @Override
    public boolean test(IInstance instance) {
      boolean retval = false;
      if (instance instanceof INamedInstance) {
        if (getName().equals(((INamedInstance) instance).getEffectiveName())) {
          retval = true;
        }
      }
      return retval;
    }

  }

  private static class WildcardMatcher implements Predicate<IInstance> {

    @Override
    public boolean test(IInstance instance) {
      boolean retval = false;
      if (instance instanceof INamedInstance) {
        retval = true;
      }
      return retval;
    }

  }
}
