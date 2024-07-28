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

package gov.nist.secauto.metaschema.core.metapath.cst.path;

import gov.nist.secauto.metaschema.core.metapath.DynamicContext;
import gov.nist.secauto.metaschema.core.metapath.ISequence;
import gov.nist.secauto.metaschema.core.metapath.cst.IExpressionVisitor;
import gov.nist.secauto.metaschema.core.metapath.item.ItemUtils;
import gov.nist.secauto.metaschema.core.metapath.item.node.IDefinitionNodeItem;
import gov.nist.secauto.metaschema.core.metapath.item.node.INodeItem;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.function.Predicate;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * The CST node for a Metapath
 * <a href="https://www.w3.org/TR/xpath-31/#doc-xpath31-Wildcard">wildcard name
 * test</a>.
 */
public class Wildcard implements INameTestExpression {
  @Nullable
  private final Predicate<IDefinitionNodeItem<?, ?>> matcher;

  /**
   * Construct a new wildcard name test expression using the provided matcher.
   *
   * @param matcher
   *          the matcher used to determine matching nodes
   */
  public Wildcard(@Nullable Predicate<IDefinitionNodeItem<?, ?>> matcher) {
    this.matcher = matcher;
  }

  @Override
  public <RESULT, CONTEXT> RESULT accept(IExpressionVisitor<RESULT, CONTEXT> visitor, CONTEXT context) {
    return visitor.visitWildcard(this, context);
  }

  @Override
  public ISequence<? extends INodeItem> accept(
      DynamicContext dynamicContext, ISequence<?> focus) {
    Stream<? extends INodeItem> nodes = focus.stream().map(ItemUtils::checkItemIsNodeItemForStep);
    if (matcher != null) {
      Predicate<IDefinitionNodeItem<?, ?>> test = matcher;
      nodes = nodes.filter(item -> {
        assert matcher != null;
        return !(item instanceof IDefinitionNodeItem) ||
            test.test((IDefinitionNodeItem<?, ?>) item);
      });
    }
    return ISequence.of(ObjectUtils.notNull(nodes));
  }

  /**
   * A wildcard matcher that matches a specific local name in any namespace.
   */
  public static class MatchAnyNamespace implements Predicate<IDefinitionNodeItem<?, ?>> {
    @NonNull
    private final String localName;

    /**
     * Construct the matcher using the provided local name for matching.
     *
     * @param localName
     *          the name used to match nodes
     */
    public MatchAnyNamespace(@NonNull String localName) {
      this.localName = localName;
    }

    @Override
    public boolean test(IDefinitionNodeItem<?, ?> item) {
      return localName.equals(item.getName().getLocalPart());
    }
  }

  /**
   * A wildcard matcher that matches any local name in a specific namespace.
   */
  public static class MatchAnyLocalName implements Predicate<IDefinitionNodeItem<?, ?>> {
    @NonNull
    private final String namespace;

    /**
     * Construct the matcher using the provided namespace for matching.
     *
     * @param namespace
     *          the namespace used to match nodes
     */
    public MatchAnyLocalName(@NonNull String namespace) {
      this.namespace = namespace;
    }

    @Override
    public boolean test(IDefinitionNodeItem<?, ?> item) {
      return namespace.equals(item.getName().getNamespaceURI());
    }
  }
}
