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

package gov.nist.secauto.metaschema.core.datatype;

import gov.nist.secauto.metaschema.core.util.CustomCollectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import nl.talsmasoftware.lazy4j.Lazy;

/**
 * This class provides a singleton service to allow data types to be discovered
 * within the system based on an SPI provided by {@link IDataTypeProvider}.
 */
public final class DataTypeService {
  private static final Logger LOGGER = LogManager.getLogger(DataTypeService.class);
  private static final Lazy<DataTypeService> INSTANCE = Lazy.lazy(() -> new DataTypeService());

  private final Map<String, IDataTypeAdapter<?>> libraryByName;
  private final Map<Class<? extends IDataTypeAdapter<?>>, IDataTypeAdapter<?>> libraryByClass;

  /**
   * Get the singleton service instance, which will be lazy constructed on first
   * access.
   *
   * @return the service instance
   */
  @SuppressWarnings("null")
  @NonNull
  public static DataTypeService getInstance() {
    return INSTANCE.get();
  }

  private DataTypeService() {

    ServiceLoader<IDataTypeProvider> loader = ServiceLoader.load(IDataTypeProvider.class);
    List<IDataTypeAdapter<?>> dataTypes = loader.stream()
        .map(Provider<IDataTypeProvider>::get)
        .flatMap(provider -> provider.getJavaTypeAdapters().stream())
        .collect(Collectors.toList());

    Map<String, IDataTypeAdapter<?>> libraryByName = dataTypes.stream()
        .flatMap(dataType -> dataType.getNames().stream()
            .map(name -> Map.entry(name, dataType)))
        .collect(CustomCollectors.toMap(
            Map.Entry::getKey,
            (entry) -> entry.getValue(),
            (key, v1, v2) -> {
              if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Data types '{}' and '{}' have duplicate name '{}'. Using the first.",
                    v1.getClass().getName(),
                    v2.getClass().getName(),
                    key);
              }
              return v1;
            },
            ConcurrentHashMap::new));

    @SuppressWarnings({ "unchecked", "null" }) Map<Class<? extends IDataTypeAdapter<?>>,
        IDataTypeAdapter<?>> libraryByClass = dataTypes.stream()
            .collect(CustomCollectors.toMap(
                dataType -> (Class<? extends IDataTypeAdapter<?>>) dataType.getClass(),
                Function.identity(),
                (key, v1, v2) -> {
                  if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Duplicate data type class '{}'. Using the first.",
                        key.getClass().getName());
                  }
                  return v1;
                },
                ConcurrentHashMap::new));
    this.libraryByName = libraryByName;
    this.libraryByClass = libraryByClass;
  }

  /**
   * Lookup a specific {@link IDataTypeAdapter} instance by its name.
   *
   * @param name
   *          the data type name of data type adapter to get the instance for
   * @return the instance or {@code null} if the instance is unknown to the type
   *         system
   */
  @Nullable
  public IDataTypeAdapter<?> getJavaTypeAdapterByName(@NonNull String name) {
    return libraryByName.get(name);
  }

  /**
   * Lookup a specific {@link IDataTypeAdapter} instance by its class.
   *
   * @param clazz
   *          the adapter class to get the instance for
   * @param <TYPE>
   *          the type of the requested adapter
   * @return the instance or {@code null} if the instance is unknown to the type
   *         system
   */
  @SuppressWarnings("unchecked")
  @Nullable
  public <TYPE extends IDataTypeAdapter<?>> TYPE getJavaTypeAdapterByClass(@NonNull Class<TYPE> clazz) {
    return (TYPE) libraryByClass.get(clazz);
  }
}
