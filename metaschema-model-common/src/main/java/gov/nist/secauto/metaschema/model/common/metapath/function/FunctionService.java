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

package gov.nist.secauto.metaschema.model.common.metapath.function;

import gov.nist.secauto.metaschema.model.common.metapath.ast.IExpression;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Stream;

public class FunctionService {
  private static FunctionService functionService;

  /**
   * Get the singleton instance of the function service.
   * 
   * @return the service instance
   */
  public static synchronized FunctionService getInstance() {
    if (functionService == null) {
      functionService = new FunctionService();
    }
    return functionService;
  }

  @NotNull
  private final ServiceLoader<@NotNull IFunctionLibrary> loader;
  @NotNull
  private LoadedFunctionsLibrary library;

  /**
   * Construct a new function service.
   */
  @SuppressWarnings("null")
  public FunctionService() {
    this.loader = ServiceLoader.load(IFunctionLibrary.class);
    this.library = load();
  }

  /**
   * Load all known functions registered with this function service.
   */
  @NotNull
  public synchronized LoadedFunctionsLibrary load() {
    ServiceLoader<@NotNull IFunctionLibrary> loader = getLoader();
    @SuppressWarnings("null")
    Stream<@NotNull IFunctionLibrary> libraryStream = loader.stream().map(Provider<IFunctionLibrary>::get);
    Stream<@NotNull IFunction> functions = libraryStream.flatMap(library -> {
      return library.getFunctionsAsStream();
    });

    this.library = new LoadedFunctionsLibrary();
    functions.forEachOrdered(function -> this.library.registerFunction(function));
    return this.library;
  }

  /**
   * Get the function service loader instance.
   * 
   * @return the service loader instance.
   */
  @NotNull
  protected ServiceLoader<@NotNull IFunctionLibrary> getLoader() {
    return loader;
  }

  /**
   * Retrieve the function with the provided name that supports the signature of the provided methods,
   * if such a function exists.
   * 
   * @param name
   *          the name of a group of functions
   * @param arguments
   *          a list of argument expressions for use in determining an argument signature match
   * @return the matching function or {@code null} if no match exists
   */
  @SuppressWarnings("null")
  public IFunction getFunction(@NotNull String name, @NotNull IExpression... arguments) {
    return getFunction(name, Arrays.asList(arguments));
  }

  /**
   * Retrieve the function with the provided name that supports the signature of the provided methods,
   * if such a function exists.
   * 
   * @param name
   *          the name of a group of functions
   * @param arguments
   *          a list of argument expressions for use in determining an argument signature match
   * @return the matching function or {@code null} if no match exists
   */
  public synchronized IFunction getFunction(@NotNull String name, @NotNull List<@NotNull IExpression> arguments) {
    return library.getFunction(name, arguments);
  }
}
