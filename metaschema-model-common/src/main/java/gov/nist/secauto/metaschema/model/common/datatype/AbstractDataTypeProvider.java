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

package gov.nist.secauto.metaschema.model.common.datatype;

import gov.nist.secauto.metaschema.model.common.datatype.adapter.IDataTypeAdapter;
import gov.nist.secauto.metaschema.model.common.datatype.adapter.MetaschemaDataTypeProvider;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A base implementation of an {@link IDataTypeProvider}, supporting dynamic loading of Metaschema data type
 * extensions at runtime.
 * <p>
 * The {@link MetaschemaDataTypeProvider} class provides an example of how to use this class to provide new data types.
 */
public abstract class AbstractDataTypeProvider implements IDataTypeProvider {
  private final Map<@NotNull String, IDataTypeAdapter<?>> library = new HashMap<>(); // NOPMD - synchronized
  private final Map<@NotNull Class<? extends IDataTypeAdapter<?>>, // NOPMD - synchronized
      IDataTypeAdapter<?>> libraryByClass = new HashMap<>();

  @SuppressWarnings("null")
  @Override
  public Map<@NotNull String, ? extends IDataTypeAdapter<?>> getJavaTypeAdapters() {
    synchronized (this) {
      return Collections.unmodifiableMap(library);
    }
  }

  public Map<@NotNull Class<? extends IDataTypeAdapter<?>>, IDataTypeAdapter<?>> getJavaTypeAdaptersByClass() {
    synchronized (this) {
      return Collections.unmodifiableMap(libraryByClass);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <TYPE extends IDataTypeAdapter<?>> TYPE getJavaTypeAdapterInstance(@NotNull Class<TYPE> clazz) {
    synchronized (this) {
      return (TYPE) libraryByClass.get(clazz);
    }
  }

  /**
   * Register the provided {@code adapter} with the type system.
   * 
   * @param adapter
   *          the adapter to register
   * @throws IllegalArgumentException
   *           if another type adapter is already bound to the same name
   */
  protected void registerDatatype(@NotNull IDataTypeAdapter<?> adapter) {
    String name = adapter.getName();

    registerDatatypeByName(name, adapter);
  }

  /**
   * Register the provided {@code adapter} with the type system using the provided {@code name}.
   * 
   * @param name
   *          the type name to register
   * @param adapter
   *          the adapter to register
   * @throws IllegalArgumentException
   *           if another type adapter is already bound to the same name
   */
  protected void registerDatatypeByName(@NotNull String name, @NotNull IDataTypeAdapter<?> adapter) {
    IDataTypeAdapter<?> duplicate;
    synchronized (this) {
      duplicate = library.put(name, adapter);
      @SuppressWarnings("unchecked")
      Class<? extends IDataTypeAdapter<?>> clazz = (Class<? extends IDataTypeAdapter<?>>) adapter.getClass();
      libraryByClass.put(clazz, adapter);
    }
    if (duplicate != null) {
      throw new IllegalArgumentException(String.format("Another adapter was registered with the name '%s'", name));
    }
  }
}
