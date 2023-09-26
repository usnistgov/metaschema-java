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

package gov.nist.secauto.metaschema.core.model.constraint;

import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.core.util.CollectionUtil;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public abstract class AbstractConstraint implements IConstraint { // NOPMD - intentional data class
  @Nullable
  private final String id;
  @Nullable
  private final String formalName;
  @Nullable
  private final MarkupLine description;
  @NonNull
  private final ISource source;
  @NonNull
  private final Level level;
  @NonNull
  private final MetapathExpression target;
  @Nullable
  private final MarkupMultiline remarks;
  @NonNull
  private final Map<QName, Set<String>> properties;

  /**
   * Construct a new Metaschema constraint.
   *
   * @param id
   *          the optional identifier for the constraint
   * @param formalName
   *          the constraint's formal name or {@code null} if not provided
   * @param description
   *          the constraint's semantic description or {@code null} if not
   *          provided
   * @param source
   *          information about the constraint source
   * @param level
   *          the significance of a violation of this constraint
   * @param target
   *          the Metapath expression identifying the nodes the constraint targets
   * @param properties
   *          a collection of associated properties
   * @param remarks
   *          optional remarks describing the intent of the constraint
   */
  protected AbstractConstraint(
      @Nullable String id,
      @Nullable String formalName,
      @Nullable MarkupLine description,
      @NonNull ISource source,
      @NonNull Level level,
      @NonNull MetapathExpression target,
      @NonNull Map<QName, Set<String>> properties,
      @Nullable MarkupMultiline remarks) {
    Objects.requireNonNull(target);
    this.id = id;
    this.formalName = formalName;
    this.description = description;
    this.source = source;
    this.level = ObjectUtils.requireNonNull(level, "level");
    this.target = ObjectUtils.requireNonNull(target, "target");
    this.properties = properties;
    this.remarks = remarks;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public MarkupLine getDescription() {
    return description;
  }

  @Override
  public String getFormalName() {
    return formalName;
  }

  @Override
  public ISource getSource() {
    return source;
  }

  @Override
  @NonNull
  public Level getLevel() {
    return level;
  }

  @Override
  public MetapathExpression getTarget() {
    return target;
  }

  @Override
  public Map<QName, Set<String>> getProperties() {
    return CollectionUtil.unmodifiableMap(properties);
  }

  @Override
  public MarkupMultiline getRemarks() {
    return remarks;
  }

  public abstract static class AbstractConstraintBuilder<
      T extends AbstractConstraintBuilder<T,
          R>,
      R extends AbstractConstraint> {
    private String id;
    private String formalName;
    private MarkupLine description;
    private ISource source;
    @NonNull
    private Level level = IConstraint.DEFAULT_LEVEL;
    @NonNull
    private MetapathExpression target = IConstraint.DEFAULT_TARGET;
    @NonNull
    private Map<QName, Set<String>> properties = new LinkedHashMap<>(); // NOPMD not thread safe
    private MarkupMultiline remarks;

    protected abstract T getThis();

    public T identifier(@NonNull String id) {
      this.id = id;
      return getThis();
    }

    public T formalName(@NonNull String name) {
      this.formalName = name;
      return getThis();
    }

    public T description(@NonNull MarkupLine description) {
      this.description = description;
      return getThis();
    }

    public T source(@NonNull ISource source) {
      this.source = source;
      return getThis();
    }

    public T level(@NonNull Level level) {
      this.level = level;
      return getThis();
    }

    public T target(@NonNull MetapathExpression target) {
      this.target = target;
      return getThis();
    }

    public T properties(@NonNull Map<QName, Set<String>> properties) {
      this.properties = properties;
      return getThis();
    }

    public T property(@NonNull QName name, @NonNull String value) {
      return property(name, CollectionUtil.singleton(value));
    }

    public T property(@NonNull QName name, @NonNull Set<String> newValues) {
      Set<String> existingValues = properties.get(name);
      if (existingValues == null) {
        existingValues = new LinkedHashSet<>();
        properties.put(name, existingValues);
      }

      existingValues.addAll(newValues);
      return getThis();
    }

    public T remarks(@NonNull MarkupMultiline remarks) {
      this.remarks = remarks;
      return getThis();
    }

    protected void validate() {
      ObjectUtils.requireNonNull(getSource());
    }

    @NonNull
    protected abstract R newInstance();

    @NonNull
    public R build() {
      validate();
      return newInstance();
    }

    protected String getId() {
      return id;
    }

    protected String getFormalName() {
      return formalName;
    }

    protected MarkupLine getDescription() {
      return description;
    }

    protected ISource getSource() {
      return source;
    }

    @NonNull
    protected Level getLevel() {
      return level;
    }

    @NonNull
    protected MetapathExpression getTarget() {
      return target;
    }

    @NonNull
    protected Map<QName, Set<String>> getProperties() {
      return properties;
    }

    protected MarkupMultiline getRemarks() {
      return remarks;
    }
  }
}
