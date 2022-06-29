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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

/**
 * This class provides a singleton service to allow data types to be discovered within the system
 * based on an SPI provided by {@link IDataTypeProvider}.
 */
public class DataTypeService {
  private static DataTypeService instance;

  public static synchronized DataTypeService getInstance() {
    if (instance == null) {
      instance = new DataTypeService();
    }
    return instance;
  }

  private Map<String, IDataTypeAdapter<?>> libraryByName;
  @SuppressWarnings("rawtypes")
  private Map<Class<? extends IDataTypeAdapter>, IDataTypeAdapter<?>> libraryByClass;

  public DataTypeService() {
    load();
  }

  public synchronized IDataTypeAdapter<?> getJavaTypeAdapterByName(String name) {
    return libraryByName.get(name);
  }

  public synchronized IDataTypeAdapter<?>
      getJavaTypeAdapterByClass(@SuppressWarnings("rawtypes") Class<? extends IDataTypeAdapter> clazz) {
    return libraryByClass.get(clazz);
  }

  /**
   * Load available data types registered with the {@link IDataTypeProvider} SPI.
   * 
   * @throws IllegalStateException
   *           if there are two adapters with the same name
   */
  private synchronized void load() throws IllegalStateException {
    Map<String, IDataTypeAdapter<?>> libraryByName = new HashMap<>();
    @SuppressWarnings("rawtypes")
    Map<Class<? extends IDataTypeAdapter>, IDataTypeAdapter<?>> libraryByClass
        = new HashMap<>();
    ServiceLoader.load(IDataTypeProvider.class).stream()
        .map(Provider<IDataTypeProvider>::get)
        .flatMap(provider -> {
          return provider.getJavaTypeAdapters().entrySet().stream();
        }).forEach(entry -> {
          String datatypeName = entry.getKey();
          @SuppressWarnings("null")
          IDataTypeAdapter<?> adapter = entry.getValue();
          libraryByName.put(datatypeName, adapter);
          @SuppressWarnings("rawtypes")
          Class<? extends IDataTypeAdapter> clazz = adapter.getClass();
          libraryByClass.putIfAbsent(clazz, adapter);
        });
    this.libraryByName = libraryByName;
    this.libraryByClass = libraryByClass;
  }
}
