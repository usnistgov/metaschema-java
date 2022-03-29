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

package gov.nist.secauto.metaschema.binding.model.property;

import gov.nist.secauto.metaschema.binding.model.IBoundFlagDefinition;
import gov.nist.secauto.metaschema.binding.model.IClassBinding;
import gov.nist.secauto.metaschema.binding.model.ModelUtil;
import gov.nist.secauto.metaschema.binding.model.ValueConstraintSupport;
import gov.nist.secauto.metaschema.binding.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonFieldValueKeyFlag;
import gov.nist.secauto.metaschema.binding.model.annotations.JsonKey;
import gov.nist.secauto.metaschema.binding.model.annotations.NullJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.IMetaschema;
import gov.nist.secauto.metaschema.model.common.ModuleScopeEnum;
import gov.nist.secauto.metaschema.model.common.constraint.IAllowedValuesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IExpectConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IIndexHasKeyConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IMatchesConstraint;
import gov.nist.secauto.metaschema.model.common.constraint.IValueConstraintSupport;
import gov.nist.secauto.metaschema.model.common.datatype.IJavaTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.model.common.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.model.common.metapath.MetapathExpression;
import gov.nist.secauto.metaschema.model.common.metapath.evaluate.instance.IInstanceSet;
import gov.nist.secauto.metaschema.model.common.util.ObjectUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

public class DefaultFlagProperty
    extends AbstractFlagProperty implements IBoundJavaField {
  // private static final Logger logger = LogManager.getLogger(DefaultFlagProperty.class);
  @NotNull
  private final Field field;
  @NotNull
  private final BoundFlag flag;
  @NotNull
  private final IJavaTypeAdapter<?> javaTypeAdapter;
  private InternalFlagDefinition definition;

  /**
   * Construct a new bound flag instance based on a Java property. The name of the property is bound
   * to the name of the instance.
   * 
   * @param field
   *          the Java field to bind to
   * @param parentClassBinding
   *          the class binding for the field's containing class
   */
  public DefaultFlagProperty(@NotNull Field field, @NotNull IClassBinding parentClassBinding) {
    super(parentClassBinding);
    this.field = ObjectUtils.requireNonNull(field, "field");
    this.flag = ObjectUtils.requireNonNull(field.getAnnotation(BoundFlag.class));

    Class<? extends IJavaTypeAdapter<?>> adapterClass = ObjectUtils.notNull(this.flag.typeAdapter());
    if (NullJavaTypeAdapter.class.equals(adapterClass)) {
      this.javaTypeAdapter = MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
    } else {
      this.javaTypeAdapter = ObjectUtils.requireNonNull(
          parentClassBinding.getBindingContext().getJavaTypeAdapterInstance(adapterClass));
    }
  }

  @Override
  public @NotNull Field getField() {
    return field;
  }

  @NotNull
  protected BoundFlag getFlagAnnotation() {
    return flag;
  }

  @Override
  public boolean isJsonKey() {
    return getField().isAnnotationPresent(JsonKey.class);
  }

  @Override
  public boolean isJsonValueKey() {
    return getField().isAnnotationPresent(JsonFieldValueKeyFlag.class);
  }

  @Override
  public boolean isRequired() {
    return getFlagAnnotation().required();
  }

  @NotNull
  protected IJavaTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @Override
  public String getUseName() {
    return ModelUtil.resolveLocalName(getFlagAnnotation().useName(), getJavaPropertyName());
  }

  @Override
  public String getXmlNamespace() {
    return ModelUtil.resolveOptionalNamespace(getFlagAnnotation().namespace(), getParentClassBinding());
  }

  @Override
  public MarkupMultiline getRemarks() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("null")
  @Override
  public IBoundFlagDefinition getDefinition() {
    synchronized (this) {
      if (definition == null) {
        definition = new InternalFlagDefinition();
      }
    }
    return definition;
  }



  @Override
  public @NotNull IInstanceSet evaluateMetapathInstances(@NotNull MetapathExpression expression) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("null")
  @Override
  public String toCoordinates() {
    return String.format("%s Instance(%s): %s:%s",
        getModelType().name().toLowerCase(Locale.ROOT),
        getName(),
        getParentClassBinding().getBoundClass().getName(),
        getField().getName());
  }

  private class InternalFlagDefinition implements IBoundFlagDefinition {
    private IValueConstraintSupport constraints;


    /**
     * Used to generate the instances for the constraints in a lazy fashion when the constraints are
     * first accessed.
     */
    protected void checkModelConstraints() {
      synchronized (this) {
        if (constraints == null) {
          constraints = new ValueConstraintSupport(getFlagAnnotation());
        }
      }
    }

    @Override
    public IJavaTypeAdapter<?> getJavaTypeAdapter() {
      return DefaultFlagProperty.this.getJavaTypeAdapter();
    }

    @Override
    public boolean isInline() {
      return true;
    }

    @Override
    public IBoundFlagInstance getInlineInstance() {
      return DefaultFlagProperty.this;
    }

    @Override
    public String getName() {
      return DefaultFlagProperty.this.getName();
    }

    @Override
    public String getUseName() {
      return null;
    }

    @Override
    public MarkupMultiline getRemarks() {
      return DefaultFlagProperty.this.getRemarks();
    }

    @Override
    public String toCoordinates() {
      return DefaultFlagProperty.this.toCoordinates();
    }

    @Override
    public List<? extends IConstraint> getConstraints() {
      checkModelConstraints();
      return constraints.getConstraints();
    }

    @Override
    public List<? extends IAllowedValuesConstraint> getAllowedValuesContraints() {
      checkModelConstraints();
      return constraints.getAllowedValuesContraints();
    }

    @Override
    public List<? extends IMatchesConstraint> getMatchesConstraints() {
      checkModelConstraints();
      return constraints.getMatchesConstraints();
    }

    @Override
    public List<? extends IIndexHasKeyConstraint> getIndexHasKeyConstraints() {
      checkModelConstraints();
      return constraints.getIndexHasKeyConstraints();
    }

    @Override
    public List<? extends IExpectConstraint> getExpectConstraints() {
      checkModelConstraints();
      return constraints.getExpectConstraints();
    }

    @Override
    public String getFormalName() {
      // TODO: implement
      return null;
    }

    @Override
    public MarkupLine getDescription() {
      // TODO: implement
      return null;
    }

    @Override
    public @NotNull ModuleScopeEnum getModuleScope() {
      // TODO: is this the right value?
      return ModuleScopeEnum.INHERITED;
    }

    @Override
    public IMetaschema getContainingMetaschema() {
      return DefaultFlagProperty.this.getContainingMetaschema();
    }
  }

}
