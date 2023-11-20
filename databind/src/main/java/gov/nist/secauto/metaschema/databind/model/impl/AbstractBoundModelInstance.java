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

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.BindingException;
import gov.nist.secauto.metaschema.databind.model.IAssemblyClassBinding;
import gov.nist.secauto.metaschema.databind.model.IBoundModelInstance;
import gov.nist.secauto.metaschema.databind.model.info.IModelInstanceCollectionInfo;

import java.lang.reflect.Field;
import java.util.Collection;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public abstract class AbstractBoundModelInstance
    extends AbstractProperty<IAssemblyClassBinding>
    implements IBoundModelInstance {
  // private static final Logger logger =
  // LogManager.getLogger(AbstractBoundModelInstance.class);

  @NonNull
  private final Field field;
  @NonNull
  private final Lazy<IModelInstanceCollectionInfo> collectionInfo;

  /**
   * Construct a new bound model instance based on a Java property. The name of
   * the property is bound to the name of the instance.
   *
   * @param field
   *          the field instance associated with this property
   *
   * @param containingDefinition
   *          the class binding for the field's containing class
   */
  protected AbstractBoundModelInstance(
      @NonNull Field field,
      @NonNull IAssemblyClassBinding containingDefinition) {
    super(containingDefinition);
    this.field = ObjectUtils.requireNonNull(field, "field");
    this.collectionInfo = ObjectUtils.notNull(Lazy.lazy(() -> IModelInstanceCollectionInfo.of(this)));
  }

  @Override
  public Field getField() {
    return field;
  }

  @Override
  public abstract int getMinOccurs();

  @Override
  public abstract int getMaxOccurs();

  /**
   * Gets information about the bound property.
   *
   * @return the collection information for the bound property
   */
  @SuppressWarnings("null")
  @Override
  @NonNull
  public IModelInstanceCollectionInfo getCollectionInfo() {
    return collectionInfo.get();
  }

  @Override
  public Collection<? extends Object> getItemValues(Object value) {
    return getCollectionInfo().getItemsFromValue(value);
  }

  @Override
  public void deepCopy(@NonNull Object fromInstance, @NonNull Object toInstance) throws BindingException {
    Object value = getValue(fromInstance);
    if (value != null) {
      value = getCollectionInfo().deepCopyItems(fromInstance, toInstance);
    }
    setValue(toInstance, value);
  }
}
