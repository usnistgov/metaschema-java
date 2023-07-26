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

package gov.nist.secauto.metaschema.model.common.constraint;

import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.metapath.DynamicContext;
import gov.nist.secauto.metaschema.model.common.metapath.ISequence;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.item.node.IDefinitionNodeItem;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Represents a rule constraining the model of a Metaschema assembly, field or flag. Provides a
 * common interface for all constraint definitions.
 */
public interface IConstraint {
  /**
   * The degree to which a constraint violation is significant.
   * <p>
   * These values are ordered from least significant to most significant.
   */
  enum Level {
    /**
     * A violation of the constraint represents a point of interest.
     */
    INFORMATIONAL,
    /**
     * A violation of the constraint represents a potential issue with the content.
     */
    WARNING,
    /**
     * A violation of the constraint represents a fault in the content. This may include issues around
     * compatibility, integrity, consistency, etc.
     */
    ERROR,
    /**
     * A violation of the constraint represents a serious fault in the content that will prevent typical
     * use of the content.
     */
    CRITICAL;
  }

  /**
   * The default level to use if no level is provided.
   */
  @NonNull
  Level DEFAULT_LEVEL = Level.ERROR;
  /**
   * The default target Metapath expression to use if no target is provided.
   */
  @NonNull
  MetapathExpression DEFAULT_TARGET = MetapathExpression.CONTEXT_NODE;
  /**
   * The default target Metapath expression to use if no target is provided.
   */
  @NonNull
  String DEFAULT_TARGET_METAPATH = ".";

  /**
   * Retrieve the unique identifier for the constraint.
   *
   * @return the identifier or {@code null} if no identifier is defined
   */
  @Nullable
  String getId();

  @Nullable
  MarkupLine getDescription();

  @Nullable
  String getFormalName();

  /**
   * Get information about the source of the constraint.
   *
   * @return the source information
   */
  @NonNull
  ISource getSource();

  /**
   * The significance of a violation of this constraint.
   *
   * @return the level
   */
  @NonNull
  Level getLevel();

  @NonNull
  Map<QName, Set<String>> getProperties();

  /**
   * Retrieve the Metapath expression to use to query the targets of the constraint.
   *
   * @return a Metapath expression
   */
  @NonNull
  MetapathExpression getTarget();

  /**
   * Based on the provided {@code contextNodeItem}, find all nodes matching the target expression.
   *
   * @param contextNodeItem
   *          the node item to evaluate the target expression against
   * @return the matching nodes as a sequence
   * @see #getTarget()
   */
  @NonNull
  default ISequence<? extends IDefinitionNodeItem<?, ?>> matchTargets(
      @NonNull IDefinitionNodeItem<?, ?> contextNodeItem) {
    return getTarget().evaluate(contextNodeItem);
  }

  /**
   * Based on the provided {@code contextNodeItem}, find all nodes matching the target expression.
   *
   * @param item
   *          the node item to evaluate the target expression against
   * @param dynamicContext
   *          the Metapath evaluation context to use
   * @return the matching nodes as a sequence
   * @see #getTarget()
   */
  @NonNull
  default ISequence<? extends IDefinitionNodeItem<?, ?>> matchTargets(@NonNull IDefinitionNodeItem<?, ?> item,
      @NonNull DynamicContext dynamicContext) {
    return item.hasValue() ? getTarget().evaluate(item, dynamicContext) : ISequence.empty();
  }

  /**
   * Retrieve the remarks associated with the constraint.
   *
   * @return the remarks or {@code null} if no remarks are defined
   */
  MarkupMultiline getRemarks();

  <T, R> R accept(@NonNull IConstraintVisitor<T, R> visitor, T state);

  interface ISource {
    enum SourceType {
      /**
       * A constraint embedded in a model.
       */
      MODEL,
      /**
       * A constraint defined externally from a model.
       */
      EXTERNAL;
    }

    @NonNull
    SourceType getSourceType();

    @Nullable
    URI getSource();
  }

  final class InternalModelSource implements ISource {
    @NonNull
    private static final ISource SINGLETON = new InternalModelSource();

    @NonNull
    public static ISource instance() {
      return SINGLETON;
    }

    private InternalModelSource() {
      // reduce visibility
    }

    @Override
    public SourceType getSourceType() {
      return SourceType.MODEL;
    }

    @Override
    public URI getSource() {
      // always null
      return null;
    }
  }

  final class ExternalModelSource implements IConstraint.ISource {
    @NonNull
    private static final Map<URI, ExternalModelSource> sources = new HashMap<>(); // NOPMD - intentional
    @NonNull
    private final URI modelUri;

    @NonNull
    public static ISource instance(@NonNull URI location) {
      ISource retval;
      synchronized (sources) {
        retval = sources.get(location);
        if (retval == null) {
          retval = new ExternalModelSource(location);
        }
      }
      return retval;
    }

    private ExternalModelSource(@NonNull URI modelSource) {
      this.modelUri = modelSource;
    }

    @Override
    public SourceType getSourceType() {
      return SourceType.MODEL;
    }

    @NonNull
    @Override
    public URI getSource() {
      return modelUri;
    }
  }

  final class ExternalSource implements IConstraint.ISource {
    @NonNull
    private static final Map<URI, ExternalSource> sources = new HashMap<>(); // NOPMD - intentional

    @NonNull
    private final URI modelUri;

    @NonNull
    public static ISource instance(@NonNull URI location) {
      ISource retval;
      synchronized (sources) {
        retval = sources.get(location);
        if (retval == null) {
          retval = new ExternalModelSource(location);
        }
      }
      return retval;
    }

    private ExternalSource(@NonNull URI modelSource) {
      this.modelUri = modelSource;
    }

    @Override
    public SourceType getSourceType() {
      return SourceType.EXTERNAL;
    }

    @NonNull
    @Override
    public URI getSource() {
      return modelUri;
    }
  }
}
