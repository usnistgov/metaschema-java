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

package gov.nist.secauto.metaschema.databind.model.impl;

import gov.nist.secauto.metaschema.core.datatype.IDataTypeAdapter;
import gov.nist.secauto.metaschema.core.datatype.adapter.MetaschemaDataTypeProvider;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupLine;
import gov.nist.secauto.metaschema.core.datatype.markup.MarkupMultiline;
import gov.nist.secauto.metaschema.core.model.IFlagDefinition;
import gov.nist.secauto.metaschema.core.model.IModule;
import gov.nist.secauto.metaschema.core.model.ModuleScopeEnum;
import gov.nist.secauto.metaschema.core.model.constraint.ISource;
import gov.nist.secauto.metaschema.core.model.constraint.IValueConstrained;
import gov.nist.secauto.metaschema.core.model.constraint.ValueConstraintSet;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IBoundFlagInstance;
import gov.nist.secauto.metaschema.databind.model.IClassBinding;
import gov.nist.secauto.metaschema.databind.model.annotations.BoundFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.Constants;
import gov.nist.secauto.metaschema.databind.model.annotations.JsonFieldValueKeyFlag;
import gov.nist.secauto.metaschema.databind.model.annotations.JsonKey;
import gov.nist.secauto.metaschema.databind.model.annotations.ModelUtil;
import gov.nist.secauto.metaschema.databind.model.annotations.NullJavaTypeAdapter;
import gov.nist.secauto.metaschema.databind.model.annotations.ValueConstraints;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

// REFACTOR: flatten instance and definition
class DefaultFlagInstance
    extends AbstractProperty<IClassBinding>
    implements IBoundFlagInstance {
  // private static final Logger logger =
  // LogManager.getLogger(DefaultFlagInstance.class);
  @NonNull
  private final Field field;
  @NonNull
  private final BoundFlag flag;
  @NonNull
  private final IDataTypeAdapter<?> javaTypeAdapter;
  @Nullable
  private final Object defaultValue;
  @NonNull
  private final Lazy<InternalFlagDefinition> definition;

  /**
   * Construct a new bound flag instance based on a Java property. The name of the
   * property is bound to the name of the instance.
   *
   * @param field
   *          the Java field to bind to
   * @param containingDefinition
   *          the class binding for the field's containing class
   */
  public DefaultFlagInstance(
      @NonNull Field field,
      @NonNull IClassBinding containingDefinition) {
    super(containingDefinition);
    this.field = ObjectUtils.requireNonNull(field, "field");
    this.flag = ObjectUtils.requireNonNull(field.getAnnotation(BoundFlag.class));

    Class<? extends IDataTypeAdapter<?>> adapterClass = ObjectUtils.notNull(this.flag.typeAdapter());
    if (NullJavaTypeAdapter.class.equals(adapterClass)) {
      this.javaTypeAdapter = MetaschemaDataTypeProvider.DEFAULT_DATA_TYPE;
    } else {
      this.javaTypeAdapter = ObjectUtils.requireNonNull(
          containingDefinition.getBindingContext().getJavaTypeAdapterInstance(adapterClass));
    }

    String defaultString = this.flag.defaultValue();
    this.defaultValue = Constants.NULL_VALUE.equals(defaultString) ? null // NOPMD readability
        : getJavaTypeAdapter().parse(defaultString);
    this.definition = ObjectUtils.notNull(Lazy.lazy(() -> new InternalFlagDefinition()));
  }

  @Override
  @NonNull
  public Field getField() {
    return field;
  }

  @NonNull
  private BoundFlag getFlagAnnotation() {
    return flag;
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
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

  @Override
  public final IDataTypeAdapter<?> getJavaTypeAdapter() {
    return javaTypeAdapter;
  }

  @Override
  public String getFormalName() {
    return ModelUtil.resolveNoneOrValue(getFlagAnnotation().formalName());
  }

  @Override
  public MarkupLine getDescription() {
    return ModelUtil.resolveToMarkupLine(getFlagAnnotation().description());
  }

  @Override
  public String getUseName() {
    return ModelUtil.resolveNoneOrValue(getFlagAnnotation().useName());
  }

  @Override
  public Integer getUseIndex() {
    int value = getFlagAnnotation().useIndex();
    return value == Integer.MIN_VALUE ? null : value;
  }

  @Override
  public String getXmlNamespace() {
    return ModelUtil.resolveOptionalNamespace(getFlagAnnotation().namespace(), getContainingDefinition());
  }

  @Override
  public MarkupMultiline getRemarks() {
    return ModelUtil.resolveToMarkupMultiline(getFlagAnnotation().remarks());
  }

  @Override
  public IFlagDefinition getDefinition() {
    return ObjectUtils.notNull(definition.get());
  }

  @SuppressWarnings("null")
  @Override
  public String toCoordinates() {
    return String.format("%s Instance(%s): %s:%s",
        getModelType().name().toLowerCase(Locale.ROOT),
        getName(),
        getField().getDeclaringClass().getName(),
        getField().getName());
  }

  @Override
  public void deepCopy(Object fromInstance, Object toInstance) throws BindingException {
    Object value = getValue(fromInstance);
    if (value != null) {
      setValue(toInstance, deepCopyItem(value, toInstance));
    }
  }

  @Override
  public String getValueAsString(Object value) {
    return value == null ? null : getDefinition().getJavaTypeAdapter().asString(value);
  }

  private final class InternalFlagDefinition implements IFlagDefinition {
    @NonNull
    private final Lazy<IValueConstrained> constraints;

    private InternalFlagDefinition() {
      this.constraints = ObjectUtils.notNull(Lazy.lazy(() -> {
        IValueConstrained retval = new ValueConstraintSet();
        ValueConstraints valueAnnotation = getFlagAnnotation().valueConstraints();
        ConstraintSupport.parse(valueAnnotation, ISource.modelSource(), retval);
        return retval;
      }));
    }

    @SuppressWarnings("null")
    @Override
    public IValueConstrained getConstraintSupport() {
      return constraints.get();
    }

    @Override
    public String getFormalName() {
      return DefaultFlagInstance.this.getFormalName();
    }

    @Override
    public MarkupLine getDescription() {
      return DefaultFlagInstance.this.getDescription();
    }

    @Override
    public @NonNull Map<QName, Set<String>> getProperties() {
      // TODO: implement
      throw new UnsupportedOperationException();
    }

    @Override
    public IDataTypeAdapter<?> getJavaTypeAdapter() {
      return DefaultFlagInstance.this.getJavaTypeAdapter();
    }

    @Override
    public Object getDefaultValue() {
      return DefaultFlagInstance.this.getDefaultValue();
    }

    @Override
    public boolean isInline() {
      // flags are always inline
      return true;
    }

    @Override
    public IBoundFlagInstance getInlineInstance() {
      return DefaultFlagInstance.this;
    }

    @Override
    public String getName() {
      return getJavaFieldName();
    }

    @Override
    public Integer getIndex() {
      return DefaultFlagInstance.this.getIndex();
    }

    @Override
    public String getUseName() {
      // always use the name instead
      return null;
    }

    @Override
    public Integer getUseIndex() {
      // always use the index instead
      return null;
    }

    @Override
    public MarkupMultiline getRemarks() {
      return DefaultFlagInstance.this.getRemarks();
    }

    @Override
    public String toCoordinates() {
      return DefaultFlagInstance.this.toCoordinates();
    }

    @Override
    public @NonNull ModuleScopeEnum getModuleScope() {
      // TODO: is this the right value?
      return ModuleScopeEnum.INHERITED;
    }

    @Override
    public IModule getContainingModule() {
      return DefaultFlagInstance.this.getContainingModule();
    }
  }
}
