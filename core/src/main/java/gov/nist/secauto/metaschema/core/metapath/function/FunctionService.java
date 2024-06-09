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

package gov.nist.secauto.metaschema.core.metapath.function;

import gov.nist.secauto.metaschema.core.metapath.StaticMetapathException;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;

import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import edu.umd.cs.findbugs.annotations.NonNull;
import nl.talsmasoftware.lazy4j.Lazy;

public final class FunctionService {
  private static final Lazy<FunctionService> INSTANCE = Lazy.lazy(() -> new FunctionService());
  @NonNull
  private final ServiceLoader<IFunctionLibrary> loader;
  @NonNull
  private final IFunctionLibrary library;

  /**
   * Get the singleton instance of the function service.
   *
   * @return the service instance
   */
  public static FunctionService getInstance() {
    return INSTANCE.get();
  }

  /**
   * Construct a new function service.
   */
  @SuppressWarnings("null")
  public FunctionService() {
    this.loader = ServiceLoader.load(IFunctionLibrary.class);
    ServiceLoader<IFunctionLibrary> loader = getLoader();

    FunctionLibrary functionLibrary = new FunctionLibrary();
    loader.stream()
        .map(Provider<IFunctionLibrary>::get)
        .flatMap(IFunctionLibrary::stream)
        .forEachOrdered(function -> functionLibrary.registerFunction(ObjectUtils.notNull(function)));
    this.library = functionLibrary;
  }

  /**
   * Get the function service loader instance.
   *
   * @return the service loader instance.
   */
  @NonNull
  private ServiceLoader<IFunctionLibrary> getLoader() {
    return loader;
  }

  public Stream<IFunction> stream() {
    return this.library.stream();
  }

  /**
   * Retrieve the function with the provided name that supports the signature of
   * the provided methods, if such a function exists.
   *
   * @param name
   *          the name of a group of functions
   * @param arity
   *          the count of arguments for use in determining an argument signature
   *          match
   * @return the matching function or {@code null} if no match exists
   * @throws StaticMetapathException
   *           if a matching function was not found
   */
  public IFunction getFunction(@NonNull String name, int arity) {
    IFunction retval;
    synchronized (this) {
      retval = library.getFunction(name, arity);
    }

    if (retval == null) {
      throw new StaticMetapathException(StaticMetapathException.NO_FUNCTION_MATCH,
          String.format("unable to find function with name '%s' having arity '%d'", name, arity));
    }
    return retval;
  }

  /**
   * Retrieve the function with the provided name that supports the signature of
   * the provided methods, if such a function exists.
   *
   * @param name
   *          the name of a group of functions
   * @param arity
   *          the count of arguments for use in determining an argument signature
   *          match
   * @return the matching function or {@code null} if no match exists
   * @throws StaticMetapathException
   *           if a matching function was not found
   */
  public IFunction getFunction(@NonNull QName name, int arity) {
    IFunction retval;
    synchronized (this) {
      retval = library.getFunction(name, arity);
    }

    if (retval == null) {
      throw new StaticMetapathException(StaticMetapathException.NO_FUNCTION_MATCH,
          String.format("unable to find function with name '%s' having arity '%d'", name, arity));
    }
    return retval;
  }

}
